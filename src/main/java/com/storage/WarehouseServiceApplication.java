package com.storage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;



//@EnableDiscoveryClient
@EnableDiscoveryClient
@SpringBootApplication
@EnableCircuitBreaker
public class WarehouseServiceApplication {

	public static void main(String[] args) {
		  new SpringApplicationBuilder(WarehouseServiceApplication.class)
		  
		  .run(args);
	}
	

	
	

	
}
