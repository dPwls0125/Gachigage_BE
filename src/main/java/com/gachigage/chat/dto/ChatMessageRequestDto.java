package com.gachigage.chat.dto;

import com.gachigage.chat.domain.ChatMessageType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ChatMessageRequestDto {

	private Long chatRoomId;

	private String senderNickname;

	private String content;

	private ChatMessageType messageType;

	private String sendTime;

	private String messageUuid;
}
