package com.storage.service.imp;

import com.storage.entity.Customer;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.CustomerRepo;
import com.storage.service.CustomerService;
import com.storage.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@org.springframework.transaction.annotation.Transactional
public class CustomerServiceImp implements CustomerService {
	@Autowired
	CustomerRepo customerRepo;
 
	@Override
	public StorageResult addCustomer(Customer customer) {
		if (customer == null)
			return StorageResult.failed("invalid parameter");
		Customer example=new Customer();
		example.setSessionId(customer.getSessionId());
		Optional<Customer> one = customerRepo.findOne(Example.of(example));
		if(one.isPresent())
		{
			example=one.get();
		}
			// if has ID, then update
		if (StringUtils.isEmpty(customer.getName()))
			return StorageResult.failed("customer name required");
		if (StringUtils.isEmpty(customer.getEmail()))
			return StorageResult.failed("customer email required");
		if (StringUtils.isEmpty(customer.getPassword()))
			return StorageResult.failed("customer password required");
		Customer probe=new Customer();
		probe.setEmail(customer.getEmail());
		long count = customerRepo.count(Example.of(probe));
		if(count>0) {
			return StorageResult.failed("email address already in use");
		}
		customer.setUpdatetime(new Date());
		customer.setCreatetime(new Date());
		customer.setLanguage("zh");
		customer.setIsEmpty(0);

		BeanUtils.copyProperties(customer,example,"id");
		Customer save = customerRepo.save(example);
		return StorageResult.succeed(save);


	}

	@Override
	public StorageResult updateCustomer(Customer customer) {
		if (customer == null || customer.getId() == null || customer.getId() < 0)
			return StorageResult.failed("invalid parameter ");
		Customer one = customerRepo.getOne(customer.getId());
		if(!StringUtils.isEmpty(customer.getAddress()))
		one.setAddress(customer.getAddress());
		if(!StringUtils.isEmpty(customer.getName()))
			one.setName(customer.getName());
		if(!StringUtils.isEmpty(customer.getEmail()))
			one.setEmail(customer.getEmail());
		if(!StringUtils.isEmpty(customer.getPostcode()))
			one.setPostcode(customer.getPostcode());
		if(!StringUtils.isEmpty(customer.getPhone()))
			one.setPhone(customer.getPhone());
		if(!StringUtils.isEmpty(customer.getFirstName()))
			one.setFirstName(customer.getFirstName());
		if(!StringUtils.isEmpty(customer.getLastName()))
			one.setLastName(customer.getLastName());
		if(!StringUtils.isEmpty(customer.getLanguage()))
			one.setLanguage(customer.getLanguage());
		customerRepo.save(one);

		return StorageResult.succeed();
	}

	@Override
	public StorageResult deleteCustomerById(Integer id) {
		if (id == null || id < 0)
			return StorageResult.failed("invalid id");
		customerRepo.deleteById(id);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult getCustomerByExample(Customer customer) {

		Example<Customer> of = Example.of(customer);

		List<Customer> results = this.customerRepo.findAll(of);

		return StorageResult.succeed(results);
	}
	@Override
	public Customer createEmptyUser(){
		Customer customer=new Customer();
		customer.setIsEmpty(1);
		customer.setSessionId(UUID.randomUUID().toString());
		customer.setUpdatetime(new Date());
		customer.setCreatetime(new Date());
		customer.setLanguage("zh");

		return customerRepo.save(customer);
	}
	@Override
	public StorageResult getCustomerById(Integer id) {
		if (id == null || id < 0)
			return StorageResult.failed("invalid id");

		Optional<Customer> customer = this.customerRepo.findById(id);
		if (customer.isPresent())
			return StorageResult.succeed(customer.get());
		else {
			return StorageResult.failed("no such customer");
		}
	}

	@Override
	public StorageResult updateCustomerSelective(Customer customer) {
		if (customer == null || customer.getId() == null || customer.getId() < 0)
			return StorageResult.failed("invalid parameter ");

		this.customerRepo.save(customer);
		return StorageResult.succeed();

	}

	@Override
	public StorageResult count() {
		long count = this.customerRepo.count();
		return StorageResult.succeed(count);
	}

	@Override
	public Object getCustomerList(String order) {
		return customerRepo.findAll();
	}

	@Override
	public StorageResult getCustomerByName(String name) {
		if (StringUtils.isEmpty(name))
			return StorageResult.failed("name required");
		Customer customer=new Customer();
		customer.setName(name);
		Example<Customer> of = Example.of(customer);
		
		List<Customer> selectByExample = customerRepo.findAll(of);
		return StorageResult.succeed(selectByExample);
	}

	@Override
	public Object List() {

		return customerRepo.findAll();
	}

	@Override
	public Object login(Customer customer) {
		Customer customer2=new Customer();
		if(!StringUtils.isEmail(customer.getEmail()))
			return StorageResult.failed("vaild email address required");
		
		if(StringUtils.isEmpty(customer.getPassword()))
			return StorageResult.failed("password required");
		
		customer2.setEmail(customer.getEmail());
		customer2.setPassword(customer.getPassword());
		Example<Customer> of = Example.of(customer);

		List<Customer> results = this.customerRepo.findAll(of);
		if(results.isEmpty())
			return StorageResult.failed("cannot find a match!");
		return StorageResult.succeed(results.get(0));
	}

	@Override
	public Customer getCustomerBySessionid(String sessionId) {
		Customer customer=new Customer();
		customer.setSessionId(sessionId);
		Optional<Customer> one = customerRepo.findOne(Example.of(customer));
		return one.orElse(null);
	}

}
