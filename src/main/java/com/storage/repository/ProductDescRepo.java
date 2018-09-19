package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.Customer;
import com.storage.entity.OrderShipping;
import com.storage.entity.Orderitem;
import com.storage.entity.Product;
import com.storage.entity.ProductDesc;

public interface ProductDescRepo extends JpaRepository<ProductDesc,Integer>{

}
