package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.Customer;
import com.storage.entity.Orderitem;

public interface OrderItemRepo extends JpaRepository<Orderitem,Integer>{

}
