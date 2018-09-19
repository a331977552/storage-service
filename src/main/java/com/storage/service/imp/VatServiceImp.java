package com.storage.service.imp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.storage.entity.Vat;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.VatRepo;
import com.storage.service.VatService;
@Service
public class VatServiceImp implements  VatService{
	@Autowired
	VatRepo vatRepo;

	@Override
	public StorageResult addVat(Vat vat){
		if(vat==null)
			return StorageResult.failed("invalid parameter");

		this.vatRepo.save(vat);
		return StorageResult.succeed();
	}
	@Override
	public StorageResult updateVat(Vat vat ){
		
		if(vat==null )
			return StorageResult.failed("invalid parameter ");
		if(vat.getId()!=null)
		{
			Optional<Vat> findById = vatRepo.findById(vat.getId());
			if(findById.isPresent())
			{
				Vat vat2 = findById.get();
				vat2.setRate(vat.getRate());
				this.vatRepo.save(vat2);
				
			}
		}else {			
			this.vatRepo.save(vat);			
		}

		return StorageResult.succeed();
	}
	@Override
	public StorageResult deleteVatById(Integer id){
		if(id==null || id<0)
			return StorageResult.failed("invalid id");
		this.vatRepo.deleteById(id);
		return StorageResult.succeed();
	}
	@Override
	public StorageResult<List<Vat>> getVatByExample(Vat  vat ){


		List<Vat> results = this.vatRepo.findAll(Example.of(vat));


		return StorageResult.succeed(results);
	}
	@Override
	public StorageResult getVatById(Integer id){
		if(id==null || id<0)
			return StorageResult.failed("invalid id");

		Optional<Vat> vat = this.vatRepo.findById(id);
		if(vat.isPresent())
		return StorageResult.succeed(vat);
		return StorageResult.failed("no such Vat");
	}
	@Override
	public StorageResult updateVatSelective(Vat vat){
		if(vat==null ||vat.getId()==null ||vat.getId()<0)
			return StorageResult.failed("invalid parameter ");


		this.vatRepo.save(vat);
		return  StorageResult.succeed();

	}

	@Override
	public StorageResult count(){
		long count = this.vatRepo.count();
		return  StorageResult.succeed(count);
	}
	@Override
	public StorageResult findAll() {
		return StorageResult.succeed(vatRepo.findAll());
	}






}




