package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.FeedBack;

public interface FeedBackRepo extends JpaRepository<FeedBack, Integer> {

}
