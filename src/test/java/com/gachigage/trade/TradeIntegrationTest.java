package com.gachigage.trade;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachigage.chat.domain.ChatRoom;
import com.gachigage.chat.repository.ChatRoomRepository;
import com.gachigage.global.WithMockCustomUser;
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
import com.gachigage.product.repository.ProductPriceRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;
import com.gachigage.trade.domain.Trade;
import com.gachigage.trade.dto.TradeRequestDto;
import com.gachigage.trade.repository.TradeItemRepository;
import com.gachigage.trade.repository.TradeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 각 테스트 후 롤백을 위해 사용
public class TradeIntegrationTest {

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
	private ProductPriceRepository productPriceRepository;

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@Autowired
	private TradeRepository tradeRepository;

	@Autowired
	private TradeItemRepository tradeItemRepository;

	private Member seller;
	private Member buyer;
	private ProductCategory subCategory;
	private Region region;
	private Product product;
	private ProductPrice productPrice;
	private ChatRoom chatRoom;

	@BeforeEach
	void setUp() {
		// 1. 판매자, 구매자 생성 및 저장
		seller = Member.builder()
			.email("seller@test.com")
			.name("판매자")
			.roleType(RoleType.USER)
			.birthDate(LocalDate.of(1990, 1, 1))
			.oauthId(100L)
			.build();
		memberRepository.save(seller);

		buyer = Member.builder()
			.email("buyer@test.com")
			.name("구매자")
			.roleType(RoleType.USER)
			.birthDate(LocalDate.of(1991, 2, 2))
			.oauthId(200L)
			.build();
		memberRepository.save(buyer);

		// 2. 카테고리, 지역 생성 및 저장
		ProductCategory mainCategory = ProductCategory.builder().name("식기류").build();
		productCategoryRepository.save(mainCategory);
		subCategory = ProductCategory.builder().name("컵").parent(mainCategory).build();
		productCategoryRepository.save(subCategory);

		region = Region.builder().province("서울특별시").city("강남구").district("역삼동").lawCode("1234567890").build();
		regionRepository.save(region);

		// 3. 상품 생성 및 저장 (재고 10)
		product = Product.create(null, seller, subCategory, region, "테스트 상품", "테스트 상품 상세 설명", 10L, TradeType.DIRECT,
			37.123456, 127.654321, "서울 강남구 역삼동",
			List.of(ProductPrice.builder().quantity(1).price(10000).status(PriceTableStatus.ACTIVE).build()),
			List.of(ProductImage.builder().imageUrl("http://localhost/image1.jpg").order(0).build()));
		productRepository.save(product);

		// 4. 상품 가격 테이블 저장
		productPrice = product.getPrices().get(0); // Product.create에서 생성된 ProductPrice를 가져옴

		// 5. 채팅방 생성 및 저장 (판매자-구매자, 상품 연결)
		chatRoom = ChatRoom.builder()
			.seller(seller)
			.buyer(buyer)
			.product(product)
			.build();
		chatRoomRepository.save(chatRoom);
	}

	@AfterEach
	void tearDown() {
		tradeItemRepository.deleteAllInBatch();
		tradeRepository.deleteAllInBatch();
		chatRoomRepository.deleteAllInBatch();
		productPriceRepository.deleteAllInBatch();
		productRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		productCategoryRepository.deleteAllInBatch();
		regionRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("거래 요청 통합 테스트 - 재고 충분, 성공")
	@WithMockCustomUser(oauthId = 200L)
		// 구매자 (buyer)로 인증
	void requestTrade_sufficientStock_success() throws Exception {
		// Given
		// 요청 수량: ProductPrice (단위 1, 가격 10000)을 5개 요청 -> 총 5개
		// 상품 재고: 10개 (setUp에서 설정) -> 충분
		int requestedSetQuantity = 5;
		TradeRequestDto.TradeSet tradeSet = new TradeRequestDto.TradeSet(productPrice.getId(), requestedSetQuantity);
		TradeRequestDto tradeRequestDto = new TradeRequestDto(chatRoom.getId(), List.of(tradeSet));

		// When
		mockMvc.perform(post("/trades/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tradeRequestDto))
				.with(user(String.valueOf(buyer.getOauthId())))) // 구매자로 인증
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."));

		// Then
		// Trade가 생성되었는지 확인
		assertThat(tradeRepository.count()).isEqualTo(1);
		Trade createdTrade = tradeRepository.findAll().get(0);
		assertThat(createdTrade.getProduct().getId()).isEqualTo(product.getId());
		assertThat(createdTrade.getChatRoom().getId()).isEqualTo(chatRoom.getId());

		// TradeItem이 생성되었는지 확인
		assertThat(tradeItemRepository.count()).isEqualTo(1);
	}

	@Test
	@DisplayName("거래 요청 통합 테스트 - 재고 부족, 실패")
	@WithMockCustomUser(oauthId = 200L)
		// 구매자 (buyer)로 인증
	void requestTrade_insufficientStock_failure() throws Exception {
		// Given
		// 요청 수량: ProductPrice (단위 1, 가격 10000)을 11개 요청 -> 총 11개
		// 상품 재고: 10개 (setUp에서 설정) -> 부족
		int requestedSetQuantity = 11;
		TradeRequestDto.TradeSet tradeSet = new TradeRequestDto.TradeSet(productPrice.getId(), requestedSetQuantity);
		TradeRequestDto tradeRequestDto = new TradeRequestDto(chatRoom.getId(), List.of(tradeSet));

		// When & Then
		mockMvc.perform(post("/trades/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tradeRequestDto))
				.with(user(String.valueOf(buyer.getOauthId())))) // 구매자로 인증
			.andDo(print())
			.andExpect(status().isBadRequest()) // 재고 부족 예외는 BAD_REQUEST로 처리
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.message").value("거래를 요청한 수량 보다, 재고가 부족합니다."));

		// Then (Trade, TradeItem이 생성되지 않았는지 확인)
		assertThat(tradeRepository.count()).isEqualTo(0);
		assertThat(tradeItemRepository.count()).isEqualTo(0);
	}

	@Test
	@DisplayName("거래 요청 통합 테스트 - 존재하지 않는 채팅방, 실패")
	@WithMockCustomUser(oauthId = 200L)
		// 구매자 (buyer)로 인증
	void requestTrade_invalidChatRoomId_failure() throws Exception {
		// Given
		Long nonExistentChatRoomId = 9999L; // 존재하지 않는 채팅방 ID
		int requestedSetQuantity = 1;
		TradeRequestDto.TradeSet tradeSet = new TradeRequestDto.TradeSet(productPrice.getId(), requestedSetQuantity);
		TradeRequestDto tradeRequestDto = new TradeRequestDto(nonExistentChatRoomId, List.of(tradeSet));

		// When & Then
		mockMvc.perform(post("/trades/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tradeRequestDto))
				.with(user(String.valueOf(buyer.getOauthId()))))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.message").value("존재하지 않는 채팅방 정보입니다."));

		// Then
		assertThat(tradeRepository.count()).isEqualTo(0);
		assertThat(tradeItemRepository.count()).isEqualTo(0);
	}

	@Test
	@DisplayName("거래 요청 통합 테스트 - 존재하지 않는 상품 가격 정보, 실패")
	@WithMockCustomUser(oauthId = 200L)
		// 구매자 (buyer)로 인증
	void requestTrade_invalidProductPriceId_failure() throws Exception {
		// Given
		Long nonExistentProductPriceId = 8888L; // 존재하지 않는 ProductPrice ID
		int requestedSetQuantity = 1;
		TradeRequestDto.TradeSet tradeSet = new TradeRequestDto.TradeSet(nonExistentProductPriceId,
			requestedSetQuantity);
		TradeRequestDto tradeRequestDto = new TradeRequestDto(chatRoom.getId(), List.of(tradeSet));

		// When & Then
		mockMvc.perform(post("/trades/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tradeRequestDto))
				.with(user(String.valueOf(buyer.getOauthId()))))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.message").value("존재하지 않는 가격 정보입니다."));

		// Then
		assertThat(tradeRepository.count()).isEqualTo(0);
		assertThat(tradeItemRepository.count()).isEqualTo(0);
	}

	@Test
	@DisplayName("거래 요청 통합 테스트 - 여러개의 가격 테이블과 재고 부족, 실패")
	@WithMockCustomUser(oauthId = 200L)
		// 구매자 (buyer)로 인증
	void requestTrade_multipleSetsInsufficientStock_failure() throws Exception {
		// Given
		// 새로운 ProductPrice 생성 (단위 2, 가격 20000)
		ProductPrice productPrice2 = ProductPrice.builder()
			.quantity(2)
			.price(20000)
			.status(PriceTableStatus.ACTIVE)
			.product(product)
			.build();
		productPriceRepository.save(productPrice2);
		product.getPrices().add(productPrice2); // product 엔티티에 추가

		// 첫 번째 요청: ProductPrice (단위 1) 5개 = 총 5개
		// 두 번째 요청: ProductPrice (단위 2) 3개 = 총 6개
		// 총 요청 수량: 5 + 6 = 11개
		// 상품 재고: 10개 -> 부족

		TradeRequestDto.TradeSet tradeSet1 = new TradeRequestDto.TradeSet(productPrice.getId(), 5);
		TradeRequestDto.TradeSet tradeSet2 = new TradeRequestDto.TradeSet(productPrice2.getId(), 3);
		TradeRequestDto tradeRequestDto = new TradeRequestDto(chatRoom.getId(), List.of(tradeSet1, tradeSet2));

		// When & Then
		mockMvc.perform(post("/trades/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tradeRequestDto))
				.with(user(String.valueOf(buyer.getOauthId()))))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.message").value("거래를 요청한 수량 보다, 재고가 부족합니다."));

		// Then
		assertThat(tradeRepository.count()).isEqualTo(0);
		assertThat(tradeItemRepository.count()).isEqualTo(0);
	}

}
