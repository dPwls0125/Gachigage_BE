package com.gachigage.trade.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gachigage.global.ApiResponse;
import com.gachigage.trade.domain.Trade;
import com.gachigage.trade.dto.ProductPricesInfoResponse;
import com.gachigage.trade.dto.TradeRequestDto;
import com.gachigage.trade.dto.TradeResponseDto;
import com.gachigage.trade.service.TradeService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

	private final TradeService tradeService;

	@GetMapping("/{chatRoomId}")
	@Operation(description = "채팅방에서 거래중인 상품의 가격 리스트를 제공")
	public ResponseEntity<ApiResponse<ProductPricesInfoResponse>> getProductPricesInfo(@PathVariable Long chatRoomId) {
		ProductPricesInfoResponse response = tradeService.getProductInfo(chatRoomId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/request")
	@Operation(description = "거래자가 거래 가격에 대한 set와 함께 거래 요청")
	public ResponseEntity<ApiResponse<TradeResponseDto>> reqeustTrade(@RequestBody TradeRequestDto tradeRequest) {
		Trade trade = tradeService.requestTrade(tradeRequest);
		return ResponseEntity.ok(ApiResponse.success(new TradeResponseDto(trade.getId())));
	}

	@PostMapping("/confirm/{tradeId}")
	@Operation(description = "거래 완료 처리, 실제 stock 감소")
	public ResponseEntity<ApiResponse<Void>> approveTrade(@PathVariable Long tradeId) {
		tradeService.approveTrade(tradeId);
		return ResponseEntity.ok(ApiResponse.success());
	}

}
