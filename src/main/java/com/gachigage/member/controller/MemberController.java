package com.gachigage.member.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gachigage.global.ApiResponse;
import com.gachigage.member.dto.response.ProfileImageResponseDto;
import com.gachigage.member.dto.response.SellerProfileResponseDto;
import com.gachigage.member.dto.response.TradeResponseDto;
import com.gachigage.member.service.MemberService;
import com.gachigage.member.service.MypageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final MypageService mypageService;

	@GetMapping("/{memberId}/profile")
	public ResponseEntity<ApiResponse<SellerProfileResponseDto>> getSellerProfile(@PathVariable Long memberId) {
		SellerProfileResponseDto response = memberService.getSellerProfile(memberId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{memberId}/products")
	public ResponseEntity<ApiResponse<Page<TradeResponseDto>>> getSellerProducts(
		@PathVariable Long memberId,
		@RequestParam(required = false) String status,
		Pageable pageable) {

		Page<TradeResponseDto> response = memberService.getSellerProducts(memberId, status, pageable);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/user/image")
	public ResponseEntity<ApiResponse<ProfileImageResponseDto>> registerProfileImage(
		@AuthenticationPrincipal UserDetails user,
		@RequestPart("file") MultipartFile file) {

		Long oauthId = Long.valueOf(user.getUsername());
		return ResponseEntity.ok(ApiResponse.success(mypageService.updateProfileImage(oauthId, file)));
	}
}
