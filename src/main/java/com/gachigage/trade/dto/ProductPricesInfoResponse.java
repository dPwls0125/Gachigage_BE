package com.gachigage.trade.dto;

import java.util.List;

import com.gachigage.product.domain.ProductPrice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductPricesInfoResponse {
	Long stock;
	List<QuantityPriceSet> productPriceList;

	@SuppressWarnings("checkstyle:WhitespaceAfter")
	public ProductPricesInfoResponse(Long stock, List<ProductPrice> productPriceList) {
		this.stock = stock;
		this.productPriceList = productPriceList.stream()
			.map(p -> new QuantityPriceSet(p.getId(), p.getQuantity(), p.getPrice())).toList();
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Setter
	public static class QuantityPriceSet {
		Long id;
		Integer quantity;
		Integer price;
	}
}
