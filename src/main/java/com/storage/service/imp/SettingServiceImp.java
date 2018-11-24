package com.storage.service.imp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.storage.entity.Setting;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.SettingRepo;
import com.storage.service.SettingService;
import com.storage.utils.PojoUtils;
@Service
public class SettingServiceImp implements  SettingService{
	@Autowired
	SettingRepo settingRepo;

	@Override
	public StorageResult addSetting(Setting setting){
		if(setting==null)
			return StorageResult.failed("invalid parameter");
			Setting save = this.settingRepo.save(setting);
			
		return StorageResult.succeed(save);
	}
	@Override
	public StorageResult updateSetting(Setting setting ){
		if(setting==null)
			return StorageResult.failed("setting required ");
		Setting save;
		if(setting.getId()!=null) {
			Setting one = settingRepo.getOne(setting.getId());
			PojoUtils.copyNonNullProperties(setting, one);
			save= this.settingRepo.save(one);
			
		}else {
			 save = this.settingRepo.save(setting);			
		}

		return StorageResult.succeed(save);
	}
	@Override
	public StorageResult deleteSettingById(Integer id){
		if(id==null || id<0)
			return StorageResult.failed("invalid id");
		this.settingRepo.deleteById(id);
		return StorageResult.succeed();
	}
	@Override
	public StorageResult getSettingByExample(Setting  setting ){


	List<Setting> results = this.settingRepo.findAll(Example.of(setting));


		return StorageResult.succeed(results);
	}
	@Override
	public StorageResult getSettingById(Integer id){
		if(id==null || id<0)
			return StorageResult.failed("invalid id");
		List<Setting> findAll = settingRepo.findAll();
		if(findAll.isEmpty())
			return  StorageResult.failed("no  setting");
		
		return StorageResult.succeed(findAll.get(0));
	}
	@Override
	public StorageResult updateSettingSelective(Setting setting){
		if(setting==null ||setting.getId()==null ||setting.getId()<0)
			return StorageResult.failed("invalid parameter ");
		this.settingRepo.save(setting);
		return  StorageResult.succeed();

	}

	@Override
	public StorageResult<Long> count(){
		long count = this.settingRepo.count();
		return  StorageResult.succeed(count);
	}
	@Override
	public StorageResult<Setting> getSetting() {
		
		List<Setting> findAll = settingRepo.findAll();
		if(findAll.isEmpty())
		{
			Setting entity=new Setting();
			entity.setCurrencyDisplay(1);
			entity.setCurrencyRate(9.f);
			
			Setting save = settingRepo.save(entity);
			return StorageResult.succeed(save);
		}
		return StorageResult.succeed(findAll.get(0));
		
	}






}




