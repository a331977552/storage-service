package com.storage.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.storage.entity.FeedBack;
import com.storage.repository.FeedBackRepo;
import com.storage.service.FeedBackService;

@Service
public class FeedBackServiceImp  implements FeedBackService{
	@Autowired
	FeedBackRepo repo;


	@HystrixCommand(fallbackMethod="addOrUpdate_error")
	@Override
	public Optional<FeedBack> addOrUpdate(FeedBack back) {
		FeedBack save = repo.save(back);
		
		return Optional.of(save);
	}
	
	public Optional<FeedBack> addOrUpdate_error(FeedBack back,Throwable e) {
		e.printStackTrace();
		return Optional.empty();
	}

	@Override
	public void deleteById(Integer id) {
		repo.deleteById(id);
	}

	@Override
	public List<FeedBack> getAll() {
		// TODO Auto-generated method stub
		return repo.findAll();
	}

	@HystrixCommand(fallbackMethod="getById_error")
	@Override
	public Optional<FeedBack>  getById(Integer id) {
		return repo.findById(id);
	}
	public Optional<FeedBack> getById_error(Integer id,Throwable e) {
		e.printStackTrace();
		Optional<FeedBack> empty = Optional.empty();
		return empty;
	}

}
