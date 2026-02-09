package com.gachigage.member.controller;

import com.gachigage.member.dto.response.ProfileImageResponseDto;
import com.gachigage.member.dto.response.TradeResponseDto;
import com.gachigage.member.service.MypageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MypageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MypageService mypageService;

    @Test
    @DisplayName("프로필 이미지 변경 테스트 - 성공하면 200 OK")
    @WithMockUser(username = "12345678")
        // 가짜로 로그인한 척 (ID: 12345678)
    void updateProfileImageTest() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes()
        );


        ProfileImageResponseDto responseDto = ProfileImageResponseDto.builder()
                .imageUrl("https://s3.bucket/new-image.jpg")
                .build();

        // 서비스가 호출되면 이렇게 응답하라고 설정 (Stubbing)
        given(mypageService.updateProfileImage(eq(12345678L), any())).willReturn(responseDto);

        // when & then: API 찌르기
        mockMvc.perform(multipart("/users/me/profile-image")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print()) // 결과를 콘솔에 출력
                .andExpect(status().isOk()) // 200 OK인지 확인
                .andExpect(jsonPath("$.data.imageUrl").value("https://s3.bucket/new-image.jpg")); // 응답 값 확인
    }

    @Test
    @DisplayName("구매 내역 조회 테스트 - 성공하면 200 OK")
    @WithMockUser(username = "12345678")
    void getPurchaseHistoryTest() throws Exception {
        // given: 빈 페이지 객체 준비
        Page<TradeResponseDto> emptyPage = new PageImpl<>(List.of());

        // 서비스가 호출되면 빈 페이지를 리턴하도록 설정
        given(mypageService.getPurchaseHistory(eq(12345678L), any(Pageable.class)))
                .willReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/users/me/purchases")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray()); // 배열 형태인지 확인
    }

    @Test
    @DisplayName("판매 내역 조회 테스트 - 성공하면 200 OK")
    @WithMockUser(username = "12345678")
    void getSalesHistoryTest() throws Exception {
        // given
        Page<TradeResponseDto> emptyPage = new PageImpl<>(List.of());

        given(mypageService.getSalesHistory(eq(12345678L), any(Pageable.class)))
                .willReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/users/me/sales")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}

