package com.gachigage.trade.domain;

import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductPrice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "trade_item")
public class TradeItem {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trade_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Trade trade;

	@JoinColumn(name = "product_price_id")
	@ManyToOne
	private ProductPrice productPrice;

	@JoinColumn(name = "product_id")
	@ManyToOne
	private Product product;

	@Column(name = "unit_price")
	private int priceSnapshot;

	@Column(name = "unit_quantity")
	private int quantitySnapshot;

	@Column(name = "total_price")
	private int totalPrice;
}

