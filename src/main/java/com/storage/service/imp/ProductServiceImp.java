package com.storage.service.imp;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import com.storage.entity.Category;
import com.storage.entity.Product;
import com.storage.entity.ProductDesc;
import com.storage.entity.Productimg;
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
	@Autowired
	ProductRepo productRepo;
	@Autowired
	ProductImgRepo productimgRepo;
	
	@Autowired
	SettingService settingService;
	
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
		if (product == null || product.getId() == null || product.getId() < 0)
			return StorageResult.failed("invalid parameter ");

		this.productRepo.save(product);

		return StorageResult.succeed();
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
		BigDecimal valueOf = BigDecimal.valueOf(100);

		BigDecimal sell = BigDecimal.valueOf(product.getSellingprice()).divide(valueOf);

		BigDecimal buy = BigDecimal.valueOf(product.getBuyingprice()).divide(valueOf);
		product.setSellingprice_(sell.floatValue());
		product.setBuyingprice_(buy.floatValue());
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
			product.setBuyingprice((int) (product.getBuyingprice_() * 100));
		}
		if (product.getVat() == null) {
			product.setVat(0.0f);
		}
		if (product.getSellingprice_() == null)
			return StorageResult.failed("selling price is empty");
		else {
			Float vat = cusproduct.getProduct().getVat();
			BigDecimal bigDecimal=new BigDecimal(vat);
			BigDecimal divide = bigDecimal.divide(new BigDecimal("100"));
			float floatValue = divide.floatValue();			
			int finalPrice= (int)((product.getSellingprice_() * 100)*(1+floatValue));
			product.setSellingprice(finalPrice);
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
				.and(ProductSpecification.buyingPriceEqual(product.getBuyingprice()))

				.and(ProductSpecification.categoryEqual(product.getCategory()))
				.and(ProductSpecification.createdtimeEqual(product.getCreatedtime()))
				.and(ProductSpecification.nameLike(product.getName()))
				.and(ProductSpecification.quantityEqual(product.getQuantity()))
				.and(ProductSpecification.sellingpriceEqual(product.getSellingprice()))
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
			example.setStatus(1);
			Example.of(example);
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
			product2.setSellingprice((int)(currencyRate*product2.getSellingprice()));
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
	public StorageResult getProductByExample(List<CustomOrder> jsonToList) {
		List<CustomProduct> list = new ArrayList<>();
		OrderWrap orderWrap = new OrderWrap();
		double totalPrice = 0;
		NumberFormat numberInstance = new DecimalFormat("0.00");

		numberInstance.setMaximumFractionDigits(2);
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
				Integer sellingprice = product.get().getSellingprice();
				double subtotal = (double) (sellingprice * customOrder.getQty()) / 100;
				Double valueOf = Double.valueOf(numberInstance.format(subtotal));
				totalPrice += valueOf;
				customProduct.setSubtotal(valueOf);
				list.add(customProduct);
			}
		}
		orderWrap.setList(list);
		orderWrap.setTotalPrice(Double.valueOf(numberInstance.format(totalPrice)));
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
		
		product2.setSellingprice((int)(product2.getSellingprice()*currencyRate));
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
		
		Float vat = cusproduct.getProduct().getVat();
		BigDecimal bigDecimal=new BigDecimal(vat);
		BigDecimal divide = bigDecimal.divide(new BigDecimal("100"));
		float floatValue = divide.floatValue();			
		int finalPrice= (int)((product.getSellingprice_() * 100)*(1+floatValue));
		product.setSellingprice(finalPrice);
		
		
		product.setBuyingprice((int) (product.getBuyingprice_() * 100));

		if (product.getQuantity_() == null) {
			product.setQuantity(0);
		} else {
			Float quantity_ = product.getQuantity_();
			product.setQuantity(quantity_.intValue());
		}

		if (product.getVat() == null) {
			product.setVat(0.0f);
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

}
