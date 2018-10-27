package com.storage.service.imp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.storage.entity.Category;
import com.storage.entity.Product;
import com.storage.entity.ProductDesc;
import com.storage.entity.Productimg;
import com.storage.entity.SellingRecord;
import com.storage.entity.Setting;
import com.storage.entity.custom.CustomOrder;
import com.storage.entity.custom.CustomProduct;
import com.storage.entity.custom.CustomeProductName;
import com.storage.entity.custom.OrderWrap;
import com.storage.entity.custom.PageBean;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.CategoryRepo;
import com.storage.repository.ProductDescRepo;
import com.storage.repository.ProductImgRepo;
import com.storage.repository.ProductRepo;
import com.storage.repository.specification.ProductSpecification;
import com.storage.service.ProductService;
import com.storage.service.SellingRecordService;
import com.storage.service.SettingService;
import com.storage.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@PropertySource("classpath:myapp.properties")
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class ProductServiceImp implements ProductService {
	private static final Integer PRODUCT_DELETE = 1;
	private static final Integer PRODUCT_NORNAL = 2;
	
	private static final Integer POUND = 1;
	private static final Integer RMB = 2;
	private static final String RMB_SYMBOL="¥";
	private static final String POUND_SYMBOL="£";
	
	
	@Autowired
	ProductRepo productRepo;
	@Autowired
	ProductImgRepo productimgRepo;
	
	@Autowired
	SettingService settingService;
	@Autowired
	SellingRecordService SellingRecordService;
	@Autowired
	CategoryRepo categoryRepo;
	@Value("${seaweeddfs.url}")
	String seaweeddfsUrl;

	@Autowired
	ProductDescRepo productContentRepo;
	/* @Value("${pageSize}") */
	Integer pageSize = 24;
	// @Value("${product_warning_quantity}")
	Integer product_warning_quantity = 10;
	NumberFormat format;
	@Autowired
	EntityManager manager;

	/*
	 * @Autowired JedisClientPool jedisPool;
	 */
	@Resource(name = "redisTemplate")
	ValueOperations<String, String> opsForValue;

	@Autowired
	StringRedisTemplate redisTemplate;

	ObjectMapper mapper = new ObjectMapper();

	public ProductServiceImp() {

		format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
	}

	@Override
	public StorageResult addProduct(Product product) {
		if (product == null)
			return StorageResult.failed("invalid parameter");
		Integer id = product.getId();
		Product one = productRepo.getOne(id);
		one.setStatus(PRODUCT_NORNAL);
		
		this.productRepo.save(one);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult updateProduct(Product product) {
	/*	if (product == null || product.getId() == null || product.getId() < 0)
			return StorageResult.failed("invalid parameter ");

		this.productRepo.save(product);
*/
		return StorageResult.failed();
	}

	@Override
	public StorageResult deleteProductById(Integer id) {
		if (id == null || id < 0)
			return StorageResult.failed("invalid id");
		Product one = productRepo.getOne(id);
		one.setStatus(PRODUCT_DELETE);
		one.setBarcode(one.getBarcode()+"_delete");
		this.productRepo.save(one);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult getProductByExample(Product product) {
		return this.getProductByExample(product, 0, this.pageSize);

	}

	@Override
	public StorageResult<CustomProduct> getProductById(Integer id) {
		if (id == null || id < 0)
			return StorageResult.failed("invalid id");

		CustomProduct customProduct = new CustomProduct();
		Optional<Product> pro = this.productRepo.findById(id);
		if (!pro.isPresent()) {
			return StorageResult.failed("no such product");
		}
		Product product = pro.get();
		manager.detach(product);
		
		product.setSellingprice_(product.getSellingprice());
		product.setBuyingprice_(product.getBuyingprice());
		Setting setting = settingService.getSetting().getResult();
		
		if(setting.getCurrencyDisplay()==1) {
			product.setMoneyDisplayed("£"+product.getSellingprice_aftertax().toPlainString());			
		}else {			
			product.setMoneyDisplayed("¥"+(product.getSellingprice_aftertax().multiply(new BigDecimal(setting.getCurrencyRate()))).toPlainString());			
			product.setSellingprice_aftertax(product.getSellingprice_aftertax().multiply(new BigDecimal(setting.getCurrencyRate())));
		}
		
		
		
		customProduct.setProduct(product);
		Productimg probe = new Productimg();
		probe.setProdcutid(product.getId());
		Example<Productimg> of = Example.of(probe);
		List<Productimg> selectByExample = this.productimgRepo.findAll(of);
		for (Productimg productimg : selectByExample) {
			if (!productimg.getUrl().startsWith("http:"))
				productimg.setUrl(seaweeddfsUrl + ":8080/" + productimg.getUrl());
		}
		ProductDesc desc = new ProductDesc();
		desc.setProduct_id(product.getId());
		Example<ProductDesc> of2 = Example.of(desc);

		List<ProductDesc> findAll = productContentRepo.findAll(of2);
		if (!findAll.isEmpty()) {
			ProductDesc productDesc = findAll.get(0);
			product.setContent(productDesc.getItemDesc());
		}
		customProduct.setImgs(selectByExample);
		return StorageResult.succeed(customProduct);
	}

	@Override
	public StorageResult updateProductSelective(Product product) {
		if (product == null || product.getId() == null || product.getId() < 0)
			return StorageResult.failed("invalid parameter ");

		this.productRepo.save(product);
		return StorageResult.succeed();

	}

	@Override
	public StorageResult count(Product example) {
		Example<Product> ex = Example.of(example);
		long count = this.productRepo.count(ex);
		return StorageResult.succeed(count);
	}

	@Override

	public StorageResult addProduct(CustomProduct cusproduct) {
		if (cusproduct == null || cusproduct.getProduct() == null || cusproduct.getImgs() == null)
			return StorageResult.failed("empty products or imgs");
		Product product = cusproduct.getProduct();
		String barcode = cusproduct.getProduct().getBarcode().trim();
		if (StringUtils.isEmpty(barcode))
			return StorageResult.failed("empty barcode");

		if (StringUtils.isEmpty(product.getName()))
			return StorageResult.failed("product name is empty");

		if (product.getBuyingprice_() == null)
			return StorageResult.failed("buying price is empty");
		else {
			product.setBuyingprice(product.getBuyingprice_());
		}
		if (product.getVat() == null) {
			product.setVat(0.0f);
		}
		if (product.getSellingprice_() == null)
			return StorageResult.failed("selling price is empty");
		else {
			Float vat = cusproduct.getProduct().getVat();
			BigDecimal divide=new  BigDecimal(vat).divide(new BigDecimal(100));
			BigDecimal multiply = product.getSellingprice_().multiply(new BigDecimal(1+divide.floatValue()));			
			product.setSellingprice_aftertax(multiply);
			product.setSellingprice(product.getSellingprice_());
		}
		if (product.getQuantity_() == null) {
			product.setQuantity(0);
		} else {
			Float quantity_ = product.getQuantity_();
			product.setQuantity(quantity_.intValue());
		}

		

		Product product2 = new Product();
		product2.setBarcode(barcode);

		List<Product> selectByExample = this.productRepo.findAll(Example.of(product2));
		if (selectByExample != null && selectByExample.size() > 0)
			return StorageResult.failed("barcode: " + barcode + " already exists");

		product.setCreatedtime(new Date());
		product.setUpdatetime(new Date());
		product.setStatus(PRODUCT_NORNAL);
		Product save = this.productRepo.save(product);
		Integer id = save.getId();

		// save img
		List<Productimg> list = cusproduct.getImgs();
		for (Productimg productimg : list) {
			Optional<Productimg> one = productimgRepo.findById(productimg.getId());
			if (!one.isPresent())
				StorageResult.failed("no img present");
			Productimg productimg2 = one.get();
			productimg2.setProdcutid(id);
			productimgRepo.save(productimg2);

		}
		// save content

		ProductDesc desc = new ProductDesc();
		desc.setItemDesc(cusproduct.getProduct().getContent());
		desc.setProduct_id(id);
		productContentRepo.save(desc);

		return StorageResult.succeed(product);
	}

	@Override
	public StorageResult getProductByExample(Product product, Integer currentPage, Integer pageSize) {

		if (pageSize == null) {
			pageSize = this.pageSize;
		}

		Specification<Product> and = Specification.where(ProductSpecification.idEqual(product.getId()))
				
				.and(ProductSpecification.categoryEqual(product.getCategory()))
				.and(ProductSpecification.createdtimeEqual(product.getCreatedtime()))
				.and(ProductSpecification.nameLike(product.getName()))
				.and(ProductSpecification.quantityEqual(product.getQuantity()))
				.and(ProductSpecification.supplierEqual(product.getSupplier()))
				.and(ProductSpecification.updatetimeEqual(product.getUpdatetime()))
				.and(ProductSpecification.vatEqual(product.getId()))
				.and(ProductSpecification.statueEqual(PRODUCT_NORNAL));
	
		long count = productRepo.count(and);

		and.and(ProductSpecification.orderByUpdateTime());
		PageBean<Product> bean = new PageBean<>(currentPage, (int) count, pageSize);
	
		pageSize = bean.getPageSize();
		String sort2 = product.getSort();
		if(com.storage.utils.StringUtils.isEmpty(sort2))
			sort2="updatetime";
		String sort_order = product.getSort_order();
		
		if(com.storage.utils.StringUtils.isEmpty(sort_order))
			sort_order="desc";
		org.springframework.data.domain.Sort.Order order;
			if(sort_order.equals("desc")) {
				order= org.springframework.data.domain.Sort.Order.desc(sort2);				
			}else {
				order= org.springframework.data.domain.Sort.Order.asc(sort2);								
			}
		Sort sort=Sort.by(order);
		PageRequest of = PageRequest.of(currentPage, pageSize,sort);
		Page<Product> findAll = productRepo.findAll(and,of );
		List<Product> results = findAll.getContent();

		if (results.size() == 0 && !com.storage.utils.StringUtils.isEmpty(product.getName())) {
			Product example = new Product();
			example.setBarcode(product.getName());
			example.setStatus(PRODUCT_NORNAL);

			results = this.productRepo.findAll(Example.of(example));
		}
		Iterator<Product> iterator = results.iterator();
		StorageResult<Setting> settingresult = settingService.getSetting();
		if(!settingresult.isSuccess())
		{
			log.error("cannot find setting ");
			return StorageResult.failed("cannot find setting");
		}
		Setting setting = settingresult.getResult();
		Float currencyRate = setting.getCurrencyRate();
		manager.clear();
		while (iterator.hasNext()) {
			Product product2 = iterator.next();
			product2.setProduct_warning_quantity(this.product_warning_quantity);
			Productimg example2 = new Productimg();
			if(setting.getCurrencyDisplay()==RMB) {
				product2.setSellingprice_aftertax(product2.getSellingprice_aftertax().multiply(new BigDecimal(currencyRate)));				
				product2.setMoneyDisplayed("¥"+product2.getSellingprice_aftertax());
			}else {				
				product2.setMoneyDisplayed("£"+product2.getSellingprice_aftertax());
			}
			
			example2.setProdcutid(product2.getId());
			List<Productimg> selectByExample = this.productimgRepo.findAll(Example.of(example2));
			if (selectByExample.size() > 0) {
				if (!selectByExample.get(0).getUrl().startsWith("http")) {
					product2.setImgUrl(seaweeddfsUrl + ":8080/" + selectByExample.get(0).getUrl());
				} else {
					product2.setImgUrl(selectByExample.get(0).getUrl());
				}
			}
		}

		bean.setBeans(results);

		return StorageResult.succeed(bean);

	}

	@Override
	public StorageResult getProductNamesByCategory(Integer categoryId) {
		String string = opsForValue.get("productNameByCategory" + categoryId);
		if (string != null) {
			List<CustomeProductName> jsonToList = JsonUtils.jsonToList(string, CustomeProductName.class);
			return StorageResult.succeed(jsonToList);
		}

		int myCategoryId = -1;
		if (categoryId != null) {
			Optional<Category> selectByPrimaryKey = this.categoryRepo.findById(categoryId);
			if (selectByPrimaryKey.isPresent()) {
				myCategoryId = selectByPrimaryKey.get().getId();
			}
		}
		List<com.storage.entity.custom.CustomeProductName> selectNameByExample;
		if (myCategoryId == -1) {
			selectNameByExample = this.productRepo.findAllProductName();
		} else {
			selectNameByExample = this.productRepo.findCustomerNameByCategory(myCategoryId);
		}

		try {
			String writeValueAsString = mapper.writeValueAsString(selectNameByExample);

			opsForValue.set("productNameByCategory" + categoryId, writeValueAsString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return StorageResult.succeed(selectNameByExample);
	}

	@Override
	public StorageResult<OrderWrap> getProductByExample(List<CustomOrder> jsonToList) {
		List<CustomProduct> list = new ArrayList<>();
		OrderWrap orderWrap = new OrderWrap();
		BigDecimal totalPrice=new BigDecimal(0);
		Setting setting = settingService.getSetting().getResult();
		Float currencyRate = setting.getCurrencyRate();
		
		if (jsonToList == null)
			return StorageResult.failed("Repeat submission");
		for (CustomOrder customOrder : jsonToList) {
			Integer id = customOrder.getId();
			if (id != null && id > 0) {
				Optional<Product> product = this.productRepo.findById(id);
				if (!product.isPresent())
					return StorageResult.failed("error: product not found");
				CustomProduct customProduct = new CustomProduct();
				customProduct.setProduct(product.get());
				customProduct.setQty(customOrder.getQty());
				BigDecimal sellingprice = product.get().getSellingprice_aftertax();
				BigDecimal subtotal =sellingprice.multiply(new BigDecimal(customOrder.getQty()));
				totalPrice=totalPrice.add(subtotal);
				
				customProduct.setSubtotal(subtotal);
				if(setting.getCurrencyDisplay()==1) {
					customProduct.setSubtotal(subtotal);
					customProduct.setSubtotalDisplayed("£"+totalPrice);
				}else {
					customProduct.setSubtotal(subtotal.multiply(new BigDecimal(currencyRate)));
					customProduct.setSubtotalDisplayed("¥"+customProduct.getSubtotal().toPlainString());
				}
				
				list.add(customProduct);
			}
		}
		orderWrap.setList(list);
		if(setting.getCurrencyDisplay()==1) {
			orderWrap.setTotalPrice(totalPrice);
			orderWrap.setTotalPriceDisplay("£"+totalPrice);
		}else {
			orderWrap.setTotalPrice(totalPrice.multiply(new BigDecimal(currencyRate)));
			orderWrap.setTotalPriceDisplay("¥"+orderWrap.getTotalPrice().toPlainString());
		}
		
		
		return StorageResult.succeed(orderWrap);
	}

	@Override
	public StorageResult getProductByBarCode(String barcode) {

		Product pro = new Product();
		pro.setBarcode(barcode);
		Example<Product> of = Example.of(pro);

		List<Product> selectByExample = this.productRepo.findAll(of);
		if (selectByExample.size() == 0)
			return StorageResult.failed("didnot find the item");
	
		StorageResult<Setting> settingresult = settingService.getSetting();
		
		if(!settingresult.isSuccess())
		{
			log.error("cannot find setting ");
			return StorageResult.failed("cannot find setting");
		}
		Float currencyRate = settingresult.getResult().getCurrencyRate();
		Product product2 = selectByExample.get(0);
		manager.detach(product2);
		
		Integer currencyDisplay = settingresult.getResult().getCurrencyDisplay();
		if(currencyDisplay==POUND) {
			
			product2.setMoneyDisplayed(POUND_SYMBOL+product2.getSellingprice_aftertax());
		}else {
			product2.setSellingprice_aftertax(product2.getSellingprice_aftertax().multiply(new BigDecimal(currencyRate)));
			product2.setMoneyDisplayed(RMB_SYMBOL+product2.getSellingprice_aftertax());

		}
		return StorageResult.succeed(product2);
	}

	@Override
	public StorageResult getStockReminder() {

		// criteria.andQuantityLessThanOrEqualTo(this.product_warning_quantity);

		List<Product> selectByExample = this.productRepo.findStockReminder(product_warning_quantity);

		return StorageResult.succeed(selectByExample);
	}

	@Override
	public StorageResult updateProduct(CustomProduct cusproduct) {

		if (cusproduct == null || cusproduct.getProduct() == null)
			return StorageResult.failed("product required");
		Product product = cusproduct.getProduct();
		if (product.getVat() == null) {
			product.setVat(0.0f);
		}
		Float vat = cusproduct.getProduct().getVat();
		BigDecimal bigDecimal=new BigDecimal(vat);
		BigDecimal divide = bigDecimal.divide(new BigDecimal("100"));
		float floatValue = divide.floatValue();			
		product.setSellingprice(product.getSellingprice_());
		BigDecimal multiply = product.getSellingprice_().multiply(new BigDecimal(1+floatValue));		
		product.setSellingprice_aftertax(multiply);
		
		
		product.setBuyingprice(product.getBuyingprice_());

		if (product.getQuantity_() == null) {
			product.setQuantity(0);
		} else {
			Float quantity_ = product.getQuantity_();
			product.setQuantity(quantity_.intValue());
		}

		

		product.setUpdatetime(new Date());
		Product one = productRepo.getOne(product.getId());
		String barcode = one.getBarcode();
		Date createdtime = one.getCreatedtime();
		product.setBarcode(barcode);
		product.setCreatedtime(createdtime);
		product.setStatus(one.getStatus());

		this.productRepo.save(product);
		ProductDesc probe = new ProductDesc();
		probe.setProduct_id(product.getId());
		List<ProductDesc> findAll2 = productContentRepo.findAll(Example.of(probe));
		if (!findAll2.isEmpty()) {
			ProductDesc productDesc = findAll2.get(0);
			productDesc.setItemDesc(product.getContent());
			productContentRepo.save(productDesc);
		}
		List<Productimg> list = cusproduct.getImgs();
		if (list != null && !list.isEmpty()) {
			Productimg productimg = new Productimg();
			productimg.setProdcutid(product.getId());
			Example<Productimg> example = Example.of(productimg);
			List<Productimg> findAll = productimgRepo.findAll(example);
			this.productimgRepo.deleteAll(findAll);
			for (Productimg productimg2 : list) {
				Optional<Productimg> findById = productimgRepo.findById(productimg2.getId());
				if (findById.isPresent()) {
					Productimg productimg3 = findById.get();
					productimg3.setProdcutid(product.getId());
					this.productimgRepo.save(productimg3);
				}
			}
		}
		return StorageResult.succeed(product);
	}

	@HystrixCommand(fallbackMethod="getBestSellingProductByCategory_fallBack")
	@Override
	public List<Product> getBestSellingProductByCategory(Integer categoryId) {
		List<Product> list=new ArrayList<>();
		
		//get desired products according to the categoryid
		List<SellingRecord> all = SellingRecordService.getAll();
		for (SellingRecord sellingRecord : all) {
			Product product = sellingRecord.getProduct();
			if (product.getCategory()==categoryId && product.getStatus()==PRODUCT_NORNAL) {
				list.add(product);
			if(list.size()>=10)
				break;
			}
			
		}
		//get best selling products that is not in the the productId
		if(list.size()<10) {
			for (SellingRecord sellingRecord : all) {
				Product product = sellingRecord.getProduct();
				if (product.getCategory()!=categoryId  && product.getStatus()==PRODUCT_NORNAL) {
					list.add(product);
				if(list.size()>=10)
					break;
				}				
			}			
		}
		//fill the list with new added products
		if(list.size()<10) {
			Product probe=new Product();
			probe.setCategory(categoryId);
			probe.setStatus(PRODUCT_NORNAL);
			Sort ascending = Sort.by("createdtime").descending();
			
			List<Product> findAll = productRepo.findAll(Example.of(probe),ascending);
			for (Product product : findAll) {
				list.add(product);
				if(list.size()>=10)
					break;
				}				
			}
		
		return list;
	}
	
	public List<Product> getBestSellingProductByCategory_fallBack(Integer categoryId,Throwable throwable) {
		throwable.printStackTrace();
		return new ArrayList<Product>();
	}
	
}
