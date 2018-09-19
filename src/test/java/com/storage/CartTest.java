package com.storage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
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

import com.storage.entity.Cart;
import com.storage.entity.custom.CustomCart;
import com.storage.utils.JsonUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

public class CartTest {


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url;
	@Before
	public void contexLoads() throws Exception {
		url="http://localhost:" + port + "/api/cart";
		
	}

	@Test
	public void add() throws Exception {
		Cart cart=new Cart();
		cart.setUserId(1);
		List<CustomCart> list=new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(new CustomCart(i,RandomUtils.nextInt(30)));
		}
		String json=JsonUtils.objectToJson(list);
		cart.setItems(json);

		try {
		
			String postForObject = this.restTemplate.postForObject(url+"/add",cart, String.class);
			System.out.println(postForObject);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		assertThat(this.restTemplate.put(url, objectToJson));
	}
	
	@Test
	public void find() throws Exception {
		ResponseEntity<String> postForObject = this.restTemplate.getForEntity(url+"/findByUserId/2", String.class);
		System.out.println(postForObject.getBody());
		
	}
	@Test
	public void merge() throws Exception {
		Cart cart=new Cart();
		cart.setUserId(2);
		List<CustomCart> list=new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(new CustomCart(i,1));
		}
		String json=JsonUtils.objectToJson(list);
		cart.setItems(json);

		try {
		
			String postForObject = this.restTemplate.postForObject(url+"/merge",cart, String.class);
			System.out.println(postForObject);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Test
	public void empty() throws Exception {
		Cart cart=new Cart();
		cart.setUserId(2);
		try {
			
			ResponseEntity<String> postForObject = this.restTemplate.getForEntity(url+"/empty/2", String.class);
			System.out.println(postForObject.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
