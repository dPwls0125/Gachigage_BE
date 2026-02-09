package com.gachigage.chat.dto;

import com.gachigage.product.domain.ProductStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class ChatRoomResponseDto {

	private final Long chatRoomId;

	private final String sellerName;

	private final String sellerImageUrl;

	private final String buyerName;

	private final String buyerImageUrl;

	private final String productTitle;

	private final String productImageUrl;

	private final ProductStatus productStatus;

	private final int unreadCount;

	private final boolean amIBuyer;

	private final Long memberId;
}
