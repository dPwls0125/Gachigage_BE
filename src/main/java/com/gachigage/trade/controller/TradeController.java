package com.gachigage.trade.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gachigage.global.ApiResponse;
import com.gachigage.trade.dto.ProductPricesInfoResponse;
import com.gachigage.trade.dto.TradeRequest;
import com.gachigage.trade.service.TradeService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

	private final TradeService tradeService;

	@GetMapping("{chatRoomId}")
	@Transactional
	@Operation(description = "채팅방에서 거래중인 상품의 가격 리스트를 제공")
	public ResponseEntity<ApiResponse<ProductPricesInfoResponse>> getProductPricesInfo(@PathVariable Long chatRoomId) {
		ProductPricesInfoResponse response = tradeService.getProductInfo(chatRoomId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/{chatRoomId}")
	@Operation(description = "채팅방에서 거래중인 상품 거래 확정, 현재는 바로 거래 확정이 되는 상황")
	public ResponseEntity<ApiResponse<Void>> registerTradeInfo(@PathVariable Long chatRoomId,
		@RequestBody TradeRequest request) {
		tradeService.createTrade(chatRoomId, request.getProductPriceId());
		return ResponseEntity.ok(ApiResponse.success());
	}

}
