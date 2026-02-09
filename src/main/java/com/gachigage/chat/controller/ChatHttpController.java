package com.gachigage.chat.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gachigage.chat.dto.ChatMessageResponseDto;
import com.gachigage.chat.dto.ChatRoomCreateRequestDto;
import com.gachigage.chat.dto.ChatRoomCreateResponseDto;
import com.gachigage.chat.dto.ChatRoomListResponseDto;
import com.gachigage.chat.dto.ChatRoomResponseDto;
import com.gachigage.chat.service.ChatService;
import com.gachigage.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RequestMapping("/chats")
@RestController
@RequiredArgsConstructor
public class ChatHttpController {
	private final ChatService chatService;

	@Operation(summary = "채팅방 생성/입장", description = "상품 ID를 받아 채팅방을 생성하거나, 이미 존재하면 해당 방 번호를 반환합니다.")
	@PostMapping
	public ResponseEntity<ApiResponse<ChatRoomCreateResponseDto>> createOrGetChatRoom(
		@RequestBody ChatRoomCreateRequestDto requestDto,
		@Parameter(hidden = true) @AuthenticationPrincipal User user) {
		return ResponseEntity.ok(
			ApiResponse.success(chatService.createOrFindRoom(requestDto, Long.parseLong(user.getUsername()))));
	}

	@Operation(summary = "내 채팅방 일괄 조회 ", description = "유저 정보를 받아 해당 유저 채팅방을 일괄로 간략하게 조회한 리스트를 반환합니다.")
	@GetMapping("/rooms")
	public ResponseEntity<ApiResponse<List<ChatRoomListResponseDto>>> getMyRooms(
		@Parameter(hidden = true) @AuthenticationPrincipal User user) {
		return ResponseEntity.ok(ApiResponse.success(chatService.getMyChatRooms(Long.parseLong(user.getUsername()))));
	}

	@Operation(summary = "내 채팅방 조회 ", description = "유저 정보와 채팅방 id를 받아 해당 채팅방을 조회하여 반환합니다.")
	@GetMapping("/rooms/{chatRoomId}")
	public ResponseEntity<ApiResponse<ChatRoomResponseDto>> getRoom(
		@Parameter(hidden = true) @AuthenticationPrincipal User user, @PathVariable Long chatRoomId) {
		return ResponseEntity.ok(
			ApiResponse.success(chatService.getChatRoom(Long.parseLong(user.getUsername()), chatRoomId)));
	}

	@Operation(summary = "채팅방 메세지 상세 조회", description = "채팅방 ID를 받아 채팅방의 메세지들을 Slice 형태로 생성하고 반환합니다.")
	@GetMapping("/rooms/{chatRoomId}/messages")
	public ResponseEntity<ApiResponse<Slice<ChatMessageResponseDto>>> getMessages(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@PathVariable Long chatRoomId, @RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		return ResponseEntity.ok(
			ApiResponse.success(chatService.getChatMessages(chatRoomId, pageable, Long.parseLong(user.getUsername()))));
	}
}
