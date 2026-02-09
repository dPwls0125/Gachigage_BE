package com.gachigage.product.dto;

import java.util.List;

public record ProductImageResponseDto(
	List<String> imageUrls
) {
}
