package com.gachigage.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.gachigage.product.domain.Product;
import com.gachigage.product.dto.ProductListRequestDto;
import com.gachigage.product.dto.ProductListResponseDto;

public interface ProductRepositoryCustom {
	Page<ProductListResponseDto> search(ProductListRequestDto requestDto, Pageable pageable, Long loginMemberId);

	List<Product> findRelatedProducts(
		@Param("categoryId") Long categoryId,
		@Param("province") String province,
		@Param("city") String city,
		@Param("productId") Long productId,
		Pageable pageable);
}
