package com.storage.service.imp;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.storage.entity.Carousel;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.CarouselRepo;
import com.storage.service.CarouselService;
import com.storage.utils.PojoUtils;
import com.storage.utils.StringUtils;

@Service
@PropertySource("classpath:myapp.properties")
public class CarouselServiceImp implements CarouselService {

	@Autowired
	CarouselRepo repo;
	
	@Value("${seaweeddfs.url}")
	String seaweeddfsUrl;
	@Override
	public StorageResult<Carousel> add(Carousel carousel) {
		if(carousel==null || StringUtils.isEmpty(carousel.getPic_url())
				|| StringUtils.isEmpty(carousel.getTitle()))
			return StorageResult.failed("incomplete carousel");
		carousel.setCreatedtime(new Date());
		carousel.setUpdatetime(new Date());
		Carousel save = repo.save(carousel);
		return StorageResult.succeed(save);
	}

	@Override
	public StorageResult<Carousel> update(Carousel carousel) {
		if(carousel==null && StringUtils.isEmpty(carousel.getPic_url())
				&& StringUtils.isEmpty(carousel.getTitle())&& StringUtils.isEmpty(carousel.getRedirect_url()))
			return StorageResult.failed("incomplete carousel");
		if(carousel.getId()==null || carousel.getId()<0)
			return StorageResult.failed("illegal access, id required");
		carousel.setUpdatetime(new Date());
		Optional<Carousel> findById = repo.findById(carousel.getId());
		if(findById.isPresent()) {
			Carousel carousel2 = findById.get();
			PojoUtils.copyNonNullProperties(carousel, carousel2);
			return StorageResult.succeed(repo.save(carousel2));	
		}else {
			
			return StorageResult.failed("the carousel doesn't exist");
		}
		
	}
	
	@Override
	public StorageResult<String> deleteById(Integer id) {
		if(id==null || id<0)
			return StorageResult.failed("illegal access, id required");
		repo.deleteById(id);
		return StorageResult.succeed();
	}

	@Override
	public StorageResult<List<Carousel>> findAll() {
		
		List<Carousel> list=repo.findAll();
		for (Carousel carousel : list) {
			String pic_url = carousel.getPic_url();
			if(pic_url.startsWith("http"))
				continue;
			carousel.setPic_url(seaweeddfsUrl + ":8080/" +pic_url);
		}
		return StorageResult.succeed(list);
	}

	@Override
	public StorageResult<Carousel> findById(Integer id) {
		if(id==null || id<0)
			return StorageResult.failed("illegal access, id required");
		Optional<Carousel> findById = repo.findById(id);
		if(findById.isPresent())
		{
			Carousel carousel = findById.get();
			String pic_url = carousel.getPic_url();
			if(!pic_url.startsWith("http"))
			carousel.setPic_url(seaweeddfsUrl + ":8080/" +pic_url);		
			return StorageResult.succeed(findById.get());
		}
		else {
			return StorageResult.failed("failure to find the carousel");
		}
	}

	@Override
	public StorageResult<List<Carousel>> findAllByExample(Carousel carousel) {
		
		Example<Carousel> of = Example.of(carousel);
		
		List<Carousel> list= repo.findAll(of);
		for (Carousel car : list) {
			String pic_url = car.getPic_url();
			if(pic_url.startsWith("http"))
				continue;
			car.setPic_url(seaweeddfsUrl + ":8080/" +pic_url);
		}
		return StorageResult.succeed(list);
	}

}
