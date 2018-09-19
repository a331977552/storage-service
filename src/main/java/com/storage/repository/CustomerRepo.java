package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.Customer;

public interface CustomerRepo extends JpaRepository<Customer,Integer>{

}
