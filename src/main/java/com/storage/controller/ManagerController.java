package com.storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Manager;
import com.storage.entity.custom.StorageResult;
import com.storage.service.ManagerService;

@RestController()
@RequestMapping("/manager")
@PropertySource("classpath:myapp.properties")
public class ManagerController  {

	@Autowired
	ManagerService service;


	/* (non-Javadoc)
	 * @see com.storage.controller.IManagerController#addManager(com.storage.entity.Manager)
	 */
	@GetMapping("/manager/getOne")
	public StorageResult<Manager>   getOne(){
		StorageResult<Manager> one = service.getOne();
		return one;
	}
	@PostMapping("/add")
	public Object addManager(Manager manager) {
		return this.service.addManager(manager);
	}
	/*@PostMapping("/login")
	public ModelAndView loginManager(@AuthenticationPrincipal User user,  ModelAndView model,HttpServletRequest request,HttpServletResponse response, Manager manager) {
		System.out.println("1111111111111111111111");

		return model;
	}*/

	/* (non-Javadoc)
	 * @see com.storage.controller.IManagerController#getManager(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getManager(@PathVariable(name = "id") Integer id) {
		return this.service.getManagerById(id);
	}
	
	@RequestMapping("/getByExample")
	public Object getManagerByExample( Manager manager) {
		return this.service.getManagerByExample(manager);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IManagerController#deleteManagerById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteManagerById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteManagerById(id);
	}
	/*@GetMapping("/logout")
	public Object signout() {
		return this.service.deleteManagerById(id);
	}*/

	



	/* (non-Javadoc)
	 * @see com.storage.controller.IManagerController#updateManager(com.storage.entity.Manager)
	 */
	
	@PostMapping("/update")
	public Object updateManager(Manager manager) {
		return this.service.updateManager(manager);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.IManagerController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		
		return this.service.count();
	}


}
