package com.gachigage.product.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachigage.global.WithMockCustomUser;
import com.gachigage.global.config.CustomAuthenticationEntryPoint;
import com.gachigage.global.config.JwtProvider;
import com.gachigage.global.config.SecurityConfig;
import com.gachigage.global.login.service.CustomOAuth2UserService;
import com.gachigage.member.Member;
import com.gachigage.member.RoleType;
import com.gachigage.product.domain.PriceTableStatus;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.domain.TradeType;
import com.gachigage.product.dto.ProductDetailResponseDto;
import com.gachigage.product.dto.ProductRegistrationRequestDto;
import com.gachigage.product.service.ProductCategoryService;
import com.gachigage.product.service.ProductService;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class})
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductService productService;

	@MockitoBean
	private ProductCategoryService productCategoryService;

	@MockitoBean
	private JwtProvider jwtProvider;

	@MockitoBean
	private AuthenticationSuccessHandler oAuth2SuccessHandler;

	@MockitoBean
	private CustomOAuth2UserService oAuth2UserService;

	@MockitoBean
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	private Member member;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.id(1L)
			.email("test@gmail.com")
			.name("테스터")
			.roleType(RoleType.USER)
			.birthDate(LocalDate.now())
			.build();
	}

	@Test
	@DisplayName("상품 등록 컨트롤러 테스트")
	@WithMockCustomUser
	void registerProductSuccess() throws Exception {
		// given
		List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> priceTable = List.of(
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(1, 10000, PriceTableStatus.ACTIVE),
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(5, 45000, PriceTableStatus.ACTIVE)
		);
		ProductRegistrationRequestDto.TradeLocationRegistrationDto tradeLocation =
			new ProductRegistrationRequestDto.TradeLocationRegistrationDto(37.497952, 127.027619, "서울 강남구 강남역");

		ProductRegistrationRequestDto requestDto = new ProductRegistrationRequestDto(
			1L,
			"테스트 의자",
			"테스트 상세 설명",
			10L,
			priceTable,
			TradeType.DELIVERY,
			tradeLocation,
			List.of("image_url1.jpg", "image_url2.jpg")
		);

		Product returnedProduct = Product.create(
			1L,
			member,
			null,
			null,
			requestDto.getTitle(),
			requestDto.getDetail(),
			requestDto.getStock(),
			requestDto.getTradeType(),
			tradeLocation.getLatitude(),
			tradeLocation.getLongitude(),
			tradeLocation.getAddress(),
			List.of(ProductPrice.builder()
				.quantity(1)
				.price(10000)
				.status(PriceTableStatus.ACTIVE)
				.build()
			),
			List.of()
		);

		given(productService.createProduct(
			anyLong(),
			anyLong(),
			any(),
			any(),
			any(),
			any(),
			any(),
			any(),
			any()
		)).willReturn(returnedProduct);

		// when & then
		mockMvc.perform(post("/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andDo(print()) // Add this line to print the MvcResult
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."))
			.andExpect(jsonPath("$.data.productId").value(1L));
	}

	@Test
	@DisplayName("상품 상세 조회 컨트롤러 테스트")
	@WithMockCustomUser
	void getProductDetailSuccess() throws Exception {
		// given
		Long productId = 1L;

		ProductDetailResponseDto.ProductCategoryDto categoryDto =
			ProductDetailResponseDto.ProductCategoryDto.builder()
				.main("가전제품")
				.sub("냉장고")
				.build();

		ProductDetailResponseDto.ProductPriceDto productPriceDto1 =
			ProductDetailResponseDto.ProductPriceDto.builder()
				.quantity(1)
				.price(10000)
				.build();

		ProductDetailResponseDto.ProductPriceDto productPriceDto2 =
			ProductDetailResponseDto.ProductPriceDto.builder()
				.quantity(5)
				.price(45000)
				.build();

		ProductDetailResponseDto.TradeLocationDto tradeLocationDto =
			ProductDetailResponseDto.TradeLocationDto.builder()
				.latitude(37.497952)
				.longitude(127.027619)
				.address("서울 강남구 강남역")
				.build();

		ProductDetailResponseDto.RelatedProductDto relatedProductDto =
			ProductDetailResponseDto.RelatedProductDto.builder()
				.productId(2L)
				.title("관련 상품 제목")
				.thumbnailUrl("https://bucket/related1.jpg")
				.price(20000)
				.quantity(1)
				.province("서울특별시")
				.city("서초구")
				.viewCount(10)
				.build();

		ProductDetailResponseDto.RelatedProductsDto relatedProductsDto =
			ProductDetailResponseDto.RelatedProductsDto.builder()
				.size(1)
				.products(List.of(relatedProductDto))
				.build();

		ProductDetailResponseDto mockResponseDto = ProductDetailResponseDto.builder()
			.productId(productId)
			.title("테스트 상품")
			.detail("테스트 상품 상세 설명")
			.sellerName("테스터")
			.category(categoryDto)
			.tradeType(TradeType.DELIVERY)
			.imageUrls(List.of("https://bucket/image1.jpg", "https://bucket/image2.jpg"))
			.stock(10L)
			.priceTable(List.of(productPriceDto1, productPriceDto2))
			.preferredTradeLocations(List.of(tradeLocationDto))
			.viewCount(52)
			.isLiked(true)
			.relatedProducts(relatedProductsDto)
			.build();

		given(productService.getProductDetail(anyLong())).willReturn(mockResponseDto);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/products/{productId}", productId)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("성공적으로 처리되었습니다."))
			.andExpect(jsonPath("$.data.productId").value(productId))
			.andExpect(jsonPath("$.data.title").value("테스트 상품"))
			.andExpect(jsonPath("$.data.detail").value("테스트 상품 상세 설명"))
			.andExpect(jsonPath("$.data.sellerName").value("테스터"))
			.andExpect(jsonPath("$.data.category.main").value("가전제품"))
			.andExpect(jsonPath("$.data.category.sub").value("냉장고"))
			.andExpect(jsonPath("$.data.tradeType").value("DELIVERY"))
			.andExpect(jsonPath("$.data.imageUrls[0]").value("https://bucket/image1.jpg"))
			.andExpect(jsonPath("$.data.imageUrls[1]").value("https://bucket/image2.jpg"))
			.andExpect(jsonPath("$.data.stock").value(10L))
			.andExpect(jsonPath("$.data.priceTable[0].quantity").value(1))
			.andExpect(jsonPath("$.data.priceTable[0].price").value(10000))
			.andExpect(jsonPath("$.data.priceTable[1].quantity").value(5))
			.andExpect(jsonPath("$.data.priceTable[1].price").value(45000))
			.andExpect(jsonPath("$.data.preferredTradeLocations[0].latitude").value(37.497952))
			.andExpect(jsonPath("$.data.preferredTradeLocations[0].longitude").value(127.027619))
			.andExpect(jsonPath("$.data.preferredTradeLocations[0].address").value("서울 강남구 강남역"))
			.andExpect(jsonPath("$.data.viewCount").value(52))
			.andExpect(jsonPath("$.data.isLiked").value(true))
			.andExpect(jsonPath("$.data.relatedProducts.size").value(1))
			.andExpect(jsonPath("$.data.relatedProducts.products[0].productId").value(2L))
			.andExpect(jsonPath("$.data.relatedProducts.products[0].title").value("관련 상품 제목"));
	}
}
