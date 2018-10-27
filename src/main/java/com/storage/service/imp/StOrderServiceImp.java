package com.storage.service.imp;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.storage.entity.Customer;
import com.storage.entity.OrderShipping;
import com.storage.entity.Orderitem;
import com.storage.entity.Product;
import com.storage.entity.SellingRecord;
import com.storage.entity.Setting;
import com.storage.entity.StOrder;
import com.storage.entity.custom.CustomProduct;
import com.storage.entity.custom.OrderStatis;
import com.storage.entity.custom.OrderTableItem;
import com.storage.entity.custom.OrderWrap;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.OrderItemRepo;
import com.storage.repository.OrderRepo;
import com.storage.repository.OrderShippingRepo;
import com.storage.repository.ProductRepo;
import com.storage.service.SellingRecordService;
import com.storage.service.SettingService;
import com.storage.service.StOrderService;
import com.storage.utils.IDUtils;

@Service
@org.springframework.transaction.annotation.Transactional
public class StOrderServiceImp implements StOrderService {
	@Autowired
	OrderRepo orderRepo;
	@Autowired
	OrderItemRepo orderItemRepo;
	@Autowired
	ProductRepo productRepo;
	@Autowired
	OrderShippingRepo orderShippingRepo;
	@Autowired
	SettingService settingService;
	@Autowired
	SellingRecordService sellingRecordService;
	ObjectMapper mapper = new ObjectMapper();
	private static final Integer POUND = 1;
	private static final Integer RMB = 2;
	private static final String RMB_SYMBOL = "¥";
	private static final String POUND_SYMBOL = "£";

	private static final int UNPROCESSED = 1;
	private static final int CONFIRMED = 2;
	private static final int DISPATCHED = 3;
	private static final int FINISHED = 4;
	private static final int CLOSED = 5;
	@Resource(name = "redisTemplate")
	ValueOperations<String, String> opsForValue;
	private static final String ORDER_STATISTICS_NAME="orderStatistics";

	@Autowired
	StringRedisTemplate redisTemplate;
	@Autowired
	EntityManager manager;

	Logger logger = LoggerFactory.getLogger(StOrderServiceImp.class);

	@Override
	public StorageResult addStOrder(StOrder stOrder) {
		if (stOrder == null)
			return StorageResult.failed("invalid parameter");

		this.orderRepo.save(stOrder);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult deleteStOrderById(Integer id) {
		if (id == null || id < 0)
			return StorageResult.failed("invalid id");
		this.orderRepo.deleteById(id);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult getStOrderByExample(StOrder stOrder) {

		List<StOrder> results = this.orderRepo.findAll(Example.of(stOrder));

		return StorageResult.succeed(results);
	}

	@Override
	public StorageResult getStOrderById(Integer id) {
		if (id == null || id < 0)
			return StorageResult.failed("invalid id");

		Optional<StOrder> stOrder = this.orderRepo.findById(id);
		if (stOrder.isPresent())
			return StorageResult.succeed(stOrder);
		return StorageResult.failed("no such order");
	}

	@Override
	public StorageResult updateStOrderSelective(StOrder stOrder) {
		if (stOrder == null || stOrder.getId() == null || stOrder.getId() < 0)
			return StorageResult.failed("invalid parameter ");

		this.orderRepo.save(stOrder);
		return StorageResult.succeed();

	}

	@Override
	public StorageResult count() {
		long count = this.orderRepo.count();
		return StorageResult.succeed(count);
	}

	@Transactional
	@Override
	public OrderWrap creaOrder(Customer customer, OrderWrap result) {
		Integer customerId = customer.getId();
		// create order, set customer id, time
		StOrder record = new StOrder();
		record.setCustomerid(customerId);
		record.setBuyername(customer.getName());
		Date date = new Date();
		record.setCreatedtime(date);
		record.setClosedtime(new Date(System.currentTimeMillis() + 1000 * 60 * 60));
		record.setUpdatetime(date);

		// 1 need to be confirmed 3 canceled
		record.setStatus(UNPROCESSED);

		String orderNumber = IDUtils.getOrderId();
		record.setOrdernumber(orderNumber);
		if (result.getOrder() != null) {
			record.setComment(result.getOrder().getComment());
			record.setPayment(result.getOrder().getPayment());
			String payment = result.getOrder().getPayment();
			StorageResult<Setting> setting = settingService.getSetting();
			if (!setting.isSuccess()) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new IllegalAccessError("no payment url set up yet");
			}
			Setting result2 = setting.getResult();
			String payUrl;
			if (payment.equalsIgnoreCase("wechat")) {
				payUrl = result2.getWxPayUrl();
			} else {
				payUrl = result2.getApliPayUrl();
			}
			result.setPayUrl(payUrl);
		}

		List<CustomProduct> list = result.getList();
		List<Orderitem> orderItems = new ArrayList<>();
		BigDecimal totalPrice = new BigDecimal(0);
		for (CustomProduct customProduct : list) {
			Product save2 = productRepo.getOne(customProduct.getProduct().getId());
			if (save2.getQuantity() - customProduct.getQty() < 0) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				result.setCode(500);
				result.setMsg("shortage of products: " + save2.getName());
				return result;
			} else {
				save2.setQuantity(save2.getQuantity() - customProduct.getQty());
			}
			productRepo.save(save2);

			// adding record
			Optional<SellingRecord> sellingRecodByProductId = sellingRecordService
					.getSellingRecodByProductId(save2.getId());
			SellingRecord sellingRecord;
			if (sellingRecodByProductId.isPresent()) {
				sellingRecord = sellingRecodByProductId.get();
				sellingRecord.setSellingAmount(sellingRecord.getSellingAmount() + customProduct.getQty());
			} else {
				sellingRecord = new SellingRecord();
				sellingRecord.setProduct(save2);
				sellingRecord.setSellingAmount(customProduct.getQty());
				sellingRecord.setReturnedAmount(0);
			}
			sellingRecordService.addOrUpdateSellingRecord(sellingRecord);

			Orderitem item = new Orderitem();
			item.setProductName(save2.getName());
			item.setProductid(save2.getId());
			item.setQuantity(customProduct.getQty());
			item.setTotalprice(save2.getSellingprice_aftertax().multiply(new BigDecimal(customProduct.getQty())));
			totalPrice = totalPrice.add(item.getTotalprice());
			item.setUnitprice(save2.getSellingprice_aftertax());
			orderItems.add(item);
		}
		record.setTotalprice(totalPrice);
		StOrder save = this.orderRepo.save(record);

		Integer orderId = save.getId();
		for (Orderitem orderitem : orderItems) {
			orderitem.setOrderid(orderId);
			orderItemRepo.save(orderitem);
		}

		OrderShipping shipping = new OrderShipping();

		shipping.setAddress(customer.getAddress());
		shipping.setPostCode(customer.getPostcode());
		shipping.setReceiverPhone(customer.getPhone());
		shipping.setOrderId(orderId);
		shipping.setCreatedTime(date);
		shipping.setUpdatedTime(date);
		shipping.setReceiverName(customer.getName());
		shipping.setStaus("ongoing");
		shipping.setEmail(customer.getEmail());
		shipping.setPhone(customer.getPhone());
		orderShippingRepo.save(shipping);
		result.setCode(200);
		result.setMsg("success");
		result.setOrder(save);
		result.setDate(new Date());
		opsForValue.set(ORDER_STATISTICS_NAME, null);
		return result;
	}

	@HystrixCommand(fallbackMethod = "findAllTableItems_error")
	@Override
	public StorageResult<List<OrderTableItem>> findAllTableItems() {
		List<OrderTableItem> items = new ArrayList<>();
		List<StOrder> findAll = orderRepo.findAll();
		for (StOrder order : findAll) {
			OrderShipping example = new OrderShipping();
			example.setOrderId(order.getId());
			Optional<OrderShipping> findAll2 = orderShippingRepo.findOne(Example.of(example));

			OrderTableItem item = new OrderTableItem();
			item.setId(order.getId());
			item.setOrderNumber(order.getOrdernumber());
			item.setCreatedTime(order.getCreatedtime());
			item.setPrice(order.getTotalprice());
			Integer currencyDisplay = settingService.getSetting().getResult().getCurrencyDisplay();
			if (currencyDisplay == RMB) {
				item.setPriceDisplayed(RMB_SYMBOL + order.getTotalprice()
						.multiply(new BigDecimal(settingService.getSetting().getResult().getCurrencyRate())));
			} else {
				item.setPriceDisplayed(POUND_SYMBOL + order.getTotalprice().toPlainString());
			}
			item.setStatus(order.getStatus());
			item.setName(order.getBuyername());
			if (findAll2.isPresent())
				item.setPhone(findAll2.get().getPhone());

			items.add(item);

		}

		return StorageResult.succeed(items);
	}

	public StorageResult<List<OrderTableItem>> findAllTableItems_error(Throwable e) {
		e.printStackTrace();
		return StorageResult.failed("unknow error when getting order list");
	}

	/**
	 * update order table status
	 */
	@Override
	public StorageResult updateStOrder(OrderTableItem stOrder) {
		opsForValue.set(ORDER_STATISTICS_NAME,	null);
		if (stOrder == null || stOrder.getId() == null || stOrder.getId() < 0)
			return StorageResult.failed("invalid parameter ");
		StOrder one = orderRepo.getOne(stOrder.getId());
		one.setStatus(stOrder.getStatus());
		one.setUpdatetime(new Date());
		StOrder save = this.orderRepo.save(one);
		return StorageResult.succeed();
	}

	@HystrixCommand(fallbackMethod = "getInfoFromOrder_error")
	@Override
	public OrderWrap getInfoFromOrder(Integer orderId) {
		Optional<StOrder> opOrder = orderRepo.findById(orderId);
		OrderWrap orderWrap = new OrderWrap();
		if (!opOrder.isPresent()) {
			orderWrap.setCode(500);
			orderWrap.setMsg("no order detail present");
			return orderWrap;

		}
		StOrder order = opOrder.get();

		OrderShipping shipping = new OrderShipping();
		shipping.setOrderId(orderId);
		Optional<OrderShipping> findOne = orderShippingRepo.findOne(Example.of(shipping));
		if (!findOne.isPresent()) {
			orderWrap.setCode(500);
			orderWrap.setMsg("no shipping detail present");
			return orderWrap;
		}
		OrderShipping orderShipping = findOne.get();
		Customer customer = new Customer();
		customer.setAddress(orderShipping.getAddress());
		customer.setName(order.getBuyername());
		customer.setPhone(orderShipping.getPhone());
		customer.setPostcode(orderShipping.getPostCode());

		List<CustomProduct> list = new ArrayList<>();
		Orderitem example = new Orderitem();
		example.setOrderid(orderId);
		List<Orderitem> findAll = orderItemRepo.findAll(Example.of(example));

		for (Orderitem orderitem : findAll) {
			Integer productid = orderitem.getProductid();
			Optional<Product> findById = productRepo.findById(productid);
			CustomProduct e = new CustomProduct();
			if (!findById.isPresent()) {
				Product product = new Product();
				product.setId(null);
				product.setName(orderitem.getProductName() + "(deleted)");
				product.setTitle("");
				e.setProduct(product);
			} else {
				Product product = findById.get();
				e.setProduct(product);
				e.setQty(orderitem.getQuantity());
				e.setSubtotal(orderitem.getTotalprice());
			}
			list.add(e);
		}
		orderWrap.setList(list);
		orderWrap.setTotalPrice(order.getTotalprice());
		orderWrap.setCustomer(customer);
		orderWrap.setOrder(order);
		orderWrap.setDate(order.getCreatedtime());
		Setting setting = settingService.getSetting().getResult();

		Float currencyRate = setting.getCurrencyRate();
		Integer currencyDisplay = setting.getCurrencyDisplay();
		if (currencyDisplay == 2) {
			orderWrap.setTotalPrice(orderWrap.getTotalPrice().multiply(new BigDecimal(currencyRate)));
			orderWrap.setTotalPriceDisplay(RMB_SYMBOL + orderWrap.getTotalPrice().toPlainString());
			List<CustomProduct> list2 = orderWrap.getList();
			for (CustomProduct customProduct : list2) {
				customProduct.setSubtotal(customProduct.getSubtotal().multiply(new BigDecimal(currencyRate)));
				customProduct.setSubtotalDisplayed(RMB_SYMBOL + customProduct.getSubtotal().toPlainString());
				customProduct.getProduct().setSellingprice_aftertax(
						customProduct.getProduct().getSellingprice_aftertax().multiply(new BigDecimal(currencyRate)));
				customProduct.getProduct().setMoneyDisplayed(
						RMB_SYMBOL + customProduct.getProduct().getSellingprice_aftertax().toPlainString());
			}
		} else {
			orderWrap.setTotalPriceDisplay(POUND_SYMBOL + orderWrap.getTotalPrice().toPlainString());
			List<CustomProduct> list2 = orderWrap.getList();
			for (CustomProduct customProduct : list2) {
				customProduct.setSubtotalDisplayed(POUND_SYMBOL + customProduct.getSubtotal().toPlainString());
				customProduct.getProduct().setMoneyDisplayed(
						POUND_SYMBOL + customProduct.getProduct().getSellingprice_aftertax().toPlainString());

			}

		}

		String payUrl;
		if (order.getPayment() != null && order.getPayment().equalsIgnoreCase("wechat")) {
			payUrl = setting.getWxPayUrl();
		} else {
			payUrl = setting.getApliPayUrl();
		}
		orderWrap.setPayUrl(payUrl);

		return orderWrap;
	}

	public OrderWrap getInfoFromOrder_error(Integer orderid, Throwable e) {
		OrderWrap orderWrap = new OrderWrap();
		orderWrap.setCode(500);
		orderWrap.setMsg("unknow error");
		e.printStackTrace();
		return orderWrap;
	}

	@HystrixCommand(fallbackMethod = "findAllTableItemsByUserId_error")
	@Override
	public StorageResult<List<OrderTableItem>> findAllTableItemsByUserId(Integer Id) {
		List<OrderTableItem> items = new ArrayList<>();
		StOrder probe = new StOrder();
		probe.setCustomerid(Id);
		List<StOrder> findAll = orderRepo.findAll(Example.of(probe));
		Float currencyRate = settingService.getSetting().getResult().getCurrencyRate();
		Integer currencyDisplay = settingService.getSetting().getResult().getCurrencyDisplay();

		for (StOrder order : findAll) {
			OrderShipping example = new OrderShipping();
			example.setOrderId(order.getId());
			OrderTableItem item = new OrderTableItem();
			item.setId(order.getId());
			item.setOrderNumber(order.getOrdernumber());
			item.setCreatedTime(order.getCreatedtime());
			if (currencyDisplay == POUND) {
				item.setPrice(order.getTotalprice());
				item.setPriceDisplayed(POUND_SYMBOL + order.getTotalprice().toPlainString());
			} else {
				item.setPrice(order.getTotalprice().multiply(new BigDecimal(currencyRate)));
				item.setPriceDisplayed(RMB_SYMBOL + order.getTotalprice().toPlainString());
			}

			item.setStatus(order.getStatus());
			item.setName(order.getBuyername());
			items.add(item);
		}
		
		return StorageResult.succeed(items);
	}

	public StorageResult<List<OrderTableItem>> findAllTableItemsByUserId_error(Integer Id, Throwable e) {

		e.printStackTrace();
		return StorageResult.failed("error to retrive all order info");
	}

	@Override
	public OrderStatis getStatistics() {
		String string = opsForValue.get(ORDER_STATISTICS_NAME);
		if(!StringUtils.isEmpty(string))
			try {
				return mapper.readValue(string, OrderStatis.class);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		long total = orderRepo.count();
		OrderStatis orderStatis = new OrderStatis();
		orderStatis.setTotalNumber(total);

		StOrder probe = new StOrder();

		probe.setStatus(UNPROCESSED);
		Example<StOrder> example = Example.of(probe);
		long unprocessed = orderRepo.count(example);
		orderStatis.setUnprocessed(unprocessed);

		probe.setStatus(CONFIRMED);
		example = Example.of(probe);
		long confirmed = orderRepo.count(example);
		orderStatis.setConfirmed(confirmed);

		probe.setStatus(DISPATCHED);
		example = Example.of(probe);
		long dispatched = orderRepo.count(example);
		orderStatis.setDispatched(dispatched);

		probe.setStatus(FINISHED);
		example = Example.of(probe);
		long finished = orderRepo.count(example);
		orderStatis.setFinished(finished);
		
		probe.setStatus(CLOSED);
		example = Example.of(probe);
		long closed = orderRepo.count(example);
		orderStatis.setClosed(closed);
		try {
			opsForValue.set(ORDER_STATISTICS_NAME, mapper.writeValueAsString(orderStatis));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return orderStatis;
	}

}
