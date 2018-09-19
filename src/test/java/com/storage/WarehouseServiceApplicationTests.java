package com.storage;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.storage.entity.Customer;
import com.storage.repository.CustomerRepo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WarehouseServiceApplicationTests {

	@Autowired
	CustomerRepo repository;
	
	@Test
	public void contextLoads() {
	
		
	}

}
