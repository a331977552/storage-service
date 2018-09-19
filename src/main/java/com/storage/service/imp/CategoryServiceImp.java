package com.storage.service.imp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.storage.entity.Category;
import com.storage.entity.Product;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.CategoryRepo;
import com.storage.repository.ProductRepo;
import com.storage.service.CategoryService;
import com.storage.utils.JsonUtils;
import com.storage.utils.StringUtils;


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
		if(StringUtils.isEmpty(category.getName())) {
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
		if(StringUtils.isEmpty(category.getName())) {
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

		
		String string = jedisPool.get(REDIS_CACHE);
		
		if(!StringUtils.isEmpty(string)) {
			return StorageResult.succeed(JsonUtils.jsonToList(string,Category.class));
		}

		

		Example<Category> of = Example.of(category);
		
		List<Category> results = this.categoryRepo.findAll(of);
		jedisPool.set(REDIS_CACHE,JsonUtils.objectToJson(results));
	
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
	public StorageResult<List<Category>> findAll() {
		return StorageResult.succeed(categoryRepo.findAll());
	}






}




