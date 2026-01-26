package com.gachigage.product.dto;

import java.util.List;

import com.gachigage.product.domain.PriceTableStatus;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductImage;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.domain.TradeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponseDto {
	private Long productId;
	private String title;
	private String detail;
	private String sellerName;

	private ProductCategoryDto category;
	private TradeType tradeType;
	private List<String> imageUrls;

	private Long stock;

	private List<ProductPriceDto> priceTable;
	private List<TradeLocationDto> preferredTradeLocations;

	private Integer viewCount;
	private Boolean isLiked;

	private RelatedProductsDto relatedProducts;

	public static ProductDetailResponseDto fromEntity(Product product, List<Product> relatedProducts) {

		// TODO : 활성화된 가격 테이블만 필터링하는 로직 추가
		// List<ProductPriceDto> productpriceDtos = product.getPrices().stream()
		// 	.filter(price -> price.getStatus() == PriceTableStatus.ACTIVE)
		// 	.map(price -> ProductPriceDto.builder()
		// 		.quantity(price.getQuantity())
		// 		.price(price.getPrice())
		// 		.build())
		// 	.toList();

		return ProductDetailResponseDto.builder()
			.productId(product.getId())
			.title(product.getTitle())
			.detail(product.getDescription())
			.sellerName(product.getSeller().getName())
			.category(ProductCategoryDto.builder()
				.main(product.getCategory().getParent().getName())
				.sub(product.getCategory().getName())
				.build())
			.tradeType(product.getTradeType())
			.imageUrls(product.getImages().stream()
				.map(ProductImage::getImageUrl)
				.toList())
			.stock(product.getStock())
			.priceTable(product.getPrices().stream()
				.map(price -> ProductPriceDto.builder()
					.quantity(price.getQuantity())
					.price(price.getPrice())
					.status(price.getStatus())
					.build())
				.toList())
			.preferredTradeLocations(
				List.of(TradeLocationDto.builder()
					.latitude(product.getLatitude())
					.longitude(product.getLongtitude())
					.address(product.getAddress())
					.build())
			)
			.viewCount(product.getVisitCount())
			.isLiked(false) // Placeholder for actual like status
			.relatedProducts(RelatedProductsDto.fromEntity(relatedProducts))
			.build();
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductCategoryDto {
		private String main;
		private String sub;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductPriceDto {
		private Integer quantity;
		private Integer price;
		private PriceTableStatus status;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TradeLocationDto {
		private Double latitude;
		private Double longitude;
		private String address;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RelatedProductsDto {

		private int size;
		private List<RelatedProductDto> products;

		public static RelatedProductsDto fromEntity(List<Product> relatedProducts) {
			List<RelatedProductDto> relatedProductDtos = relatedProducts.stream()
				.map(RelatedProductDto::fromEntity)
				.toList();

			return RelatedProductsDto.builder()
				.size(relatedProductDtos.size())
				.products(relatedProductDtos)
				.build();
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RelatedProductDto {

		private Long productId;
		private String title;
		private String thumbnailUrl;
		private Integer price; // Reverted to 'price'
		private Integer quantity; // Reverted to 'quantity'
		private String province; // Re-added
		private String city;     // Re-added
		private Integer viewCount; // Re-added

		public static RelatedProductDto fromEntity(Product product) {

			ProductPrice minQuantityProdcutPrice = product.getPrices().stream()
				.min((p1, p2) -> Integer.compare(p1.getQuantity(), p2.getQuantity()))
				.orElse(null);

			String thumbnailUrl = product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl();

			return RelatedProductDto.builder()
				.productId(product.getId())
				.title(product.getTitle())
				.thumbnailUrl(thumbnailUrl)
				.price(minQuantityProdcutPrice.getPrice())
				.quantity(minQuantityProdcutPrice.getQuantity())
				.province(product.getRegion().getProvince())
				.city(product.getRegion().getCity())
				.viewCount(product.getVisitCount())
				.build();
		}
	}
}
