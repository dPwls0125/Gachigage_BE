package com.gachigage.member.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gachigage.global.ApiResponse;
import com.gachigage.member.dto.request.NicknameUpdateRequestDto;
import com.gachigage.member.dto.response.MyProfileResponseDto;
import com.gachigage.member.dto.response.ProfileImageResponseDto;
import com.gachigage.member.dto.response.TradeResponseDto;
import com.gachigage.member.service.MypageService;
import com.gachigage.product.dto.ProductListResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MypageController {
	private final MypageService mypageService;

	@GetMapping
	public ResponseEntity<ApiResponse<MyProfileResponseDto>> getMyProfile(@AuthenticationPrincipal UserDetails user) {
		Long oauthId = Long.valueOf(user.getUsername());
		MyProfileResponseDto response = mypageService.getMyProfile(oauthId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/nickname")
	public ResponseEntity<ApiResponse<Void>> updateNickname(@AuthenticationPrincipal UserDetails user,
		@Valid @RequestBody NicknameUpdateRequestDto request) {
		Long oauthId = Long.valueOf(user.getUsername());
		mypageService.updateNickname(oauthId, request.getNickname());

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	@PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ProfileImageResponseDto>> updateProfileImage(
		@AuthenticationPrincipal UserDetails user,
		@RequestPart(value = "file") MultipartFile file) {

		Long oauthId = Long.valueOf(user.getUsername());

		ProfileImageResponseDto response = mypageService.updateProfileImage(oauthId, file);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/purchases")
	public ResponseEntity<ApiResponse<Page<TradeResponseDto>>> getPurchaseHistory(
		@AuthenticationPrincipal UserDetails user,
		@PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Long oauthId = Long.valueOf(user.getUsername());

		Page<TradeResponseDto> response = mypageService.getPurchaseHistory(oauthId, pageable);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/sales")
	public ResponseEntity<ApiResponse<Page<ProductListResponseDto>>> getMyProducts(
		@AuthenticationPrincipal UserDetails user,
		@PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Long oauthId = Long.valueOf(user.getUsername());

		Page<ProductListResponseDto> response = mypageService.getMySalesProducts(oauthId, pageable);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/wishlist")
	public ResponseEntity<ApiResponse<Page<ProductListResponseDto>>> getMyWishlist(
		@AuthenticationPrincipal UserDetails user,
		@PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

		Long oauthId = Long.valueOf(user.getUsername());

		Page<ProductListResponseDto> response = mypageService.getMyLikes(oauthId, pageable);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
