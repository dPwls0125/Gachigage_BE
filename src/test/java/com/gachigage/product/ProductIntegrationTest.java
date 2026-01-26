package com.gachigage.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.gachigage.product.repository.ProductImageRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // Use a test-specific profile if you have one
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
			.build();
		memberRepository.save(testMember);

		// 2. ProductCategory 생성 및 저장
		mainCategory = ProductCategory.builder().name("식기류").build();
		productCategoryRepository.save(mainCategory);

		subCategory = ProductCategory.builder().name("컵").parent(mainCategory).build();
		productCategoryRepository.save(subCategory);

		// 3. Region 생성 및 저장
		testRegion = Region.builder().province("서울특별시").city("강남구").district("역삼동").build();
		regionRepository.save(testRegion);

		// 4. Product 생성 및 저장
		testProduct = Product.create(null, // ID는 자동 생성
			testMember, subCategory, testRegion, "테스트 상품 제목", "테스트 상품 상세 설명", 10L, TradeType.DIRECT, 37.123456,
			127.654321, "서울 강남구 역삼동",
			List.of(ProductPrice.builder().quantity(1).price(10000).status(PriceTableStatus.ACTIVE).build(),
				ProductPrice.builder().quantity(5).price(45000).status(PriceTableStatus.ACTIVE).build()),
			List.of(ProductImage.builder().imageUrl("http://localhost/image1.jpg").build(),
				ProductImage.builder().imageUrl("http://localhost/image2.jpg").build()));
		productRepository.save(testProduct);

		// ProductImage와 ProductPrice는 cascade persit 되어 함께 저장되므로 별도 저장 필요 없음.
		// 하지만 연관 관계가 잘 맺어졌는지 확인을 위해 다시 불러오는 과정이 필요할 수도 있음.
		// 현재 Product.create 에서 builder를 통해 주입되므로 별도 save 안함.
	}

	@Test
	@DisplayName("상품 상세 조회 통합 테스트")
	@WithMockCustomUser
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
			.andExpect(jsonPath("$.data.category.main").value(mainCategory.getName()))
			.andExpect(jsonPath("$.data.category.sub").value(subCategory.getName()))
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
			.andExpect(jsonPath("$.data.preferredTradeLocations[0].latitude").value(37.123456))
			.andExpect(jsonPath("$.data.preferredTradeLocations[0].longitude").value(127.654321))
			.andExpect(jsonPath("$.data.preferredTradeLocations[0].address").value("서울 강남구 역삼동"))
			.andExpect(jsonPath("$.data.viewCount").value(1))
			.andExpect(jsonPath("$.data.isLiked").value(false))
			.andExpect(jsonPath("$.data.relatedProducts.size").value(0));
	}
}
