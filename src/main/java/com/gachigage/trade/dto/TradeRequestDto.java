package com.gachigage.trade.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequestDto {

	private Long chatRoomId;
	private List<TradeSet> tradeSets;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TradeSet {
		private Long productPriceId;
		private int quantity;
	}
}
