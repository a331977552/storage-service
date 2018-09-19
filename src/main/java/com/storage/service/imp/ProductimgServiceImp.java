package com.storage.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.storage.entity.Productimg;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.ProductImgRepo;
import com.storage.service.ProductimgService;

@Service
public class ProductimgServiceImp implements ProductimgService {
	@Autowired
	ProductImgRepo productimgMapper;

	
	@Override
	public StorageResult<Productimg> addProductimg(Productimg productimg) {
		if (productimg == null) {
			return StorageResult.failed("invalid parameter");
		}

		Productimg save = this.productimgMapper.save(productimg);
		return StorageResult.succeed(save);
	}
	

	@Override
	public StorageResult updateProductimg(Productimg productimg) {
		if (productimg == null || productimg.getId() == null || productimg.getId() < 0) {
			return StorageResult.failed("invalid parameter ");
		}

		this.productimgMapper.save(productimg);

		return StorageResult.succeed();
	}

	@Override
	public StorageResult deleteProductimgById(Integer id) {
		if (id == null || id < 0) {
			return StorageResult.failed("invalid id");
		}
		this.productimgMapper.deleteById(id);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult getProductimgByExample(Productimg productimg) {

		Example<Productimg> of = Example.of(productimg);
		List<Productimg> results = this.productimgMapper.findAll(of);

		return StorageResult.succeed(results);
	}

	@Override
	public StorageResult getProductimgById(Integer id) {
		if (id == null || id < 0) {
			return StorageResult.failed("invalid id");
		}

		Optional<Productimg> productimg = this.productimgMapper.findById(id);
		if(productimg.isPresent())
		return StorageResult.succeed(productimg);
		return StorageResult.failed("no such product img");
	}

	@Override
	public StorageResult updateProductimgSelective(Productimg productimg) {
		if (productimg == null || productimg.getId() == null || productimg.getId() < 0) {
			return StorageResult.failed("invalid parameter ");
		}

		Productimg save = this.productimgMapper.save(productimg);
		return StorageResult.succeed(save);

	}

	@Override
	public StorageResult count() {
		long count = this.productimgMapper.count();
		return StorageResult.succeed(count);
	}

}
