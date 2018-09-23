package com.storage;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.storage.entity.Product;
import com.storage.utils.JsonUtils;



@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

public class ProductTest {

	 	@LocalServerPort
	    private int port;

	    @Autowired
	    private TestRestTemplate restTemplate;

	    private String url;
		@Before
		public void contexLoads() throws Exception {
			url="http://localhost:" + port + "/product/getbestsellingproduct?category=69";
			
		}

		@Test()		
		public void getbestsellingproduct() throws Exception {
			ResponseEntity<String> forEntity = restTemplate.getForEntity(url,String.class);
			System.out.println(forEntity.getBody());
			List<Product> jsonToList = JsonUtils.jsonToList(forEntity.getBody(), Product.class);
			Product product = jsonToList.get(0);
			Product product2 = jsonToList.get(1);
			
		}
		
		
	
	
	
	
}
