package com.gachigage.member.controller;

import com.gachigage.global.ApiResponse;
import com.gachigage.member.dto.request.NicknameUpdateRequestDto;
import com.gachigage.member.dto.response.MyProfileResponseDto;
import com.gachigage.member.service.MypageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/me")
@RequiredArgsConstructor
public class MypageController {
    private final MypageService mypageService;


    @GetMapping
    public ApiResponse<MyProfileResponseDto> getMyProfile(@AuthenticationPrincipal UserDetails user) {
        Long oauthId = Long.valueOf(user.getUsername());
        MyProfileResponseDto response = mypageService.getMyProfile(oauthId);


        return ApiResponse.success(response);
    }

    @PutMapping("/nickname")
    public ApiResponse<Void> updateNickname(@AuthenticationPrincipal UserDetails user,
                                            @Valid @RequestBody NicknameUpdateRequestDto request) {
        Long oauthId = Long.valueOf(user.getUsername());
        mypageService.updateNickname(oauthId, request.getNickname());


        return ApiResponse.success(null);
    }

}
