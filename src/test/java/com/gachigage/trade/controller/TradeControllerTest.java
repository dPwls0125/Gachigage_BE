package com.gachigage.trade.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachigage.chat.domain.ChatRoom;
import com.gachigage.chat.repository.ChatRoomRepository;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;
import com.gachigage.product.domain.PriceTableStatus;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.domain.ProductImage;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.domain.Region;
import com.gachigage.product.domain.TradeType;
import com.gachigage.product.repository.ProductCategoryRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;
import com.gachigage.trade.domain.Trade;
import com.gachigage.trade.domain.TradeStatus;
import com.gachigage.trade.dto.TradeRequestDto;
import com.gachigage.trade.repository.TradeItemRepository;
import com.gachigage.trade.repository.TradeRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeItemRepository tradeItemRepository;

    private Member seller;
    private Member buyer;
    private Product product;
    private ProductPrice productPrice;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        seller = memberRepository.save(Member.builder()
                .email("seller@test.com")
                .name("판매자")
                .roleType(RoleType.USER)
                .birthDate(LocalDate.of(1990, 1, 1))
                .oauthId(1001L)
                .build());

        buyer = memberRepository.save(Member.builder()
                .email("buyer@test.com")
                .name("구매자")
                .roleType(RoleType.USER)
                .birthDate(LocalDate.of(1992, 2, 2))
                .oauthId(1002L)
                .build());

        ProductCategory mainCategory = productCategoryRepository.save(ProductCategory.builder().name("기타").build());
        ProductCategory subCategory = productCategoryRepository
                .save(ProductCategory.builder().name("테스트 소분류").parent(mainCategory).build());
        Region region = regionRepository
                .save(Region.builder().province("서울특별시").city("강남구").district("역삼동").lawCode("1234567890").build());

        product = productRepository.save(Product.create(
                null,
                seller,
                subCategory,
                region,
                "거래 테스트 상품",
                "거래 테스트 상품 설명",
                30L,
                TradeType.DIRECT,
                37.5,
                127.0,
                "서울 강남구 역삼동",
                List.of(
                        ProductPrice.builder().quantity(2).price(1000).status(PriceTableStatus.ACTIVE).build(),
                        ProductPrice.builder().quantity(5).price(2200).status(PriceTableStatus.ACTIVE).build()),
                List.of(ProductImage.builder().imageUrl("https://example.com/product.jpg").order(0).build())));

        productPrice = product.getPrices().get(0);
        chatRoom = chatRoomRepository.save(ChatRoom.builder().seller(seller).buyer(buyer).product(product).build());
    }

    @AfterEach
    void tearDown() {
        tradeItemRepository.deleteAll();
        tradeRepository.deleteAll();
        chatRoomRepository.deleteAll();
        productRepository.deleteAll();
        regionRepository.deleteAll();
        productCategoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("거래 요청 API 통합 테스트")
    void requestTradeIntegrationTest() throws Exception {
        TradeRequestDto requestDto = new TradeRequestDto(
                chatRoom.getId(),
                List.of(new TradeRequestDto.TradeSet(productPrice.getId(), 3)));

        mockMvc.perform(post("/trades/request")
                        .with(user(buyer.getOauthId().toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.tradeId").isNumber());

        List<Trade> trades = tradeRepository.findAll();
        assertThat(trades).hasSize(1);
        assertThat(trades.get(0).getStatus()).isEqualTo(TradeStatus.ING);
        assertThat(trades.get(0).getTotalQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("거래 승인 API 통합 테스트")
    void approveTradeIntegrationTest() throws Exception {
        TradeRequestDto requestDto = new TradeRequestDto(
                chatRoom.getId(),
                List.of(new TradeRequestDto.TradeSet(productPrice.getId(), 2)));

        MvcResult requestResult = mockMvc.perform(post("/trades/request")
                        .with(user(buyer.getOauthId().toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode requestBody = objectMapper.readTree(requestResult.getResponse().getContentAsString());
        Long tradeId = requestBody.get("data").get("tradeId").asLong();

        mockMvc.perform(post("/trades/confirm/{tradeId}", tradeId)
                        .with(user(seller.getOauthId().toString())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."));

        Trade approvedTrade = tradeRepository.findById(tradeId).orElseThrow();
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();

        assertThat(approvedTrade.getStatus()).isEqualTo(TradeStatus.DONE);
        assertThat(updatedProduct.getStock()).isEqualTo(26L);
    }
}
