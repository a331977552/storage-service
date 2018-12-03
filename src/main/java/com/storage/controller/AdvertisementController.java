package com.storage.controller;

import com.storage.entity.Advertisement;
import com.storage.service.AdvertisementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
	
	@GetMapping("/getall")
	public List<Advertisement> findAllAds(){
		return service.findAllVisibleAds();
	}
	
	
	
}
