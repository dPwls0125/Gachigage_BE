package com.gachigage.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.dto.CategoryResponseDto;
import com.gachigage.product.repository.ProductCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCategoryService {

	private final ProductCategoryRepository categoryRepository;

	public List<CategoryResponseDto> getCategories() {

		List<ProductCategory> parents =
			categoryRepository.findAllParentsWithChildren();

		List<CategoryResponseDto> result = new ArrayList<>();

		for (ProductCategory parent : parents) {
			result.add(toDto(parent));
		}

		return result;
	}

	private CategoryResponseDto toDto(ProductCategory parent) {
		List<CategoryResponseDto> children = new ArrayList<>();

		for (ProductCategory child : parent.getChildren()) {
			children.add(
				new CategoryResponseDto(
					child.getId(),
					child.getName(),
					List.of()
				)
			);
		}

		return new CategoryResponseDto(
			parent.getId(),
			parent.getName(),
			children
		);
	}
}

