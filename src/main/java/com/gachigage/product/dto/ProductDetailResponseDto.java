package com.gachigage.product.dto;

import com.gachigage.product.domain.Category;
import com.gachigage.product.domain.PriceTable;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.TradeLocation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductDetailResponseDto {

    private final Long productId;
    private final String title;
    private final String detail;
    private final String sellerName; // TODO: User 정보에서 가져오기
    private final Category category;
    private final List<String> tradeTypes;
    private final List<String> imageUrls;
    private final List<PriceTable> priceTable;
    private final List<TradeLocation> preferredTradeLocations;
    private final int viewCount;
    private final int likeCount;
    private final boolean isLiked; // TODO: User의 좋아요 여부 확인
    private final List<RelatedProductDto> relatedProducts; // TODO: 연관 상품 정보

    @Getter
    public static class RelatedProductDto {
        private final Long productId;
        private final String title;
        private final String thumbnailUrl;
        private final int minPrice;
        private final int maxPrice;

        @Builder
        public RelatedProductDto(Long productId, String title, String thumbnailUrl, int minPrice, int maxPrice) {
            this.productId = productId;
            this.title = title;
            this.thumbnailUrl = thumbnailUrl;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }

    @Builder
    public ProductDetailResponseDto(Product product, String sellerName, boolean isLiked, List<RelatedProductDto> relatedProducts) {
        this.productId = product.getId();
        this.title = product.getTitle();
        this.detail = product.getDetail();
        this.sellerName = sellerName;
        this.category = product.getCategory();
        this.tradeTypes = product.getTradeTypes();
        this.imageUrls = product.getImageUrls();
        this.priceTable = product.getPriceTable();
        this.preferredTradeLocations = product.getPreferredTradeLocations();
        this.viewCount = product.getViewCount();
        this.likeCount = product.getLikeCount();
        this.isLiked = isLiked;
        this.relatedProducts = relatedProducts;
    }
}
