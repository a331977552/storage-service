package com.storage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Category;
import com.storage.entity.Product;
import com.storage.entity.ProductDetail;
import com.storage.entity.Vat;
import com.storage.entity.custom.CustomOrder;
import com.storage.entity.custom.CustomProduct;
import com.storage.entity.custom.StorageResult;
import com.storage.service.CategoryService;
import com.storage.service.ProductService;
import com.storage.service.VatService;
import com.storage.utils.JsonUtils;
import com.storage.utils.StringUtils;

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
			StorageResult productById = this.service.getProductById(id);
		
			CustomProduct result = (CustomProduct) productById.getResult();
			ProductDetail model=new ProductDetail();
			model.setProduct(result.getProduct());
			model.setImgs(result.getImgs());
			Category category=new Category();
			StorageResult<List<Category>> categoryByExample = this.categoryService.getCategoryByExample(category);
			StorageResult<List<Vat>> vatByExample = vatService.getVatByExample(new Vat());
			
			model.setCategories(categoryByExample.getResult());
			model.setVats(vatByExample.getResult());
			return model;
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

	public Object getProduct(@RequestBody Product product,Integer currentPage,Integer pageSize) {

		/*try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {121311260004
			e.printStackTrace();
		}*/
		product.setStatus(2);
		StorageResult productByExample = this.service.getProductByExample(product,currentPage,pageSize);
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

	/*	@ExceptionHandler(value=SizeLimitExceededException.class)
	@ResponseBody
	public StorageResult handleException(Exception exception) {
		System.out.println("handleException22:  "+ exception.getMessage());
		return StorageResult.failed("file is too large, size cannot exceed 5MB");

	}*/


}
