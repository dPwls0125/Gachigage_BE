package com.gachigage.product.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import com.gachigage.chat.domain.ChatRoom;
import com.gachigage.global.common.BaseEntity;
import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.member.Member;
import com.gachigage.trade.domain.Trade;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id", nullable = false)
	private Member seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private ProductCategory category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_id")
	private Region region;

	@Column(name = "title", length = 100, nullable = false)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT", nullable = false)
	private String description;

	@Column(name = "stock", nullable = false)
	private Long stock;

	@Enumerated(EnumType.STRING)
	@Column(name = "trade_type", nullable = false)
	private TradeType tradeType;

	@ColumnDefault("0")
	@Column(name = "visit_count", nullable = false)
	private int visitCount;

	@ColumnDefault("0")
	@Column(name = "like_count", nullable = false)
	private int likeCount;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Column(name = "address", length = 255)
	private String address;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProductImage> images = new ArrayList<>();
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProductPrice> prices = new ArrayList<>();
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProductLike> likes = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Trade> trades = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<ChatRoom> chatRooms = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	@ColumnDefault("'SELLING'") // DB에 들어갈 기본값
	private ProductStatus status;

	private Product(Long id, Member seller, ProductCategory category, Region region,
		String title, String description, Long stock, TradeType tradeType,
		Double latitude, Double longitude, String address) {

		this.id = id;
		this.seller = seller;
		this.category = category;
		this.region = region;
		this.title = title;
		this.description = description;
		this.stock = stock;
		this.tradeType = tradeType;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
	}

	public static Product create(
		Long id,
		Member seller,
		ProductCategory category,
		Region region,
		String title,
		String description,
		Long stock,
		TradeType tradeType,
		Double latitude,
		Double longitude,
		String address,
		List<ProductPrice> prices,
		List<ProductImage> images
	) {

		if (latitude != null) {
			isValidLatitude(latitude);
		}

		if (longitude != null) {
			isValidLongitude(longitude);
		}

		validateCategory(category);
		validateImage(images);
		validatePriceTable(prices);

		Product product = new Product(
			id, seller, category, region,
			title, description, stock, tradeType,
			latitude, longitude, address
		);

		if (prices == null || prices.isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "상품은 최소 하나의 가격 정보를 가져야 합니다.");
		}

		if (images.size() > 8) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "상품 이미지는 최대 8개까지 등록할 수 있습니다.");
		}
		product.status = ProductStatus.SELLING;

		prices.forEach(product::addPrice);
		images.forEach(product::addImage);
		return product;
	}

	private static void isValidLongitude(Double coordinate) {
		if (coordinate < -180 || coordinate > 180) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경도는 -180에서 180 사이의 값이어야 합니다.");
		}
	}

	private static void isValidLatitude(Double coordinate) {
		if (coordinate < -90 || coordinate > 90) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "위도는 -90에서 90 사이의 값이어야 합니다.");
		}
	}

	private static void validatePriceTable(List<ProductPrice> priceTable) {
		if (priceTable == null || priceTable.isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "상품은 최소 하나의 가격 정보를 가져야 합니다.");
		}
		if (priceTable.size() > 5) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "상품 가격 정보는 최대 5개까지 등록할 수 있습니다.");
		}
	}

	private static void validateImage(List<ProductImage> images) {
		if (images.size() > 8) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "상품 이미지는 최대 8개까지 등록할 수 있습니다.");
		}
	}

	private static void validateCategory(ProductCategory category) {
		if (category != null && category.getParent() == null) {
			if (!category.getName().equals("기타")) {
				throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
					"대분류 카테고리는 기타 카테고리만 선택할 수 있습니다. 하위 카테고리를 입력해주세요.");
			}
		}
	}

	public List<ProductImage> getProductImages() {
		return images;
	}

	public void modify(
		ProductCategory category,
		String title,
		String description,
		Long stock,
		TradeType tradeType,
		Double latitude,
		Double longitude,
		String address,
		List<ProductPrice> newPrices,
		List<ProductImage> newImages,
		Region region
	) {
		this.category = category;
		this.title = title;
		this.description = description;
		this.stock = stock;
		this.tradeType = tradeType;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.region = region;
		changePrices(newPrices);
		changeImages(newImages);
	}

	private void changePrices(List<ProductPrice> newPrices) {
		this.prices.forEach(ProductPrice::deprecated);
		for (ProductPrice price : newPrices) {
			addPrice(price);
		}
	}

	private void changeImages(List<ProductImage> newImages) {
		this.images.clear();
		for (ProductImage image : newImages) {
			addImage(image);
		}
	}

	public void increaseVisitCount() {
		this.visitCount += 1;
	}

	public void incrementLikeCount() {
		this.likeCount += 1;
	}

	public void decrementLikeCount() {
		if (this.likeCount > 0) {
			this.likeCount -= 1;
		}
	}

	private void addPrice(ProductPrice price) {

		if (price == null) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "가격 정보이 비어있을 수 없습니다.");
		}

		if (price.getQuantity() <= 0 || price.getPrice() < 0) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "가격 정보의 수량과 가격은 0 이상이어야 합니다.");
		}

		if (price.getQuantity() > this.stock) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "가격 정보의 수량은 재고 수량을 초과할 수 없습니다.");
		}

		this.prices.add(price);
		price.setProduct(this);
	}

	private void addImage(ProductImage image) {
		this.images.add(image);
		image.setProduct(this);
	}

	public void deduceStock(Long quantity) {
		if (this.stock < quantity) {
			throw new CustomException(ErrorCode.INSUFFICIENT_STOCK, "재고가 부족합니다.");
		}
		this.stock -= quantity;
	}
}
