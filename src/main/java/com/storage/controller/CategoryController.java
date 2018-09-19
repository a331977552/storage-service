package com.storage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Category;
import com.storage.service.CategoryService;

@RestController()
@RequestMapping("/category")
@PropertySource("classpath:myapp.properties")
public class CategoryController {


			
	@Autowired
	CategoryService service;
	Logger logger = LoggerFactory.getLogger(CategoryController.class);

	/* (non-Javadoc)
	 * @see com.storage.controller.ICategoryController#addCategory(com.storage.entity.Category)
	 */
	
	@PostMapping("/add")
	public Object addCategory(@RequestBody Category category) {
	
		return this.service.addCategory(category);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ICategoryController#getCategory(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getCategory(@PathVariable(name = "id") Integer id) {

		return this.service.getCategoryById(id);
	}
	

	/* (non-Javadoc)
	 * @see com.storage.controller.ICategoryController#deleteCategoryById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteCategoryById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteCategoryById(id);
	}



	/*	@PostMapping("/update")
	public Object updateCategory(Category category) {
		return this.service.updateCategory(category);
	}*/
	/* (non-Javadoc)
	 * @see com.storage.controller.ICategoryController#updateCategory(com.storage.entity.custom.CustomeCategory)
	 */
	
	@PostMapping("/update")
	public Object updateCategory(@RequestBody Category category) {
	
		return this.service.updateCategory(category);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ICategoryController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		return this.service.count();
	}

	
	@GetMapping("/findAll")
	public Object findAll() {
		return service.findAll();
	}


}
