package com.gachigage.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponseDto {

	private Long id;
	private String name;
	private List<CategoryResponseDto> children;
}

