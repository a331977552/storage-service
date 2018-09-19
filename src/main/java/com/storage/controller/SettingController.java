package com.storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Setting;
import com.storage.entity.custom.StorageResult;
import com.storage.service.SettingService;

@RestController()
@RequestMapping("/setting")
public class SettingController   {

	@Autowired
	SettingService service;

	/* (non-Javadoc)
	 * @see com.storage.controller.ISettingController#addSetting(com.storage.entity.Setting)
	 */
	
	@PostMapping("/add")
	public Object addSetting(Setting setting) {

		return this.service.addSetting(setting);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ISettingController#getSetting(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getSetting(@PathVariable(name = "id") Integer id) {

		return this.service.getSettingById(id);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.ISettingController#deleteSettingById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteSettingById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteSettingById(id);
	}



	/* (non-Javadoc)
	 * @see com.storage.controller.ISettingController#updateSetting(com.storage.entity.Setting)
	 */
	
	@PostMapping("/update")
	public Object updateSetting(@RequestBody Setting setting) {
		return this.service.updateSetting(setting);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.ISettingController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		return this.service.count();
	}
	@GetMapping("/setting/getOne")	
	public  StorageResult<Setting> getSetting(){
		
		return service.getSetting();
	}


}
