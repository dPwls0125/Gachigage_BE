package com.gachigage.product.domain;

public enum TradeType {
	DIRECT("직거래"), DELIVERY("택배거래"), ALL("전체");
	private String name;

	TradeType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
