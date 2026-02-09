package com.gachigage.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class ChatRoomCreateResponseDto {

	@Schema(description = "채팅방 ID", example = "1")
	private final Long chatRoomId;

	private final String sellerNickname;

	private final String sellerImageUrl;

	private final String buyerNickname;

	private final String buyerImageUrl;
	
	private final Long productId;
}
