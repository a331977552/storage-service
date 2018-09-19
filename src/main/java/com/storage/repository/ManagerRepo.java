package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storage.entity.Customer;
import com.storage.entity.Manager;

public interface ManagerRepo extends JpaRepository<Manager,Integer>{

}
