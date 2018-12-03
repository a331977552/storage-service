package com.storage.service.imp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.storage.entity.Cart;
import com.storage.entity.custom.CustomCart;
import com.storage.entity.custom.StorageResult;
import com.storage.repository.CartRepo;
import com.storage.service.CartService;
import com.storage.utils.JsonUtils;
import com.storage.utils.StringUtils;

@Service
public class CartServiceImp implements CartService {

	@Autowired
	CartRepo repo;
	Logger logger = LoggerFactory.getLogger(CartServiceImp.class);

	@Override
	public StorageResult<Cart> addCart(Cart cart) {

		if (cart == null || cart.getUserId() < 0 || StringUtils.isEmail(cart.getItems())) {
			return StorageResult.failed("invalid cart");
		}


		Cart save = repo.save(cart);
		return StorageResult.succeed(save);
	}

	@Override
	public StorageResult<Cart> updateCart(Cart cart) {
		return addCart(cart);
	}

	@Override
	public StorageResult<String> deleteCartById(Integer  userId) {
		return null;
	}

	@Override
	public StorageResult<Cart> findCartById(Integer userId) {
		Cart probe=new Cart();
		probe.setUserId(userId);
		Optional<Cart> findOne = repo.findOne(Example.of(probe));
		if(findOne.isPresent())
			return StorageResult.succeed(findOne.get());
		logger.warn("no such cart by user id" + userId);
		return StorageResult.failed("no items");
	}

	@Override
	public StorageResult<Cart> findAllCarts() {
		return null;
	}

	@Override
	public StorageResult<Cart> emptyCart(Integer userId) {
		Cart cart = new Cart();
		cart.setUserId(userId);

		List<Cart> findAll = repo.findAll(Example.of(cart));
		if (!findAll.isEmpty()) {
			Cart cart2 = findAll.get(0);
			cart2.setItems("");
			Cart save = repo.save(cart2);
			return StorageResult.succeed(save);
		} else {
			logger.warn("cannot find the cart by user id " + userId);
		}
		return StorageResult.succeed(cart);
	}

	@Override
	public StorageResult<Cart> mergeCart(Cart cart) {

		Cart cart2 = new Cart();
		cart2.setUserId(cart.getUserId());
		Cart save = null;
		List<Cart> findAll = repo.findAll(Example.of(cart2));
		if (!findAll.isEmpty()) {
			Cart cart3 = findAll.get(0);
			String items = cart3.getItems();
			String items2 = cart.getItems();
			String newItems = merge(items, items2);
			cart3.setItems(newItems);
			save = repo.save(cart3);
		} else {
			save = repo.save(cart);
		}

		return StorageResult.succeed(save);
	}

	private String merge(String items, String items2) {
		List<CustomCart> itemList1 = JsonUtils.jsonToList(items, CustomCart.class);
		List<CustomCart> itemList2 = JsonUtils.jsonToList(items2, CustomCart.class);
		Map<Integer, CustomCart> map = new HashMap<>();
		for (CustomCart customCart : itemList1) {
			map.put(customCart.getProductid(), customCart);
		}
		for (CustomCart customCart : itemList2) {
			CustomCart customCart2 = map.get(customCart.getProductid());
			if (customCart2 != null) {
				customCart2.setQuantity(customCart2.getQuantity() + customCart.getQuantity());
			} else {
				map.put(customCart.getProductid(), customCart);
			}

		}
		Collection<CustomCart> values = map.values();
		String objectToJson = JsonUtils.objectToJson(values);
		

		return objectToJson;
	}
	


}
