package com.gachigage.trade.service;

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
import com.gachigage.trade.domain.Trade;
import com.gachigage.trade.domain.TradeItem;
import com.gachigage.trade.domain.TradeStatus;
import com.gachigage.trade.dto.ProductPricesInfoResponse;
import com.gachigage.trade.dto.TradeRequestDto;
import com.gachigage.trade.repository.TradeItemRepository;
import com.gachigage.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeService {
	private final TradeRepository tradeRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ProductPriceRepository productPriceRepository;
	private final TradeItemRepository tradeItemRepository;

	public ProductPricesInfoResponse getProductInfo(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 채팅방 정보입니다."));

		Product product = chatRoom.getProduct();
		List<ProductPrice> prices = product.getPrices().stream()
			.filter(price -> price.getStatus() == PriceTableStatus.ACTIVE).toList();
		return new ProductPricesInfoResponse(product.getStock(), prices);
	}

	@Transactional
	public Trade requestTrade(TradeRequestDto tradeRequestDto) {
		ChatRoom chatRoom = chatRoomRepository.findById(tradeRequestDto.getChatRoomId())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 채팅방 정보입니다."));

		Product tradeWantedProduct = chatRoom.getProduct();

		int totalRequestedQuantity = 0;

		Trade reqeustedTrade = generateTrade(chatRoom);

		for (TradeRequestDto.TradeSet set : tradeRequestDto.getTradeSets()) {

			long priceId = set.getProductPriceId();
			ProductPrice productPrice = productPriceRepository.findById(priceId)
				.orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 가격 정보입니다."));


			int setQuantity = set.getQuantity();
			totalRequestedQuantity += productPrice.getQuantity() * setQuantity;

			validateStockAndRequestedQuantity(tradeWantedProduct, totalRequestedQuantity);

			int unitPrice = productPrice.getPrice();
			TradeItem tradeItem = TradeItem.builder()
				.trade(reqeustedTrade)
				.productPrice(productPrice)
				.priceSnapshot(productPrice.getPrice())
				.quantitySnapshot(productPrice.getQuantity())
				.totalPrice(unitPrice * setQuantity)
				.build();

			tradeItemRepository.save(tradeItem);
		}

		return reqeustedTrade;

	}

	private void validateStockAndRequestedQuantity(Product tradeWantedProduct, int totalRequestedQuantity) {
		if (totalRequestedQuantity > tradeWantedProduct.getStock()) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "거래를 요청한 수량 보다, 재고가 부족합니다.");
		}
	}

	private Trade generateTrade(ChatRoom chatRoom) {
		Trade reqeustedTrade = Trade.builder()
			.status(TradeStatus.ING)
			.product(chatRoom.getProduct())
			.chatRoom(chatRoom)
			.build();

		tradeRepository.save(reqeustedTrade);
		return reqeustedTrade;
	}

}
