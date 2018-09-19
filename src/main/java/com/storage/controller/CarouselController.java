package com.storage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Carousel;
import com.storage.entity.custom.StorageResult;
import com.storage.service.CarouselService;

@RestController
@RequestMapping("/api")
public class CarouselController {

	
	@Autowired
	CarouselService service;
	
	@PostMapping("/carousel/add")
	public StorageResult<Carousel> addCarousel(@RequestBody Carousel carousel){
		
		return service.add(carousel);
	}
	@PostMapping("/carousel/update")
	public StorageResult<Carousel> updateCarousel(@RequestBody Carousel carousel){
		
		return service.update(carousel);
	}

	@DeleteMapping("/carousel/{id}")
	public StorageResult<String> deleteCarousel(@PathVariable("id") Integer id){
		
		return service.deleteById(id);
	}
	@GetMapping("/carousel/{id}")
	public StorageResult<Carousel> getCarousel(@PathVariable("id") Integer id){
		
		return service.findById(id);
	}
	@GetMapping("/carousel/findAll")
	public StorageResult<List<Carousel>> getAllCarousel(){
		
		return service.findAll();
	}
	
	@GetMapping("/carousel/findByExample")
	public StorageResult<List<Carousel>> getAllCarousel(@RequestBody Carousel  carousel){
		return service.findAllByExample(carousel);
	}
	
	
	
	
	
	
	
}
