package com.storage.service.imp;
import com.storage.entity.Category;
import com.storage.entity.Product;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.CategoryRepo;
import com.storage.repository.ProductRepo;
import com.storage.service.CategoryService;
import com.storage.service.ProductService;
import com.storage.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
@org.springframework.transaction.annotation.Transactional
public class CategoryServiceImp implements  CategoryService{
	@Autowired
	CategoryRepo categoryRepo;
	@Autowired
	ProductRepo productRepo;
	
	@Resource(name="redisTemplate")
	ValueOperations<String, String> jedisPool;
	
	private  static final String REDIS_CACHE="category";
	@Override
	public StorageResult<Category> addCategory(Category category){
		if(category==null) {
			return StorageResult.failed("invalid parameter");
		}
		if(StringUtils.isEmpty(category.getText())) {
			return StorageResult.failed("name required!");
		}
		
		jedisPool.getOperations().expire(REDIS_CACHE, 0,TimeUnit.SECONDS);

		Category save = this.categoryRepo.save(category);
		return StorageResult.succeed(save);
	}
	@Override
	public StorageResult updateCategory(Category category ){
		if(category==null ||category.getId()==null ||category.getId()<0) {
			return StorageResult.failed("invalid parameter ");
		}
		if(StringUtils.isEmpty(category.getText())) {
			return StorageResult.failed("required!");
		}
		
		Category save = this.categoryRepo.save(category);
		
		jedisPool.getOperations().expire(REDIS_CACHE, 0,TimeUnit.SECONDS);

		return StorageResult.succeed(save);
	}
	@Override
	public StorageResult deleteCategoryById(Integer id){
		if(id==null || id<0){
			return StorageResult.failed("invalid id");
		}

		/*ProductExample example=new ProductExample();
		com.storage.entity.ProductExample.Criteria createCriteria = example.createCriteria();
		createCriteria.andCategoryEqualTo(id);*/

		Product product=new Product();
		product.setCategory(id);
		Example<Product> create =Example.of(product);
		

		
		
		List<Product> allProduct = this.productRepo.findAll(create);
		
		if(allProduct.size()!=0) {
			return StorageResult.failed("there are products under this category, you have to remove those product firstly");
		}
		categoryRepo.deleteById(id);
		
		jedisPool.getOperations().expire(REDIS_CACHE, 0,TimeUnit.SECONDS);

		return StorageResult.succeed();
	}
	@Override
	public StorageResult<List<Category>> getCategoryByExample(Category  category ){

		

		Example<Category> of = Example.of(category);
		
		List<Category> results = this.categoryRepo.findAll(of);
	
	
		return StorageResult.succeed(results);
	}
	@Override
	public StorageResult<Category> getCategoryById(Integer id){
		if(id==null || id<0){
			return StorageResult.failed("invalid id");
		}

		Optional<Category> category = this.categoryRepo.findById(id);
		boolean present = category.isPresent();
		if(present)
		return StorageResult.succeed(category.get());
		else {
			return StorageResult.failed("no such category");
		}
	}
	@Override
	public StorageResult<Category> updateCategorySelective(Category category){
		if(category==null ||category.getId()==null ||category.getId()<0) {
			return StorageResult.failed("invalid parameter ");
		}


		Category save = this.categoryRepo.save(category);
		
		jedisPool.getOperations().expire(REDIS_CACHE, 0,TimeUnit.SECONDS);

		return  StorageResult.succeed(save);

	}

	@Override
	public StorageResult<Long> count(){
		long count = this.categoryRepo.count();
		return  StorageResult.succeed(count);
	}
	@Override
	public List<Category> findAll() {
		return categoryRepo.findAll();
	}
	
	/**
	 * 
	 * delete all  that should be deleted,
	 * and update all existing, and
	 *  add all that are not existed before.
	 * 
	 */
	@Override
	public StorageResult updateCategories(List<Category> categoriesFromWeb) {

		List<Category> categoryExisting=getExistCategory(categoriesFromWeb);
		List<Category> AllCateogry = categoryRepo.findAll();
		List<Category> deletes=new ArrayList<>();
		for (Category category : AllCateogry) {
			boolean delete=true;
			inner :for (Category cat : categoryExisting) {
				if(cat.getId()==category.getId()) {
					delete=false;
					break inner;
				}
			}
			if(delete) {
				deletes.add(category);
			}			
		}
		

		for (Category category : deletes) {
			Product product=new Product();
			product.setCategory(category.getId());
			product.setStatus(ProductService.PRODUCT_NORNAL);
			Example<Product> create =Example.of(product);

			boolean existProduct = this.productRepo.exists(create);
			
			if(existProduct) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return StorageResult.failed("当前目录下有产品,无法删除: "+category.getText());				
			}
			categoryRepo.deleteById(category.getId());
		}
		
		jedisPool.getOperations().expire(REDIS_CACHE, 0,TimeUnit.SECONDS);
		categoriesFromWeb.removeAll(deletes);


		saveChildren(categoriesFromWeb);
		
		return StorageResult.succeed(categoryRepo.findAll());
	}

	private List<Category> getExistCategory(List<Category> categories) {
		List<Category> categoryExisting=new ArrayList<>();
		for (Category category : categories) {
			Integer id = category.getId();

			if(id!=null)
				categoryExisting.add(category);

		}
			return categoryExisting;
	}


	private void saveChildren( List<Category> categories){

		/**
		 * make sure the process run from top( root parents) to bottom (children).
		 */
		for (Category child : categories) {
			if (child.getStr_parents() == null) {
				findAllChildren(child, categories);
			}
		}

	}

	private void findAllChildren(Category parent, List<Category> categories) {
		List<Category> children=new ArrayList<>();

		/**
		 * find all the children
		 */
		for (Category category : categories) {
			if(category.getStr_parents()!=null&&category.getStr_parents().equals(parent.getStr_id())){
				children.add(category);
			}
		}
		/**
		 * save parents
		 * set valid parent id to children
		 */
		Category ParentInDB = categoryRepo.save(parent);
		for (Category child : children) {

			child.setParent(ParentInDB.getId());
			//save children
			findAllChildren(child,categories);
		}



	}


}




