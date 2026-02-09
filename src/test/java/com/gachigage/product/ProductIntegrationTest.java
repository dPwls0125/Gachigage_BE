package com.gachigage.product;

import com.gachigage.global.WithMockCustomUser;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.gachigage.product.repository.ProductImageRepository;
import com.gachigage.product.repository.ProductLikeRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@Autowired
	private RegionRepository regionRepository;

	@Autowired
	private ProductImageRepository productImageRepository;

	@Autowired
	private ProductLikeRepository productLikeRepository;

	private Member testMember;

	private ProductCategory mainCategory;

	private ProductCategory subCategory;

	private Region testRegion;

	private Product testProduct;

	@BeforeEach
	void setUp() {

		// 1. Member 생성 및 저장

		testMember = Member.builder()
				.email("integration@test.com")
				.name("통합테스터")
				.roleType(RoleType.USER)
				.birthDate(LocalDate.of(1990, 1, 1))
				.oauthId(111L) // OAuth ID 추가
				.build();

		memberRepository.save(testMember);

		// 2. ProductCategory 생성 및 저장

		mainCategory = ProductCategory.builder().name("식기류").build();
		productCategoryRepository.save(mainCategory);
		subCategory = ProductCategory.builder().name("컵").parent(mainCategory).build();
		productCategoryRepository.save(subCategory);

		// 3. Region 생성 및 저장

		testRegion = Region.builder().province("서울특별시").city("강남구").district("역삼동").lawCode("1234567890").build();
		regionRepository.save(testRegion);

		// 4. Product 생성 및 저장

		testProduct = Product.create(null, // ID는 자동 생성
				testMember, subCategory, testRegion, "테스트 상품 제목", "테스트 상품 상세 설명", 10L, TradeType.DIRECT, 37.123456,
				127.654321, "서울 강남구 역삼동",
				List.of(ProductPrice.builder().quantity(1).price(10000).status(PriceTableStatus.ACTIVE).build(),
						ProductPrice.builder().quantity(5).price(45000).status(PriceTableStatus.ACTIVE).build()),
				List.of(ProductImage.builder().imageUrl("http://localhost/image1.jpg").order(0).build(),
						ProductImage.builder().imageUrl("http://localhost/image2.jpg").order(1).build()));

		productRepository.save(testProduct);

		// 추가 상품 1: 여러 개의 ACTIVE PriceTable을 가진 상품
		List<ProductPrice> pricesProduct1 = List.of(
			ProductPrice.builder().quantity(1).price(1000).status(PriceTableStatus.ACTIVE).build(),
			ProductPrice.builder().quantity(2).price(1800).status(PriceTableStatus.ACTIVE).build()
		);
		Product additionalProduct1 = Product.create(null, testMember, subCategory, testRegion,
			"추가 상품 1", "설명 1", 5L, TradeType.DELIVERY, 37.0, 127.0, "서울 강남구", pricesProduct1,
			List.of(ProductImage.builder().imageUrl("http://localhost/image_add1.jpg").build()));
		productRepository.save(additionalProduct1);

		// 추가 상품 2: ACTIVE와 INACTIVE PriceTable을 모두 가진 상품
		List<ProductPrice> pricesProduct2 = List.of(
			ProductPrice.builder().quantity(1).price(2000).status(PriceTableStatus.ACTIVE).build(),
			ProductPrice.builder().quantity(3).price(5000).status(PriceTableStatus.INACTIVE).build()
		);
		Product additionalProduct2 = Product.create(null, testMember, subCategory, testRegion,
			"추가 상품 2", "설명 2", 10L, TradeType.DIRECT, 37.1, 127.1, "서울 강남구", pricesProduct2,
			List.of(ProductImage.builder().imageUrl("http://localhost/image_add2.jpg").build()));
		productRepository.save(additionalProduct2);

		// 추가 상품 3: 단일 ACTIVE PriceTable을 가진 상품
		List<ProductPrice> pricesProduct3 = List.of(
			ProductPrice.builder().quantity(1).price(3000).status(PriceTableStatus.ACTIVE).build()
		);
		Product additionalProduct3 = Product.create(null, testMember, subCategory, testRegion,
			"추가 상품 3", "설명 3", 7L, TradeType.DELIVERY, 37.2, 127.2, "서울 강남구", pricesProduct3,
			List.of(ProductImage.builder().imageUrl("http://localhost/image_add3.jpg").build()));
		productRepository.save(additionalProduct3);
	}

	@Test
	@DisplayName("상품 상세 조회 통합 테스트")
	void getProductDetailIntegrationTest() throws Exception {

		// given

		Long productId = testProduct.getId();

		// when & then

		mockMvc.perform(get("/products/{productId}", productId).contentType(MediaType.APPLICATION_JSON))

				.andDo(print())

				.andExpect(status().isOk())

				.andExpect(jsonPath("$.status").value(200))

				.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."))

				.andExpect(jsonPath("$.data.productId").value(productId))

				.andExpect(jsonPath("$.data.title").value("테스트 상품 제목"))

				.andExpect(jsonPath("$.data.detail").value("테스트 상품 상세 설명"))

				.andExpect(jsonPath("$.data.sellerName").value(testMember.getName()))

				.andExpect(jsonPath("$.data.category.mainCategoryId").value(mainCategory.getId()))

				.andExpect(jsonPath("$.data.category.subCategoryId").value(subCategory.getId()))

				.andExpect(jsonPath("$.data.tradeType").value(TradeType.DIRECT.name()))

				.andExpect(jsonPath("$.data.imageUrls[0]").value("http://localhost/image1.jpg"))

				.andExpect(jsonPath("$.data.imageUrls[1]").value("http://localhost/image2.jpg"))

				.andExpect(jsonPath("$.data.stock").value(10L))

				.andExpect(jsonPath("$.data.priceTable[0].quantity").value(1))

				.andExpect(jsonPath("$.data.priceTable[0].price").value(10000))

				.andExpect(jsonPath("$.data.priceTable[0].status").value(PriceTableStatus.ACTIVE.name()))

				.andExpect(jsonPath("$.data.priceTable[1].quantity").value(5))

				.andExpect(jsonPath("$.data.priceTable[1].price").value(45000))

				.andExpect(jsonPath("$.data.priceTable[1].status").value(PriceTableStatus.ACTIVE.name()))

				.andExpect(jsonPath("$.data.preferredTradeLocation.latitude").value(37.123456))

				.andExpect(jsonPath("$.data.preferredTradeLocation.longitude").value(127.654321))

				.andExpect(jsonPath("$.data.preferredTradeLocation.address").value("서울 강남구 역삼동"))

				.andExpect(jsonPath("$.data.viewCount").value(1))

				.andExpect(jsonPath("$.data.isLiked").value(false))

				.andExpect(jsonPath("$.data.relatedProducts.size").value(3));

	}

	@Test
	@DisplayName("상품 삭제 통합 테스트 - 성공 (소유자)")
	void deleteProductSuccessIntegrationTest() throws Exception {

		// given
		Long productId = testProduct.getId();
		String authenticatedUserId = testMember.getOauthId().toString();

		// when
		mockMvc.perform(delete("/products/{productId}", productId)
						.with(user(authenticatedUserId)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."));

		// then
		Optional<Product> foundProduct = productRepository.findById(productId);
		assertThat(foundProduct).isEmpty();
	}

	@Test
	@DisplayName("상품 삭제 통합 테스트 - 실패 (비로그인 사용자)")
	void deleteProductUnauthenticatedIntegrationTest() throws Exception {

		// given
		Long productId = testProduct.getId();

		// when & then
		mockMvc.perform(delete("/products/{productId}", productId))
				.andDo(print())
				.andExpect(status().isUnauthorized());

	}

	@Test
	@DisplayName("상품 좋아요 성공")
	void likeProductSuccessIntegrationTest() throws Exception {

		// given
		Long productId = testProduct.getId();
		String authenticatedUserId = testMember.getOauthId().toString();

		// when & then
		mockMvc.perform(post("/products/{productId}/like", productId)
						.with(user(authenticatedUserId)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."));

	}

	@Test
	@DisplayName("상품 좋아요 취소")
	void unlikeProductSuccessIntegrationTest() throws Exception {

		// given
		Long productId = testProduct.getId();
		String authenticatedUserId = testMember.getOauthId().toString();

		// 먼저 좋아요를 누름
		mockMvc.perform(post("/products/{productId}/like", productId)
						.with(user(authenticatedUserId)))
				.andExpect(status().isOk());

		// when & then
		mockMvc.perform(post("/products/{productId}/like", productId)
						.with(user(authenticatedUserId)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."));

	}

	@Test
	@DisplayName("상품 삭제 통합 테스트 - 실패 (권한 없는 사용자)")
	void deleteProductUnauthorizedIntegrationTest() throws Exception {

		// given

		Long productId = testProduct.getId();

		Member unauthorizedMember = Member.builder()
				.email("unauthorized@test.com")
				.name("권한없음")
				.roleType(RoleType.USER)
				.birthDate(LocalDate.of(1991, 2, 2))
				.oauthId(222L)
				.build();

		memberRepository.save(unauthorizedMember);
		String unauthorizedUserId = unauthorizedMember.getOauthId().toString();

		// when & then
		mockMvc.perform(delete("/products/{productId}", productId)
						.with(user(unauthorizedUserId)))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("상품 삭제 권한이 없는 사용자입니다.")); // CustomException 메시지 확인

	}

	@AfterEach
	void tearDown() {
		productLikeRepository.deleteAll();
		productRepository.deleteAll();
		memberRepository.deleteAll();
		productCategoryRepository.deleteAll();
		regionRepository.deleteAll();
		productImageRepository.deleteAll();
	}

	@Test
	@DisplayName("상품 리스트 조회 통합 테스트 - 페이지네이션 및 가격 테이블 상태 확인")
	@WithMockCustomUser(oauthId = 111L) // testMember의 oauthId와 동일
	void testProductListRetrievalAndPagination() throws Exception {
		// Given: setUp에서 4개의 ACTIVE 가격 테이블 항목이 생성됨 (testProduct 2개, additionalProduct1 2개, additionalProduct2 1개, additionalProduct3 1개)
		// testProduct: 2 ACTIVE prices
		// additionalProduct1: 2 ACTIVE prices
		// additionalProduct2: 1 ACTIVE price (1 INACTIVE price is ignored)
		// additionalProduct3: 1 ACTIVE price
		// Total active price entries: 2 + 2 + 1 + 1 = 6

		int totalActivePriceEntries = 6;
		int pageSize = 2; // 한 페이지에 2개씩

		// 1. 첫 번째 페이지 조회 (page=0)
		mockMvc.perform(get("/products")
						.param("page", "0")
						.param("size", String.valueOf(pageSize))
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."))
				.andExpect(jsonPath("$.data.content.size()").value(pageSize))
				.andExpect(jsonPath("$.data.totalElements").value(totalActivePriceEntries))
				.andExpect(jsonPath("$.data.totalPages").value((totalActivePriceEntries + pageSize - 1) / pageSize)); // 6 / 2 = 3 pages
				
						// 2. 모든 페이지를 순회하며 ACTIVE 상태의 가격 테이블만 반환되는지 확인
						for (int page = 0; page < (totalActivePriceEntries + pageSize - 1) / pageSize; page++) {
							String responseContent = mockMvc.perform(get("/products")
											.param("page", String.valueOf(page))
											.param("size", String.valueOf(pageSize))
											.contentType(MediaType.APPLICATION_JSON))
									.andExpect(status().isOk())
									.andReturn().getResponse().getContentAsString();
				
							// JSON 응답 파싱
							com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(responseContent);
							com.fasterxml.jackson.databind.JsonNode contentArray = rootNode.path("data").path("content");
				
							for (com.fasterxml.jackson.databind.JsonNode productNode : contentArray) {
								assertThat(productNode.path("price").asInt()).isNotZero(); // 가격은 0이 아니어야 함 (ACTIVE만 대상)
								assertThat(productNode.path("quantity").asInt()).isNotZero(); // 수량은 0이 아니어야 함 (ACTIVE만 대상)
							}
						}
				
						// 3. 존재하지 않는 페이지 조회 (빈 목록 반환)
						mockMvc.perform(get("/products")
										.param("page", String.valueOf((totalActivePriceEntries + pageSize - 1) / pageSize))
										.param("size", String.valueOf(pageSize))
										.contentType(MediaType.APPLICATION_JSON))
								.andDo(print())
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.data.content.size()").value(0));
	}


}


