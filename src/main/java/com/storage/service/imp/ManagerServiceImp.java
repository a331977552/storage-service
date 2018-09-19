package com.storage.service.imp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.storage.entity.Manager;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.ManagerRepo;
import com.storage.service.ManagerService;
import com.storage.utils.IDUtils;
@Service
public class ManagerServiceImp implements  ManagerService{
	@Autowired
	ManagerRepo managerRepo;

	@Override
	public StorageResult<Manager> addManager(Manager manager){
		if(manager==null) {
			return StorageResult.failed("invalid parameter");
		}

		String password = manager.getPassword();
		/*BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

		manager.setPassword(bCryptPasswordEncoder.encode(password.trim()));*/
		this.managerRepo.save(manager);
		return StorageResult.succeed();
	}
	@Override
	public StorageResult<Manager> updateManager(Manager manager ){
		if(manager==null ||manager.getId()==null ||manager.getId()<0) {
			return StorageResult.failed("invalid parameter ");
		}

		Manager save = this.managerRepo.save(manager);
		return StorageResult.succeed(save);
	}
	@Override
	public StorageResult<Manager> deleteManagerById(Integer id){
		if(id==null || id<0){
			return StorageResult.failed("invalid id");
		}
		this.managerRepo.deleteById(id);
		return StorageResult.succeed();
	}
	@Override
	public StorageResult<List<Manager>> getManagerByExample(Manager  manager ){

		manager.setId(null);
		manager.setRemember(false);
		manager.setToken(null);
		
		Example<Manager> of = Example.of(manager);
	

		List<Manager> results = this.managerRepo.findAll(of);

		return StorageResult.succeed(results);
	}
	@Override
	public StorageResult<Manager> getManagerById(Integer id){
		if(id==null || id<0){
			return StorageResult.failed("invalid id");
		}

		Optional<Manager> manager = this.managerRepo.findById(id);
		if(manager.isPresent())
		return StorageResult.succeed(manager.get());
		else {
			return StorageResult.failed("no such manager");
		}
	}
	@Override
	public StorageResult<Manager> updateManagerSelective(Manager manager){
		if(manager==null ||manager.getId()==null ||manager.getId()<0) {
			return StorageResult.failed("invalid parameter ");
		}


		this.managerRepo.save(manager);
		return  StorageResult.succeed();

	}

	@Override
	public StorageResult<Long> count(){
		long count = this.managerRepo.count();
		return  StorageResult.succeed(count);
	}
	@Override
	public StorageResult<Manager> login(Manager manager) {
		if(manager==null) {
			return StorageResult.failed("invalid parameter ");
		}
	

		if(StringUtils.isEmpty(manager.getUsername())){
			return StorageResult.failed("empty userName");
		}
		if(StringUtils.isEmpty(manager.getPassword())){
			return StorageResult.failed("empty password");
		}

		Example<Manager> of = Example.of(manager);
		
		List<Manager> selectByExample = this.managerRepo.findAll(of);
		if(selectByExample!=null && selectByExample.size() >0) {
			if(manager.isRemember()) {
				//TODO
				String uuid = IDUtils.getUUID();
				manager.setToken(uuid);
			}
			manager.setPassword(null);
			return StorageResult.succeed(manager);
		}else {
			return StorageResult.failed("username or password is wrong");
		}
	}
	@Override
	public StorageResult<Manager> getOne() {
		
		List<Manager> findAll = managerRepo.findAll();
		if(findAll.isEmpty())
			return StorageResult.failed("no manager error");
		return StorageResult.succeed(findAll.get(0));
	}






}




