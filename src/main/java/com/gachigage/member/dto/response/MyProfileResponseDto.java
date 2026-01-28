package com.gachigage.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyProfileResponseDto {

    private Long userId;
    private String name;
    private String nickname;
    private String profileImage;

}