package com.gachigage.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatRoomCreateRequestDto {

	@Schema(description = "상품 ID", example = "15")
	private Long productId;
}
