package com.gachigage.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

	List<Product> findRelatedProducts(
		@Param("categoryId") Long categoryId,
		@Param("province") String province,
		@Param("city") String city,
		@Param("productId") Long productId,
		Pageable pageable);

	Page<Product> findAllBySellerId(Long sellerId, Pageable pageable);

	Page<Product> findAllBySellerIdAndStatus(Long sellerId, ProductStatus status, Pageable pageable);
}
