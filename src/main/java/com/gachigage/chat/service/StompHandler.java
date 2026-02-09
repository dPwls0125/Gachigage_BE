package com.gachigage.chat.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.gachigage.chat.repository.ChatRoomRepository;
import com.gachigage.global.config.JwtProvider;
import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {
	private final JwtProvider jwtProvider;
	private final ChatRoomRepository chatRoomRepository;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		try {
			StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

			if (StompCommand.CONNECT.equals(accessor.getCommand())) {
				String token = resolveToken(accessor.getFirstNativeHeader("Authorization"));

				if (token != null && jwtProvider.validateToken(token)) {
					Authentication authentication = jwtProvider.getAuthentication(token);
					accessor.setUser(authentication);
					accessor.getSessionAttributes().put("AUTH", authentication);
					log.info("CONNECT user={}", authentication.getName());
				} else {
					throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
				}

				log.info("STOMP 연결 성공: 유효한 사용자");
			} else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
				log.info("destination={}", accessor.getDestination());
				log.info("authentication={}", accessor.getUser());
				Authentication authentication = (Authentication)accessor.getSessionAttributes().get("AUTH");
				if (authentication == null) {
					log.warn("user is null");
					throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
				}

				Long memberOauthId = Long.parseLong(authentication.getName());
				log.warn("memberOauthId={}", memberOauthId);
				String destination = accessor.getDestination();
				Long chatRoomId = parseRoomIdFromDestination(destination);

				if (!chatRoomRepository.existsMemberInRoom(chatRoomId, memberOauthId)) {
					log.warn("권한 없는 채팅방 구독 시도: 사용자={}, 방={}", memberOauthId, chatRoomId);
					throw new CustomException(ErrorCode.UNAUTHORIZED, "해당 채팅방에 접근 권한이 없습니다.");
				}

				log.info("STOMP 구독 허용: 사용자={}, 방={}", memberOauthId, chatRoomId);
			} else if (StompCommand.SEND.equals(accessor.getCommand())) {
				log.info("SEND 입니다");
			}

			return message;
		} catch (Exception e) {
			log.error("stompError", e);
			throw e;
		}
	}

	private String resolveToken(String bearerToken) {
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	private Long parseRoomIdFromDestination(String destination) {
		try {
			String[] parts = destination.split("/");
			return Long.parseLong(parts[parts.length - 1]);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "올바르지 않은 구독 경로입니다.");
		}
	}
}
