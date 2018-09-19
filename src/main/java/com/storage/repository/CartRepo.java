package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.Cart;

public interface CartRepo extends JpaRepository<Cart,Integer>{

	
	
}
