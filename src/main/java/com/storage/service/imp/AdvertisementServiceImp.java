package com.storage.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.storage.entity.Advertisement;
import com.storage.repository.AdvertisementRepo;
import com.storage.service.AdvertisementService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdvertisementServiceImp implements AdvertisementService {

	@Autowired
	AdvertisementRepo repo;
	@Override
	public Advertisement addAdvertisement(Advertisement ad) {
		
		return repo.save( ad);
	}

	@Override
	public void updateAdvertisement(Advertisement ad) {
		Optional<Advertisement> findById = repo.findById(ad.getId());
		if(!findById.isPresent()) {
			log.error("advertisement does not exist");
		}else {
			Advertisement advertisement = findById.get();
			advertisement.setClickUrl(ad.getClickUrl());
			advertisement.setImgUrl(ad.getImgUrl());
			advertisement.setMessage(ad.getMessage());
			repo.save( advertisement);
		}
	}

	@Override
	public void deleteAdvertisement(Advertisement ad) {
		repo.deleteById(ad.getId());
	}

	@Override
	public List<Advertisement> findAdvertisementByPosition(Integer direction) {
		
		Advertisement probe=new Advertisement();
		probe.setPosition(direction);
		List<Advertisement> findOne = repo.findAll(Example.of(probe));
		
		return findOne;
	}

}
