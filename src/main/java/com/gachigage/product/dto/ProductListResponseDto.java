package com.gachigage.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductListResponseDto {

    private final Long productId;
    private final String title;
    private final int minPrice;
    private final int maxPrice;
    private final String thumbnailUrl;
    private final String category; // main category or sub category
    private final String province;
    private final String city;
    private final String district;
    private final String tradeType;
    private final int viewCount;
    private final LocalDateTime createdAt;

    @Builder
    public ProductListResponseDto(Long productId, String title, int minPrice, int maxPrice, String thumbnailUrl, String category, String province, String city, String district, String tradeType, int viewCount, LocalDateTime createdAt) {
        this.productId = productId;
        this.title = title;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.category = category;
        this.province = province;
        this.city = city;
        this.district = district;
        this.tradeType = tradeType;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }
}
