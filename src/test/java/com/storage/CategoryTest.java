package com.storage;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.storage.entity.Carousel;
import com.storage.utils.JsonUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

public class CategoryTest {


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url;
	@Before
	public void contexLoads() throws Exception {
		url="http://localhost:" + port + "/api/carousel";
		
	}

	@Test
	public void greetingShouldReturnDefaultMessage() throws Exception {

		Carousel carousel = new Carousel();
		carousel.setPic_url("https://speckyboy.com/wp-content/uploads/2018/07/free-bootstrap-template-02.jpg");
		carousel.setTitle("这是一个title");
		try {
			this.restTemplate.put(url, carousel);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		assertThat(this.restTemplate.put(url, objectToJson));
	}
	
	@Test
	public void delete() throws Exception {

		try {
			this.restTemplate.delete(url+"/59");		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		assertThat(this.restTemplate.put(url, objectToJson));
	}
	@Test
	public void find() throws Exception {

		try {
			ResponseEntity<Carousel> forEntity = this.restTemplate.getForEntity(url+"/1",Carousel.class);
			assertThat(forEntity.getBody()!=null);
			
			assertThat(forEntity.getBody().getPic_url().equals("1到底"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		assertThat(this.restTemplate.put(url, objectToJson));
	}
	@Test
	public void findAll() throws Exception {

		try {
			ResponseEntity<String> forEntity = this.restTemplate.getForEntity(url+"/1",String.class);
			List<Carousel> jsonToList = JsonUtils.jsonToList(forEntity.getBody(), Carousel.class);
			
			assertThat(!jsonToList.isEmpty());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		assertThat(this.restTemplate.put(url, objectToJson));
	}


}
