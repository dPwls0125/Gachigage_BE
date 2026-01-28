package com.gachigage.member.service;


import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.dto.response.MyProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final MemberRepository memberRepository;


    public MyProfileResponseDto getMyProfile(Long oauthId){

        Member member = memberRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return MyProfileResponseDto.builder()
                .userId(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .profileImage(member.getImageUrl())
                .build();
    }

    @Transactional
    public void updateNickname(Long oauthId, String newNickname) {




        Member member = memberRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        member.updateNickname(newNickname);
    }
}
