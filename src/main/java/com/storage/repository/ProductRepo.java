package com.storage.repository;

import com.storage.entity.Product;
import com.storage.entity.custom.CustomeProductName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product,Integer>,JpaSpecificationExecutor<Product>{

	@Query("from com.storage.entity.Product where quantity < ?1 and status = 2 ")
	
	List<Product> findStockReminder(Integer bound);
	@Query("select new com.storage.entity.custom.CustomeProductName(a.id,a.name) from com.storage.entity.Product a where a.category= ?1 ")
	List<CustomeProductName> findCustomerNameByCategory(Integer category);

	@Query("select new com.storage.entity.custom.CustomeProductName(a.id,a.name) from com.storage.entity.Product a")
	List<CustomeProductName> findAllProductName();
	
	
}
