package com.gachigage.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SellerProfileResponseDto {
	private Long userId;
	private String nickname;
	private String profileImage;
}
