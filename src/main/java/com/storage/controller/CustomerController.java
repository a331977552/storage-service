package com.storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Customer;
import com.storage.service.CustomerService;

@RestController()
@RequestMapping("/customer")
public class CustomerController  {

	@Autowired
	CustomerService service;

	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#addCustomer(com.storage.entity.Customer)
	 */
	
	@PostMapping("/add")
	public Object addCustomer(@RequestBody Customer customer) {

		return this.service.addCustomer(customer);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#getCustomer(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getCustomer(@PathVariable(name = "id") Integer id) {
		return this.service.getCustomerById(id);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#getCustomerByName(java.lang.String)
	 */
	
	@GetMapping("/getbyName")
	public Object getCustomerByName(@RequestParam() String name) {
		return this.service.getCustomerByName(name);
	}
	@GetMapping("/createEmptyUser")
	public Object createEmptyUser() {
		return this.service.createEmptyUser();
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#getCustomer(java.lang.String)
	 */
	
	@GetMapping("/getName/{order}.json")
	public Object getCustomer(@PathVariable(name="order")String order) {

		return this.service.getCustomerList(order);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#list()
	 */
	
	@GetMapping("/list")
	public Object list() {

		return this.service.List();
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#deleteCustomerById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteCustomerById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteCustomerById(id);
	}



	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#updateCustomer(com.storage.entity.Customer)
	 */
	
	@PostMapping("/update")
	public Object updateCustomer(@RequestBody Customer customer) {
		return this.service.updateCustomer(customer);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.ICustomerController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		return this.service.count();
	}
	
	@PostMapping("/getCustomerByExample")
	public Object getCustomerByExample(@RequestBody Customer customer) {
		return this.service.getCustomerByExample(customer);
	}

	@GetMapping("/getbySession")
	public Customer getCustomerBySessionid(@RequestParam String sessionId) {
		return this.service.getCustomerBySessionid(sessionId);
	}

	@PostMapping("/login")
	public Object login(@RequestBody Customer customer) {
		return this.service.login(customer);
	}


}
