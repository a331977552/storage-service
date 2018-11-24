package com.storage.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.storage.entity.OptionalSearch;
import com.storage.repository.OptionalSearchRepo;
import com.storage.service.OptionalSearchService;

@Service
public class OptionalSearchServiceImp implements OptionalSearchService {

	@Autowired
	OptionalSearchRepo repo;
	
	
	@Override
	public Optional<OptionalSearch> findOneById(Integer id) {
		return repo.findById(id);
	}

	@Override
	public List<OptionalSearch> findAll() {
		return repo.findAll();
	}

	@Override
	public List<OptionalSearch> findAll(OptionalSearch example) {
		return repo.findAll(org.springframework.data.domain.Example.of(example));
	}

	@Override
	public void deleteById(Integer id) {
		repo.deleteById(id);
	}

	@Override
	public void deleteByIds(List<Integer> ids) {
		for (Integer integer : ids) {
			repo.deleteById(integer);
		}
		
	}

	@Override
	public OptionalSearch add(OptionalSearch optionalSearch) {
		OptionalSearch save = repo.save(optionalSearch);
		
		return save;
	}

	@Override
	public  List<OptionalSearch>  addAll(List<OptionalSearch> optionalSearch) {
		List<OptionalSearch> saveAll = repo.saveAll(optionalSearch);
		
		
		return saveAll; 
	}

	@Override
	public OptionalSearch update(OptionalSearch optionalSearch) {
		return 	 repo.save(optionalSearch);
	}

}
