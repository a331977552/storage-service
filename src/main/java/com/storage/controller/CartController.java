package com.storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Cart;
import com.storage.service.CartService;

@RestController()
@RequestMapping("/api/cart")
public class CartController {

	@Autowired
	CartService service;
	
	@RequestMapping("/add")
	public Object addCart(@RequestBody Cart cart) {
		
		return service.addCart(cart);
	}
	@RequestMapping("/empty/{id}")
	public Object emptyCart(@PathVariable(name="id") Integer userId) {
		
		return service.emptyCart(userId);
	}
	@RequestMapping("/merge")
	public Object merge(@RequestBody Cart cart) {
		
		return service.mergeCart(cart);
	}
	
	@RequestMapping("/findByUserId/{id}")
	public Object findByUserId(@PathVariable(name="id")  Integer userId) {
		
		return service.findCartById(userId);
	}

}
