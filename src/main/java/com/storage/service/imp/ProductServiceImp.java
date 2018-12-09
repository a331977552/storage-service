package com.storage.service.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.storage.entity.*;
import com.storage.entity.custom.*;
import com.storage.exception.NotSettingException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

import static com.storage.entity.utils.Constants.REDIS_NAME_PRODUCT_NAMES_PREFIX;

@Service
@PropertySource("classpath:myapp.properties")
@Slf4j
@org.springframework.transaction.annotation.Transactional

public class ProductServiceImp implements ProductService {

	
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
	SellingRecordService sellingRecordService;
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
		redisTemplate.delete(REDIS_NAME_PRODUCT_NAMES_PREFIX+one.getCategory());
		redisTemplate.delete(REDIS_NAME_PRODUCT_NAMES_PREFIX+"-1");
		return StorageResult.succeed();
	}

	@Override
	public StorageResult<CustomProduct> getProductById(Integer id) {
		if (id == null || id < 0)
			return StorageResult.failed("invalid id");

		Optional<Product> pro = this.productRepo.findById(id);
		if (!pro.isPresent()) {
			return StorageResult.failed("no such product");
		}
		Product product = pro.get();


		convertDisplayedCurrency(product);


		List<Productimg> productImgs= getProductimgsById(product.getId());

		ProductDesc desc= findProductDescribeById(product.getId());
		product.setContent(desc.getItemDesc());
		CustomProduct customProduct = new CustomProduct();
		customProduct.setImgs(productImgs);
		customProduct.setProduct(product);
		return StorageResult.succeed(customProduct);
	}

	private ProductDesc findProductDescribeById(Integer id) {
		ProductDesc desc = new ProductDesc();
		desc.setProduct_id(id);
		Example<ProductDesc> of2 = Example.of(desc);
		Optional<ProductDesc> findAll = productContentRepo.findOne(of2);
			return findAll.orElse(null);
	}

	private List<Productimg> getProductimgsById(int id){
		Productimg probe = new Productimg();
		probe.setProdcutid(id);
		Example<Productimg> of = Example.of(probe);
		List<Productimg> selectByExample = this.productimgRepo.findAll(of);
		for (Productimg productimg : selectByExample) {
			if (!productimg.getUrl().startsWith("http"))
				productimg.setUrl(seaweeddfsUrl + ":8080/" + productimg.getUrl());
		}
		return selectByExample;
	}
	private  void convertDisplayedCurrency(Product product, Setting setting){
		manager.detach(product);
		product.setSellingprice_(product.getSellingprice());
		product.setBuyingprice_(product.getBuyingprice());
		if(setting==null)


		if(setting.getCurrencyDisplay()==1) {
			product.setMoneyDisplayed("£"+product.getSellingprice_aftertax().toPlainString());
		}else {
			product.setMoneyDisplayed("¥"+(product.getSellingprice_aftertax().multiply(new BigDecimal(setting.getCurrencyRate()))).toPlainString());
			product.setSellingprice_aftertax(product.getSellingprice_aftertax().multiply(new BigDecimal(setting.getCurrencyRate())));
		}
	}
	private  void convertDisplayedCurrency(Product product ){
		Setting	setting = settingService.getSetting().getResult();
		convertDisplayedCurrency(product,setting);
	}
	private void convertDisplayedCurrency(List<Product> products){
		Setting setting=	settingService.getSetting().getResult();
		for (Product product: products) {
			convertDisplayedCurrency(product,setting);
		}



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
		if(product.getCategory()==null)
			return StorageResult.failed("category is empty");
		
		
		
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
		if(product.getSellingprice_old_()!=null) {
			product.setSellingprice_old(product.getSellingprice_old_());
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
		redisTemplate.delete(REDIS_NAME_PRODUCT_NAMES_PREFIX+product.getCategory());
		redisTemplate.delete(REDIS_NAME_PRODUCT_NAMES_PREFIX+"-1");
		return StorageResult.succeed(product);
	}


	private void currencyProcess(Iterator<Product> iterator) {
		StorageResult<Setting> settingresult = settingService.getSetting();
		if(!settingresult.isSuccess())
		{
			log.error("cannot find setting ");
			throw new NotSettingException("cannot find setting "); 
		}
		Setting setting = settingresult.getResult();
		Float currencyRate = setting.getCurrencyRate();
	
		while (iterator.hasNext()) {
			Product product2 = iterator.next();
			product2.setProduct_warning_quantity(this.product_warning_quantity);
			Productimg example2 = new Productimg();
			if(setting.getCurrencyDisplay()==RMB) {
				product2.setSellingprice_aftertax(product2.getSellingprice_aftertax().multiply(new BigDecimal(currencyRate)));				
				product2.setMoneyDisplayed("¥"+product2.getSellingprice_aftertax());				
				if(product2.getSellingprice_old()!=null)
				product2.setOldMoneyDisplayed("¥"+product2.getSellingprice_old().multiply(new BigDecimal(currencyRate)));
			}else {				
				product2.setMoneyDisplayed("£"+product2.getSellingprice_aftertax());
				if(product2.getSellingprice_old()!=null)
				product2.setOldMoneyDisplayed("£"+product2.getSellingprice_old());
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
	}

	@Override
	public StorageResult getProductNamesByCategory(Integer categoryId) {
		String string = redisTemplate.opsForValue().get( REDIS_NAME_PRODUCT_NAMES_PREFIX+ categoryId);
		if (!com.storage.utils.StringUtils.isEmpty(string)) {
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

			redisTemplate.opsForValue().set(REDIS_NAME_PRODUCT_NAMES_PREFIX + categoryId, writeValueAsString);
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

		Optional<Product> selectByExample = this.productRepo.findOne(of);
		if (!selectByExample.isPresent() )
			return StorageResult.failed("didnot find the item");
	
		StorageResult<Setting> settingresult = settingService.getSetting();
		
		if(!settingresult.isSuccess())
		{
			log.error("cannot find setting ");
			return StorageResult.failed("cannot find setting");
		}
		Float currencyRate = settingresult.getResult().getCurrencyRate();
		Product product2 = selectByExample.get();
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
		Specification<Product> and = Specification.where(ProductSpecification.quantityLessThan(product_warning_quantity))
				.and(ProductSpecification.statueEqual(PRODUCT_NORNAL));
		PageRequest of = PageRequest.of(0, 100);

		Page<Product> all = productRepo.findAll(and, of);
		all.getTotalElements();
		PageBean<Product> productPageBean=new PageBean<>();
		productPageBean.setBeans(all.getContent());
		productPageBean.setTotalCount((int)all.getTotalElements());
		return StorageResult.succeed(productPageBean);
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
		
		BigDecimal sellingprice_old_ = cusproduct.getProduct().getSellingprice_old_();
		if(sellingprice_old_!=null && sellingprice_old_.floatValue()>0.01)
		{
			product.setSellingprice_old(sellingprice_old_);
		}else
		{
			product.setSellingprice_old(null);

		}
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
		Optional<ProductDesc> findOne = productContentRepo.findOne(Example.of(probe));
		
		if (findOne.isPresent()) {
			ProductDesc productDesc = findOne.get();
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

		redisTemplate.delete(REDIS_NAME_PRODUCT_NAMES_PREFIX+"-1");
		redisTemplate.delete(REDIS_NAME_PRODUCT_NAMES_PREFIX+product.getCategory());
		return StorageResult.succeed(product);
	}

	@HystrixCommand(fallbackMethod="getBestSellingProductByCategory_fallBack")
	@Override
	public List<Product> getBestSellingProductByCategory(Integer categoryId) {
		List<Product> list=new ArrayList<>();
		
		//get desired products according to the categoryid
		List<SellingRecord> all = sellingRecordService.getAll(0,10);
		for (SellingRecord sellingRecord : all) {
			Product product = sellingRecord.getProduct();
			if (product.getCategory()==categoryId && product.getStatus()==PRODUCT_NORNAL) {
				list.add(product);
			}
			
		}
		//get best selling products that is not in the the productId
		
		//fill the list with new added products
		if(list.size()<10) {
			Product probe=new Product();
			probe.setCategory(categoryId);
			probe.setStatus(PRODUCT_NORNAL);
			Sort ascending = Sort.by("createdtime").descending();
			PageRequest of = PageRequest.of(0, 10, ascending);
			
			Page<Product> findAll = productRepo.findAll(Example.of(probe),of);
			
			for (Product product2: findAll) {
				list.add(product2);
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

	@Override
	public StorageResult<PageBean<Product>> getProductByExample(Product product, Integer currentPage, Integer pageSize,
			 Integer offerConfirmed) {

		if (pageSize == null) {
			pageSize = this.pageSize;
		}
		if (currentPage == null)
			currentPage = 0;


		Specification<Product> and = Specification.where(ProductSpecification.idEqual(product.getId()))

				.and(ProductSpecification.categoryEqual(product.getCategory()))
				.and(ProductSpecification.createdtimeEqual(product.getCreatedtime()))
				.and(ProductSpecification.nameLike(product.getName()))
				.and(ProductSpecification.quantityEqual(product.getQuantity()))
				.and(ProductSpecification.supplierEqual(product.getSupplier()))
				.and(ProductSpecification.updatetimeEqual(product.getUpdatetime()))
				.and(ProductSpecification.vatEqual(product.getId()))
				.and(ProductSpecification.statueEqual(PRODUCT_NORNAL));

		if (offerConfirmed != null && offerConfirmed == 1) {
			and=	and.and(ProductSpecification.hasOffer());
		}
		long count = productRepo.count(and);

		PageBean<Product> bean = new PageBean<>(currentPage, (int) count, pageSize);
		String sort = product.getSort();

		pageSize = bean.getPageSize();
		if(sort==null)
			sort="";
		switch (sort) {
		case "price":
			sort = "sellingprice";
			break;
		case "time":
			sort = "updatetime";
			break;
		case "quantity":
			sort = "quantity";
			break;
		case "createdtime":
				sort = "createdtime";
				break;
		default:
			sort = "updatetime";
		}
		String sort_order = product.getSort_order();
		if(sort_order==null)
			sort_order="desc";
		if(!sort_order.equals("desc") && !sort_order.equals("asc")){
			sort_order="desc";
		}


		product.setSort_order(sort_order);


		String sort_order2 = product.getSort_order();

		org.springframework.data.domain.Sort.Order order;
		if (sort_order2.equals("desc")) {
			order = org.springframework.data.domain.Sort.Order.desc(sort);
		} else {
			order = org.springframework.data.domain.Sort.Order.asc(sort);
		}
		PageRequest of = PageRequest.of(currentPage, pageSize, Sort.by(order));
		Page<Product> findAll = productRepo.findAll(and, of);
		List<Product> results = findAll.getContent();

		if (results.size() == 0 && !com.storage.utils.StringUtils.isEmpty(product.getName())) {
			Product example = new Product();
			example.setBarcode(product.getName());
			example.setStatus(PRODUCT_NORNAL);

			results = this.productRepo.findAll(Example.of(example));
		}
		Iterator<Product> iterator = results.iterator();
		manager.clear();
		currencyProcess(iterator);
		bean.setBeans(results);

		return StorageResult.succeed(bean);

	
		
	}

	@Override
	public List<CustomProduct> getProductByIds(List<Integer> ids) {
		if (ids == null){
			log.warn("getProductByIds:   ids ==null ");
			return new ArrayList<>();
		}

		List<Product> products = this.productRepo.findAllById(ids);

		convertDisplayedCurrency(products);
		List<CustomProduct> customProducts=new ArrayList<>();
		for (Product product: products) {
			List<Productimg> productImgs= getProductimgsById(product.getId());
			//ProductDesc desc= findProductDescribeById(product.getId());
			//product.setContent(desc.getItemDesc());
			CustomProduct customProduct = new CustomProduct();
			customProduct.setImgs(productImgs);
			customProduct.setProduct(product);

			customProducts.add(customProduct);
		}

		return customProducts;
	}

}
