package com.storage;

import static org.junit.Assert.assertEquals;

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

import com.storage.entity.Advertisement;



@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

public class AdTest {

	 	@LocalServerPort
	    private int port;

	    @Autowired
	    private TestRestTemplate restTemplate;

	    private String url;
		@Before
		public void contexLoads() throws Exception {
			url="http://localhost:" + port + "/ad/";
			
		}

		@Test()		
		public void add() throws Exception {
			Advertisement advertisement=new Advertisement();
			advertisement.setClickUrl("www.baidu.com");
			advertisement.setImgUrl("http://wwww.img.com");
			advertisement.setMessage("hahah");
			advertisement.setPosition(1);
			ResponseEntity<Advertisement> forEntity = restTemplate.postForEntity(url+"add", advertisement,Advertisement.class);
			System.out.println(forEntity.getBody());
			String imgUrl = forEntity.getBody().getImgUrl();
			assertEquals(imgUrl, advertisement.getImgUrl());
			
		}
		@Test()		
		public void get() throws Exception {

			ResponseEntity<String> forEntity = restTemplate.getForEntity(url+"get/2", String.class);
			System.out.println(forEntity.getBody());
			
		}
		
		
			@Test
		public void update() throws Exception {
			Advertisement advertisement=new Advertisement();
			advertisement.setClickUrl("www.baidu.com2");
			advertisement.setImgUrl("http://wwww.img.com2");
			advertisement.setMessage("hahah2");
			advertisement.setPosition(1);
			advertisement.setId(302);
			ResponseEntity<String> forEntity = restTemplate.postForEntity(url+"update", advertisement,String.class);
			System.out.println(forEntity.getBody());
		
			
		}
			@Test
			public void delete() throws Exception {
				Advertisement advertisement=new Advertisement();

				advertisement.setId(302);
				ResponseEntity<String> forEntity = restTemplate.postForEntity(url+"delete", advertisement,String.class);
				System.out.println(forEntity.getBody());
			
				
			}
			
			
		
		
	
	
	
	
}
