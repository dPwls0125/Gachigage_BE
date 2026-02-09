package com.gachigage.chat.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ChatRoomListResponseDto {
	private Long chatRoomId;
	private Long productId;
	private String otherName;
	private String otherProfileImage;
	private String lastMessage;
	private LocalDateTime lastMessageTime;
	private int unreadCount;
}
