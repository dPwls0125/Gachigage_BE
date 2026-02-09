package com.gachigage.member.dto.response;

import java.time.LocalDateTime;

import com.gachigage.trade.domain.TradeStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeResponseDto {
	private Long tradeId;
	private Long productId;
	private String title;
	private int price;
	private String thumbnailUrl;
	private LocalDateTime tradeDate;
	private String status;
	private int quantity;
}
