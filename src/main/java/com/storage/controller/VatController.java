package com.storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storage.entity.Vat;
import com.storage.service.VatService;

@RestController()
@RequestMapping("/vat")
public class VatController   {

	@Autowired
	VatService service;

	/* (non-Javadoc)
	 * @see com.storage.controller.IVatController#addVat(com.storage.entity.Vat)
	 */
	
	@PostMapping("/add")
	public Object addVat(Vat vat) {

		return this.service.addVat(vat);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IVatController#getVat(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getVat(@PathVariable(name = "id") Integer id) {

		return this.service.getVatById(id);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IVatController#deleteVatById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteVatById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteVatById(id);
	}



	/* (non-Javadoc)
	 * @see com.storage.controller.IVatController#updateVat(com.storage.entity.Vat)
	 */
	
	@PostMapping("/update")
	public Object updateVat(@RequestBody Vat vat) {
		return this.service.updateVat(vat);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.IVatController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		return this.service.count();
	}
	
	@GetMapping("/findAll")
	public Object findAll() {
		return this.service.findAll();
	}

}
