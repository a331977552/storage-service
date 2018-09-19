package com.storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Orderitem;
import com.storage.service.OrderitemService;

@RestController()
@RequestMapping("/orderitem")
public class OrderitemController  {

	@Autowired
	OrderitemService service;

	/* (non-Javadoc)
	 * @see com.storage.controller.IOrderitemController#addOrderitem(com.storage.entity.Orderitem)
	 */
	
	@PostMapping("/add")
	public Object addOrderitem(Orderitem orderitem) {

		return this.service.addOrderitem(orderitem);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IOrderitemController#getOrderitem(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getOrderitem(@PathVariable(name = "id") Integer id) {

		return this.service.getOrderitemById(id);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IOrderitemController#deleteOrderitemById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteOrderitemById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteOrderitemById(id);
	}



	/* (non-Javadoc)
	 * @see com.storage.controller.IOrderitemController#updateOrderitem(com.storage.entity.Orderitem)
	 */
	
	@PostMapping("/update")
	public Object updateOrderitem(Orderitem orderitem) {
		return this.service.updateOrderitem(orderitem);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.IOrderitemController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		return this.service.count();
	}


}
