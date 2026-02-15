package com.gachigage.trade.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gachigage.chat.domain.ChatRoom;
import com.gachigage.chat.repository.ChatRoomRepository;
import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.product.domain.PriceTableStatus;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.repository.ProductPriceRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.trade.domain.Trade;
import com.gachigage.trade.domain.TradeStatus;
import com.gachigage.trade.dto.ProductPricesInfoResponse;
import com.gachigage.trade.dto.TradeRequestDto;
import com.gachigage.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeService {
	private final TradeRepository tradeRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ProductPriceRepository productPriceRepository;
	private final ProductRepository productRepository;

	@Transactional
	public Trade createTrade(Long chatRoomId, Long productPriceId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 채팅방입니다."));

		Product tradeProduct = chatRoom.getProduct();
		ProductPrice productPrice = productPriceRepository
			.findById(productPriceId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 상품 가격 정보입니다."));

		tradeProduct.deduceStock((long)productPrice.getQuantity());

		Trade trade = Trade.builder()
			.seller(chatRoom.getSeller())
			.buyer(chatRoom.getBuyer())
			.product(tradeProduct)
			.productPrice(productPrice)
			.status(TradeStatus.DONE)
			.build();

		return tradeRepository.save(trade);
	}

	public ProductPricesInfoResponse getProductInfo(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 채팅방 정보입니다."));

		Product product = chatRoom.getProduct();
		List<ProductPrice> prices = product.getPrices().stream()
			.filter(price -> price.getStatus() == PriceTableStatus.ACTIVE).toList();
		return new ProductPricesInfoResponse(product.getStock(), prices);
	}

	public void requestTrade(TradeRequestDto tradeRequestDto) {
		ChatRoom chatRoom = chatRoomRepository.findById(tradeRequestDto.getChatRoomId())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 채팅방 정보입니다."));

		List<ProductPrice> prices = new ArrayList<>();

		int total = 0;

		for (TradeRequestDto.TradeSet set : tradeRequestDto.getTradeSets()) {
			long priceId = set.getProductPriceId();
			int setQuantity = set.getQuantity();

			ProductPrice productPrice = productPriceRepository.findById(priceId)
				.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 가격 정보입니다."));

			prices.add(productPrice);
			total += productPrice.getQuantity() * setQuantity;
		}

		// Trade reqeustedTrade = Trade.builder()
		// 	.status(TradeStatus.ING)
		// 	.
		// 	.build();

	}

}
