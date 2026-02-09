package com.gachigage.product.dto;

public record ProductLikeResponseDto(
	boolean liked,
	int likeCount
) {
}
