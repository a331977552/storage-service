package com.storage.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.storage.entity.Orderitem;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.OrderItemRepo;
import com.storage.service.OrderitemService;

@Service
public class OrderitemServiceImp implements OrderitemService {
	@Autowired
	OrderItemRepo orderitemRepo;

	@Override
	public StorageResult addOrderitem(Orderitem orderitem) {
		if (orderitem == null) {
			return StorageResult.failed("invalid parameter");
		}

		this.orderitemRepo.save(orderitem);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult updateOrderitem(Orderitem orderitem) {
		if (orderitem == null || orderitem.getId() == null || orderitem.getId() < 0) {
			return StorageResult.failed("invalid parameter ");
		}

		this.orderitemRepo.save(orderitem);

		return StorageResult.succeed();
	}

	@Override
	public StorageResult deleteOrderitemById(Integer id) {
		if (id == null || id < 0) {
			return StorageResult.failed("invalid id");
		}
		this.orderitemRepo.deleteById(id);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult getOrderitemByExample(Orderitem orderitem) {

		Example<Orderitem> example = Example.of(orderitem);
		List<Orderitem> results = this.orderitemRepo.findAll(example);

		return StorageResult.succeed(results);
	}

	@Override
	public StorageResult getOrderitemById(Integer id) {
		if (id == null || id < 0) {
			return StorageResult.failed("invalid id");
		}

		Optional<Orderitem> orderitem = this.orderitemRepo.findById(id);
		if (orderitem.isPresent())
			return StorageResult.succeed(orderitem);
		else {
			return StorageResult.failed("not such OrderItem");
		}
	}

	@Override
	public StorageResult updateOrderitemSelective(Orderitem orderitem) {
		if (orderitem == null || orderitem.getId() == null || orderitem.getId() < 0) {
			return StorageResult.failed("invalid parameter ");
		}

		this.orderitemRepo.save(orderitem);
		return StorageResult.succeed(orderitem);

	}

	@Override
	public StorageResult count() {
		long count = this.orderitemRepo.count();
		return StorageResult.succeed(count);
	}

}
