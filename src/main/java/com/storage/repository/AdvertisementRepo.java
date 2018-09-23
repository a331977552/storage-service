package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.Advertisement;

public interface AdvertisementRepo  extends JpaRepository<Advertisement,Integer>{

}
