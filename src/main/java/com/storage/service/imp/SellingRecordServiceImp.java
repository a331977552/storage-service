package com.storage.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.storage.entity.Product;
import com.storage.entity.SellingRecord;
import com.storage.repository.SellingRecoredRepo;
import com.storage.service.SellingRecordService;

@Service
public class SellingRecordServiceImp implements SellingRecordService {

	@Autowired
	SellingRecoredRepo repo;

	@HystrixCommand(fallbackMethod = "addOrUpdateSellingRecord_fallback")
	@Override
	public SellingRecord addOrUpdateSellingRecord(SellingRecord record) {
		SellingRecord result = repo.save(record);
		//TODO solve problem and add recommended products
		if (false) {
			Optional<SellingRecord> findById = repo.findById(record.getId());

			if (!findById.isPresent()) {
				SellingRecord probe = new SellingRecord();
				Product product = new Product();
				product.setId(record.getProduct().getId());
				probe.setProduct(product);
				List<SellingRecord> findAll = repo.findAll(Example.of(probe));
				if (findAll.isEmpty()) {
					result = repo.save(record);
				} else {
					SellingRecord sellingRecord = findAll.get(0);
					sellingRecord.setSellingAmount(record.getSellingAmount());
					sellingRecord.setReturnedAmount(record.getReturnedAmount());
					result = repo.save(sellingRecord);
				}
			} else {
				SellingRecord sellingRecord = findById.get();
				sellingRecord.setSellingAmount(record.getSellingAmount());
				sellingRecord.setReturnedAmount(record.getReturnedAmount());
				result = repo.save(sellingRecord);
			}
		}
		return result;
	}

	public SellingRecord addOrUpdateSellingRecord_fallback(SellingRecord record, Throwable e) {
		e.printStackTrace();
		return null;
	}

	@Override
	public Optional<SellingRecord> getSellingRecodByProductId(Integer productId) {
		SellingRecord probe = new SellingRecord();
		Product product = new Product();
		product.setId(productId);
		probe.setProduct(product);
		Optional<SellingRecord> findAll = repo.findOne(Example.of(probe));
		return findAll;
	}

	@Override
	public List<SellingRecord> getAll(int page,int pageSize) {
		
		Sort sort=Sort.by("sellingAmount").descending();
		PageRequest of = PageRequest.of(page,pageSize,sort);
		Page<SellingRecord> findAll = repo.findAll(of);
		List<SellingRecord> content = findAll.getContent();
		return content;
	}

}
