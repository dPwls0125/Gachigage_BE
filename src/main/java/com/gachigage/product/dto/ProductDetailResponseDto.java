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
import lombok.Setter;

@Getter
@Builder(toBuilder = true)
@Setter
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
	private TradeLocationDto preferredTradeLocation;

	private Integer viewCount;
	private Boolean isLiked;
	private Boolean isOwner;

	private RelatedProductsDto relatedProducts;

	public static ProductDetailResponseDto fromEntity(Product product, boolean isProductLiked,
		List<RelatedProductDto> relatedProducts, boolean isOwner) {

		ProductDetailResponseDto response = ProductDetailResponseDto.builder()
			.productId(product.getId())
			.title(product.getTitle())
			.detail(product.getDescription())
			.sellerName(product.getSeller().getName())
			.tradeType(product.getTradeType())
			.imageUrls(product.getImages().stream()
				.map(ProductImage::getImageUrl)
				.toList())
			.stock(product.getStock())
			.priceTable(product.getPrices().stream()
				.filter(price -> price.getStatus() != PriceTableStatus.DEPRECATED)
				.map(price -> ProductPriceDto.builder()
					.quantity(price.getQuantity())
					.price(price.getPrice())
					.status(price.getStatus())
					.build())
				.toList())
			.preferredTradeLocation(
				TradeLocationDto.builder()
					.latitude(product.getLatitude())
					.longitude(product.getLongitude())
					.address(product.getAddress())
					.build()
			)
			.viewCount(product.getVisitCount())
			.isLiked(isProductLiked)
			.relatedProducts(RelatedProductsDto.fromEntity(relatedProducts))
			.isOwner(isOwner)
			.build();

		if (product.getCategory().getName().equals("기타")) {
			response.setCategory(ProductCategoryDto.builder()
				.mainCategoryId(product.getCategory().getId())
				.subCategoryId(null)
				.build());
			return response;
		}

		response.setCategory(ProductCategoryDto.builder()
			.mainCategoryId(product.getCategory().getParent().getId())
			.subCategoryId(product.getCategory().getId())
			.build());

		return response;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductCategoryDto {
		private Long mainCategoryId;
		private Long subCategoryId;
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

		public static RelatedProductsDto fromEntity(List<RelatedProductDto> relatedProductDtos) {

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
		private Integer price;
		private Integer quantity;
		private String province;
		private String city;
		private Integer viewCount;
		private Boolean isLiked;

		public static RelatedProductDto fromEntity(Product product, boolean isLiked) {

			ProductPrice minQuantityProdcutPrice = product.getPrices().stream()
				.min((p1, p2) -> Integer.compare(p1.getQuantity(), p2.getQuantity()))
				.orElse(null);

			String thumbnailUrl = product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl();

			return RelatedProductDto.builder()
				.productId(product.getId())
				.title(product.getTitle())
				.thumbnailUrl(thumbnailUrl)
				.isLiked(isLiked)
				.price(minQuantityProdcutPrice.getPrice())
				.quantity(minQuantityProdcutPrice.getQuantity())
				.province(product.getRegion() == null ? null : product.getRegion().getProvince())
				.city(product.getRegion() == null ? null : product.getRegion().getCity())
				.viewCount(product.getVisitCount())
				.build();
		}
	}
}
