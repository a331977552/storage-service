package com.storage.service.imp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.storage.entity.Customer;
import com.storage.entity.OrderShipping;
import com.storage.entity.Orderitem;
import com.storage.entity.Product;
import com.storage.entity.SellingRecord;
import com.storage.entity.Setting;
import com.storage.entity.StOrder;
import com.storage.entity.custom.CustomProduct;
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

		StOrder record = new StOrder();
		record.setCustomerid(customerId);
		record.setBuyername(customer.getName());
		record.setCreatedtime(new Date());
		long closedTime = System.currentTimeMillis() + 1000 * 60 * 60;
		record.setClosedtime(new Date(closedTime));
		Double d = result.getTotalPrice() * 100;
		int intValue = d.intValue();
		// 1 need to be confirmed 3 canceled
		record.setStatus(1);
		record.setTotalprice(intValue);
		record.setUpdatetime(new Date());
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

		StOrder save = this.orderRepo.save(record);

		Integer orderId = save.getId();
		List<CustomProduct> list = result.getList();
		
		for (CustomProduct customProduct : list) {
			
			//creating order item
			Orderitem item = new Orderitem();
			item.setOrderid(orderId);
			item.setProductName(customProduct.getProduct().getName());
			item.setProductid(customProduct.getProduct().getId());
			item.setQuantity(customProduct.getQty());
			item.setTotalprice(((Double) (customProduct.getSubtotal() * 100)).intValue());
			item.setUnitprice(customProduct.getProduct().getSellingprice());
			orderItemRepo.save(item);
			
			//alter quantity of the product
			Product one = productRepo.getOne(customProduct.getProduct().getId());
			if (one.getQuantity() - customProduct.getQty() < 0) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				result.setCode(500);
				result.setMsg("shortage of products: " + one.getName());
				return result;
			} else {
				one.setQuantity(one.getQuantity() - customProduct.getQty());
			}
			Product save2 = productRepo.save(one);
			
			//adding record
			Optional<SellingRecord> sellingRecodByProductId = sellingRecordService.getSellingRecodByProductId(save2.getId());
			SellingRecord sellingRecord;
			if(sellingRecodByProductId.isPresent()) {
				 sellingRecord = sellingRecodByProductId.get();
				sellingRecord.setSellingAmount(sellingRecord.getSellingAmount()+customProduct.getQty());
			}else {
				sellingRecord=new SellingRecord();
				sellingRecord.setProduct(save2);
				sellingRecord.setSellingAmount(customProduct.getQty());
				sellingRecord.setReturnedAmount(0);
			}
			sellingRecordService.addOrUpdateSellingRecord(sellingRecord);
		}
	
		
		OrderShipping entity = new OrderShipping();
		String address = customer.getAddress();
		String postcode = customer.getPostcode();
		String phone = customer.getPhone();
		entity.setAddress(address);
		entity.setPostCode(postcode);
		entity.setReceiverPhone(phone);
		entity.setOrderId(orderId);
		entity.setCreatedTime(new Date());
		entity.setUpdatedTime(new Date());
		entity.setReceiverName(customer.getName());
		entity.setStaus("ongoing");
		entity.setEmail(customer.getEmail());
		entity.setPhone(customer.getPhone());
		orderShippingRepo.save(entity);
		result.setCode(200);
		result.setMsg("success");
		result.setOrder(save);
		result.setDate(new Date());

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
			item.setStatus(order.getStatus());
			item.setName(order.getBuyername());
			if(findAll2.isPresent())
			item.setPhone(findAll2.get().getPhone());

		
			items.add(item);
			
			
			
		}


		return StorageResult.succeed(items);
	}

	public StorageResult<List<OrderTableItem>> findAllTableItems_error(Throwable e) {
		e.printStackTrace();
		return StorageResult.failed("unknow error when getting order list");
	}

	@Override
	public StorageResult updateStOrder(OrderTableItem stOrder) {
		if (stOrder == null || stOrder.getId() == null || stOrder.getId() < 0)
			return StorageResult.failed("invalid parameter ");
		StOrder one = orderRepo.getOne(stOrder.getId());
		one.setStatus(stOrder.getStatus());
		one.setUpdatetime(new Date());
		StOrder save = this.orderRepo.save(one);
		return StorageResult.succeed();
	}



	@HystrixCommand(fallbackMethod="getInfoFromOrder_error")
	@Override
	public OrderWrap getInfoFromOrder(Integer orderId) {
		Optional<StOrder> opOrder = orderRepo.findById(orderId);
		OrderWrap  orderWrap=new OrderWrap();
			if(!opOrder.isPresent()) {
				orderWrap.setCode(500);
				orderWrap.setMsg("no order detail present");
				return orderWrap;

			}
			StOrder order=opOrder.get();
		
		OrderShipping shipping=new OrderShipping();
		shipping.setOrderId(orderId);
		Optional<OrderShipping> findOne = orderShippingRepo.findOne(Example.of(shipping));
		if(!findOne.isPresent())
		{
			orderWrap.setCode(500);
			orderWrap.setMsg("no shipping detail present");
			return orderWrap;
		}
			OrderShipping orderShipping = findOne.get();
			Customer customer=new Customer();
			customer.setAddress(orderShipping.getAddress());
			customer.setName(order.getBuyername());
			customer.setPhone(orderShipping.getPhone());
			customer.setPostcode(orderShipping.getPostCode());
			
		List<CustomProduct> list=new ArrayList<>();
		Orderitem example=new Orderitem();
		example.setOrderid(orderId);
		List<Orderitem> findAll = orderItemRepo.findAll(Example.of(example));
		
		for (Orderitem orderitem : findAll) {
			Integer productid = orderitem.getProductid();
			Optional<Product> findById = productRepo.findById(productid);
			CustomProduct e=new CustomProduct();
			if(!findById.isPresent()) {
				Product product=new Product();
				product.setId(null);
				product.setName(orderitem.getProductName()+"(deleted)");
				product.setTitle("");
				e.setProduct(product);				
			}else {
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
		return orderWrap;
	}
	public OrderWrap getInfoFromOrder_error(Integer orderid,Throwable e) {
		OrderWrap orderWrap=new OrderWrap();
		orderWrap.setCode(500);
		orderWrap.setMsg("unknow error");
		e.printStackTrace();
		return  orderWrap;
	}


	@HystrixCommand(fallbackMethod = "findAllTableItemsByUserId_error")
	@Override
	public StorageResult<List<OrderTableItem>> findAllTableItemsByUserId(Integer Id) {
		List<OrderTableItem> items = new ArrayList<>();
		StOrder probe=new StOrder();
		probe.setCustomerid(Id);
		List<StOrder> findAll = orderRepo.findAll(Example.of(probe));
		for (StOrder order : findAll) {
			OrderShipping example = new OrderShipping();
			example.setOrderId(order.getId());			
			OrderTableItem item = new OrderTableItem();
			item.setId(order.getId());
			item.setOrderNumber(order.getOrdernumber());
			item.setCreatedTime(order.getCreatedtime());
			item.setPrice(order.getTotalprice());
			item.setStatus(order.getStatus());
			item.setName(order.getBuyername());
			items.add(item);		
		}


		return StorageResult.succeed(items);
	}
	public StorageResult<List<OrderTableItem>> findAllTableItemsByUserId_error(Integer Id,Throwable e) {
	
		e.printStackTrace();
		return StorageResult.failed("error to retrive all order info");
	}

}
