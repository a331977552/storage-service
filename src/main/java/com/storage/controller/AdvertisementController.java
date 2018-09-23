package com.storage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Advertisement;
import com.storage.service.AdvertisementService;

import lombok.extern.slf4j.Slf4j;

@RestController()
@RequestMapping("/ad")
@Slf4j
public class AdvertisementController {

	@Autowired
	AdvertisementService service;
	
	@RequestMapping("/add")
	public Advertisement addAdvertisement(@RequestBody Advertisement ad) {
		return service.addAdvertisement(ad);
	}

	@RequestMapping("/update")
	public void updateAdvertisement(@RequestBody Advertisement ad){
		service.updateAdvertisement(ad);
	}

	@RequestMapping("/delete")
	public void deleteAdvertisement(@RequestBody Advertisement ad){
		service.deleteAdvertisement(ad);
	}

	@RequestMapping("/get/{direction}")
	public List<Advertisement> findAdvertisementByPosition(@PathVariable("direction")Integer direction){
		return service.findAdvertisementByPosition(direction);
	}
	
	
	
	
	
}
