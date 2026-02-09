package com.gachigage.chat.dto;

import java.time.LocalDateTime;

import com.gachigage.chat.domain.ChatMessage;
import com.gachigage.chat.domain.ChatMessageType;
import com.gachigage.member.Member;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class ChatMessageResponseDto {

	private final Long chatRoomId;

	private final String content;

	private final boolean isMe;

	private final boolean senderIsBuyer;

	private final ChatMessageType messageType;

	private final LocalDateTime sendAt;

	private final boolean isRead;

	private final Long senderId;

	private final String messageUuid;

	public static ChatMessageResponseDto from(ChatMessage chatMessage, Long userOauthId) {
		Member sender = chatMessage.getSender();
		return ChatMessageResponseDto.builder()
			.chatRoomId(chatMessage.getChatRoom().getId())
			.content(chatMessage.getContent())
			.sendAt(chatMessage.getCreatedAt())
			.isMe(sender.getOauthId().equals(userOauthId))
			.senderIsBuyer(sender.equals(chatMessage.getChatRoom().getBuyer()))
			.messageType(chatMessage.getMessageType())
			.senderId(sender.getId())
			.messageUuid(chatMessage.getMessageUuid())
			.build();
	}
}
