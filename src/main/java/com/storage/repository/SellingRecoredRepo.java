package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.SellingRecord;

public interface SellingRecoredRepo extends  JpaRepository<SellingRecord,Integer>{

}
