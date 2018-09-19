package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.Category;
import com.storage.entity.Customer;

public interface CategoryRepo extends JpaRepository<Category,Integer>{

}
