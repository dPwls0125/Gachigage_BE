package com.gachigage.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.gachigage.chat.dto.ChatMessageRequestDto;
import com.gachigage.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

	private final ChatService chatService;

	@MessageMapping("/chat/message")
	public void message(ChatMessageRequestDto messageDto, SimpMessageHeaderAccessor accessor) {
		Authentication authentication = (Authentication)accessor.getSessionAttributes().get("AUTH");
		Long memberOauthId = Long.parseLong(authentication.getName());
		chatService.processMessage(messageDto, memberOauthId);
	}
}
