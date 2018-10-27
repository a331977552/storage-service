package com.storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Customer;
import com.storage.entity.StOrder;
import com.storage.entity.custom.OrderStatis;
import com.storage.entity.custom.OrderTableItem;
import com.storage.entity.custom.OrderWrap;
import com.storage.service.StOrderService;

@RestController()
@RequestMapping("/stOrder")
public class    StOrderController{

	@Autowired
	StOrderService service;

	/* (non-Javadoc)
	 * @see com.storage.controller.IStOrderController#addStOrder(com.storage.entity.StOrder)
	 */
	
	@PostMapping("/add")
	public Object addStOrder(StOrder stOrder) {

		return this.service.addStOrder(stOrder);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IStOrderController#getStOrder(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getStOrder(@PathVariable(name = "id") Integer id) {

		return this.service.getStOrderById(id);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IStOrderController#deleteStOrderById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteStOrderById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteStOrderById(id);
	}



	/* (non-Javadoc)
	 * @see com.storage.controller.IStOrderController#updateStOrder(com.storage.entity.StOrder)
	 */
	
	@PostMapping("/update")
	public Object updateStOrder(@RequestBody OrderTableItem item) {
		return this.service.updateStOrder(item);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.IStOrderController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		return this.service.count();
	}
	
	@GetMapping("/findAll")
	public Object findAll() {
		
		return this.service.findAllTableItems();
	}
	@RequestMapping("/getInfoFromOrder/{id}")	
	public	OrderWrap getInfoFromOrder(@PathVariable(name = "id") Integer orderId) {
		return service.getInfoFromOrder(orderId);
		
	}
	
	@RequestMapping("/createOrder")
	public Object creaOrder(@RequestBody OrderWrap result) {
		Customer customer = result.getCustomer();
		return this.service.creaOrder(customer, result);
	}
	@RequestMapping("/findAllOrderByUserid/{userId}")
	public  Object findAllOrderByUserId(@PathVariable("userId")Integer id){
		
		return service.findAllTableItemsByUserId(id);
		
	}
	
	@RequestMapping("/getStatistics")
	public  Object getStatistics(){
		
		OrderStatis statistics = service.getStatistics();
		return statistics;
		
	}
}
