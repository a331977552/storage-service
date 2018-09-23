package com.storage.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.FeedBack;
import com.storage.service.FeedBackService;

@RestController

public class FeedbackController {

	@Autowired
	FeedBackService service;
	@PutMapping("/feedback")
	public Optional<FeedBack> addFeedback(@RequestBody FeedBack feedback,HttpServletResponse response){
		Optional<FeedBack> addOrUpdate = service.addOrUpdate(feedback);
		
		return addOrUpdate;
			
		
	}

	
	
}
