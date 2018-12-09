package com.storage.listener;

import com.storage.entity.Manager;
import com.storage.entity.Setting;
import com.storage.entity.Vat;
import com.storage.entity.custom.StorageResult;
import com.storage.service.ManagerService;
import com.storage.service.SettingService;
import com.storage.service.VatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener {

	@Autowired
	ManagerService managerService;
	
	@Autowired
	SettingService settingService;
	@Autowired
	VatService vatService;
	
	
	private final int RMB=2;
	private final int POUND=1;
	
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		StorageResult<Long> count = managerService.count();
		if(count.getResult() ==0) {
			Manager manager=new Manager();
			BCryptPasswordEncoder bcryptPasswordEncoder=new BCryptPasswordEncoder(11);
			String encode = bcryptPasswordEncoder.encode("111111");
			manager.setPassword(encode);
			manager.setUsername("a123456");
			managerService.addManager(manager);
		}
		
		count = settingService.count();
		if(count.getResult()==0) {
			Setting setting=new Setting();
			setting.setCurrencyRate(9.f);
			setting.setCurrencyDisplay(POUND);
			setting.setPhone("");
			settingService.addSetting(setting);
			
		}
		count = vatService.count();
		if(count.getResult()==0) {
			
				Vat vat=new Vat();
				vat.setRate(0.f);
				
				Vat vat2=new Vat();
				vat2.setRate(12.5f);
				
				Vat vat3=new Vat();
				vat3.setRate(20.f);
				
				vatService.addVat(vat);
				vatService.addVat(vat2);
				vatService.addVat(vat3);
				
		}
	}
	
}
