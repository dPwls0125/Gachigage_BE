package com.gachigage.product.dto;

import java.util.List;

import com.gachigage.product.domain.PriceTableStatus;
import com.gachigage.product.domain.TradeType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRegistrationRequestDto {
	private Long categoryId;
	private String title;
	private String detail;
	private Long stock;
	private List<ProductPriceRegistrationDto> priceTable;
	private TradeType tradeType;
	private TradeLocationRegistrationDto preferredTradeLocations;
	private List<String> imageUrls;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductPriceRegistrationDto {
		private Integer quantity;
		private Integer price;
		private PriceTableStatus status;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TradeLocationRegistrationDto {
		private Double latitude;
		private Double longitude;
		private String address;
	}
}
