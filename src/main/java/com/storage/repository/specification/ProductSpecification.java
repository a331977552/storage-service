package com.storage.repository.specification;

import java.util.Date;

import javax.persistence.criteria.CriteriaQuery;

import org.springframework.data.jpa.domain.Specification;

import com.storage.entity.Product;
import com.storage.utils.StringUtils;

public class ProductSpecification {

	public static Specification<Product> nameLike(String name) {

		return (root, query, criteriaBuilder) -> {

			if (!com.storage.utils.StringUtils.isEmpty(name)) {
				return criteriaBuilder.like(root.get("name"), "%" + name + "%");
			} else
				return null;

		};
	}

	public static Specification<Product> idEqual(Integer id) {

		return (root, query, criteriaBuilder) -> {
			if (id == null || id < 0)
				return null;
			else {
				return criteriaBuilder.equal(root.get("id"), id);
			}
		};
	}

	public static Specification<Product> quantityEqual(Integer quantity) {

		return (root, query, criteriaBuilder) -> {
			if (quantity == null || quantity < 0)
				return null;
			else {
				return criteriaBuilder.equal(root.get("quantity"), quantity);
			}
		};
	}

	public static Specification<Product> buyingPriceEqual(Integer buyingPrice) {

		return (root, query, criteriaBuilder) -> {
			if (buyingPrice == null || buyingPrice < 0)
				return null;
			else {
				return criteriaBuilder.equal(root.get("buyingPrice"), buyingPrice);
			}
		};
	}

	public static Specification<Product> sellingpriceEqual(Integer sellingprice) {

		return (root, query, criteriaBuilder) -> {
			if (sellingprice == null || sellingprice < 0)
				return null;
			else {
				return criteriaBuilder.equal(root.get("sellingprice"), sellingprice);
			}
		};
	}

	public static Specification<Product> createdtimeEqual(Date createdtime) {

		return (root, query, criteriaBuilder) -> {
			if (createdtime == null)
				return null;
			else {
				return criteriaBuilder.equal(root.get("createdtime"), createdtime);
			}
		};
	}

	public static Specification<Product> updatetimeEqual(Date updatetime) {

		return (root, query, criteriaBuilder) -> {
			if (updatetime == null)
				return null;
			else {
				return criteriaBuilder.equal(root.get("updatetime"), updatetime);
			}
		};
	}

	public static Specification<Product> supplierEqual(String supplier) {

		return (root, query, criteriaBuilder) -> {
			if (StringUtils.isEmpty(supplier))
				return null;
			else {
				return criteriaBuilder.equal(root.get("supplier"), supplier);
			}
		};
	}

	public static Specification<Product> vatEqual(Integer vat) {

		return (root, query, criteriaBuilder) -> {
			if (vat == null)
				return null;
			else {
				return criteriaBuilder.equal(root.get("vat"), vat);
			}
		};
	}

	public static Specification<Product> categoryEqual(Integer category) {

		return (root, query, criteriaBuilder) -> {
			if (category == null || category < 0)
				return null;
			else {
				return criteriaBuilder.equal(root.get("category"), category);
			}
		};
	}

	public static Specification<Product> statueEqual(Integer status) {

		return (root, query, criteriaBuilder) -> {
			if (status == null || status < 0) {
				return criteriaBuilder.equal(root.get("status"), 1);
			}
			return criteriaBuilder.equal(root.get("status"), status);
		};
	}

	public static Specification<Product> orderByUpdateTime() {
		

		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<?> orderBy = query.orderBy(criteriaBuilder.desc(root.get("updateTime")));	
			return null;
		};
	}

}
