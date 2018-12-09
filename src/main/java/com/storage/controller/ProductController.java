package com.storage.controller;

import com.storage.entity.Category;
import com.storage.entity.Product;
import com.storage.entity.ProductDetail;
import com.storage.entity.Vat;
import com.storage.entity.custom.CustomOrder;
import com.storage.entity.custom.CustomProduct;
import com.storage.entity.custom.PageBean;
import com.storage.entity.custom.StorageResult;
import com.storage.service.CategoryService;
import com.storage.service.ProductService;
import com.storage.service.VatService;
import com.storage.utils.JsonUtils;
import com.storage.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/product")
@PropertySource("classpath:myapp.properties")
public class ProductController  {
	@Autowired
	CategoryService categoryService;
	@Autowired
	ProductService service;
	@Autowired
	VatService vatService;

	
	
	
	@RequestMapping("/add")
	public Object addProductWithImg(@RequestBody CustomProduct product) {
		
		return this.service.addProduct(product);
	}






	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#getProduct(org.springframework.web.servlet.ModelAndView, java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getProduct(@PathVariable(name = "id") Integer id) {
			StorageResult<CustomProduct> productById = this.service.getProductById(id);
			CustomProduct result =productById.getResult();
			if(result==null)
				return null;
			ProductDetail model=new ProductDetail();
			model.setProduct(result.getProduct());
			model.setImgs(result.getImgs());
			Integer categoryId = result.getProduct().getCategory();
			StorageResult<Category> categoryById = categoryService.getCategoryById(categoryId);
			StorageResult<List<Vat>> vatByExample = vatService.getVatByExample(new Vat());
			
			model.setCategory(categoryById.getResult());
			model.setVats(vatByExample.getResult());
			return model;
	}
	@PostMapping("/getListByIds")
	public Object getProducts(@RequestBody  List<Integer> ids) {
		List<CustomProduct> productById = this.service.getProductByIds(ids);

		return productById;
	}


	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#getProductByBarcode(java.lang.String, java.lang.Integer)
	 */
	
	@RequestMapping("/barcode/get")

	public Object getProductByBarcode(@RequestParam(required=false,name="barcode",value="barcode") String barcode,
			@RequestParam(required=false,name="id",value="id") Integer id) {

		if(StringUtils.isEmpty(barcode) && (id==null|| id<0))
			return StorageResult.failed("barcode or product id is required");
		StorageResult productById;
		if(!StringUtils.isEmpty(barcode)) {
			productById = this.service.getProductByBarCode(barcode);
		}else {
			productById=this.service.getProductById(id);
		}
		if(productById.getResult()==null)
			return StorageResult.failed("didnot find the item");
		else
			return productById;

	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#getProduct(com.storage.entity.Product, java.lang.Integer, java.lang.Integer)
	 */
	
	@RequestMapping("/list")
	public Object getProduct(@RequestBody(required=false) Product product, @RequestParam(value="currentPage",required=false)Integer currentPage,
							 @RequestParam(value="pageSize",required=false) Integer pageSize
			,@RequestParam(value="offerConfirmed",required=false)Integer offerConfirmed
			) {



		StorageResult<PageBean<Product>> productByExample = this.service.getProductByExample(product,currentPage,pageSize
				, offerConfirmed);
		return productByExample;
	}

	

	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#deleteProductById(java.lang.Integer)
	 */
	
	@DeleteMapping("/delete/{id}")

	public Object deleteProductById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteProductById(id);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#getStockReminder()
	 */
	
	@GetMapping("/stockreminder")

	public Object getStockReminder() {
		StorageResult stockReminder = this.service.getStockReminder();
		
	
		return stockReminder;
	}




	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#updateProduct(com.storage.entity.custom.CustomProduct, org.springframework.web.multipart.MultipartFile)
	 */
	
	@PostMapping("/update")

	public Object updateProduct(@RequestBody CustomProduct product) {	
			
		
		return this.service.updateProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#getProductNamesByCategory(java.lang.Integer)
	 */
	
	@RequestMapping("/getName/{categoryId}.json")

	public Object getProductNamesByCategory(@PathVariable(name="categoryId") Integer categoryId) {
		Object result = this.service.getProductNamesByCategory(categoryId).getResult();
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IProductController#count(com.storage.entity.Product)
	 */
	
	@GetMapping("/count")

	public Object count(@RequestBody Product product) {
		
		return this.service.count(product);
	}



	
	@RequestMapping("/getProductByExample")
	public Object getProductByExample(@RequestBody String jsonToList) {
		List<CustomOrder> jsonToList2 = JsonUtils.jsonToList(jsonToList,CustomOrder.class);
		
		return service.getProductByExample(jsonToList2);
	}

	@RequestMapping("/getbestsellingproduct")	
	public List<Product> getBestSellingProduct(@RequestParam("category") Integer categoryId){
		
		return service.getBestSellingProductByCategory(categoryId);
		
	}


}
