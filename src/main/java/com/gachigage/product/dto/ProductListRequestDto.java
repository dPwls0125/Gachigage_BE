package com.gachigage.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ProductListRequestDto {

	private String query;
	private Long categoryId;
	private PriceArrangeDto priceArrange = new PriceArrangeDto();
	private LocationDto locationDto = new LocationDto();
	private String group;
	private Integer page;
	private Integer size;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Setter
	public static class PriceArrangeDto {
		private Integer minPrice;
		private Integer maxPrice;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Setter
	public static class LocationDto {
		private String province;
		private String city;
	}
}
