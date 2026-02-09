package com.gachigage.member.service;

import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.dto.response.MyProfileResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MypageServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MypageService mypageService;

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyProfile_Success() {
        Long oauthId = 12345L;
        Member mockMember = Member.builder()
                .id(1L)
                .oauthId(oauthId)
                .name("테스트유저")
                .nickname("기존닉네임")
                .email("test@test.com")
                .roleType(com.gachigage.member.RoleType.USER)
                .build();

        given(memberRepository.findMemberByOauthId(oauthId)).willReturn(Optional.of(mockMember));

        MyProfileResponseDto result = mypageService.getMyProfile(oauthId);

        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getNickname()).isEqualTo("기존닉네임");
    }

    @Test
    @DisplayName("내 정보 조회 실패")
    void getMyProfile_Fail() {

        Long oauthId = 99999L;
        given(memberRepository.findMemberByOauthId(oauthId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mypageService.getMyProfile(oauthId))
                .isInstanceOf(CustomException.class) //
                .extracting("errorCode") //
                .isEqualTo(ErrorCode.USER_NOT_FOUND); //
    }

    @Test
    @DisplayName("닉네임 수정 성공")
    void updateNickname_Success() {

        Long oauthId = 12345L;
        String newNickname = "새로운닉네임";
        Member mockMember = Member.builder()
                .oauthId(oauthId)
                .nickname("기존닉네임")
                .build();
        

        given(memberRepository.findMemberByOauthId(oauthId)).willReturn(Optional.of(mockMember));

        mypageService.updateNickname(oauthId, newNickname);

        assertThat(mockMember.getNickname()).isEqualTo(newNickname);
    }


}