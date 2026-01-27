package com.gachigage.product.service;

import static com.gachigage.global.error.ErrorCode.*;
import static com.gachigage.product.domain.PriceTableStatus.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gachigage.global.WithMockCustomUser;
import com.gachigage.global.error.CustomException;
import com.gachigage.image.service.ImageService;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.domain.Region;
import com.gachigage.product.domain.TradeType;
import com.gachigage.product.dto.ProductModifyRequestDto;
import com.gachigage.product.dto.ProductRegistrationRequestDto;
import com.gachigage.product.repository.ProductCategoryRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;

@SpringBootTest
@Transactional
class ProductServiceTest {

	@Autowired
	private ProductService productService;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ProductCategoryRepository productCategoryRepository;
	@Autowired
	private RegionRepository regionRepository;

	@Autowired
	private ImageService imageService;

	private Member savedMember;
	private ProductCategory savedCategory;

	@BeforeEach
	void setUp() {
		// `@WithMockCustomUser`가 생성하는 Member와 동일한 정보로 Member를 미리 저장
		Member member = Member.builder()
			.email("test@gmail.com")
			.name("테스터")
			.roleType(RoleType.USER)
			.birthDate(LocalDate.now())
			.oauthId(111L)
			.build();
		savedMember = memberRepository.save(member);

		ProductCategory mainCategory = ProductCategory.builder().name("가구").build();

		productCategoryRepository.save(mainCategory);

		ProductCategory subCategory = ProductCategory.builder().name("의자").parent(mainCategory).build();

		savedCategory = productCategoryRepository.save(subCategory);
	}

	@Test
	@DisplayName("상품 등록 통합 테스트")
	@WithMockCustomUser
	void createProductSuccess() {
		// given
		String title = "10일 사용한 의자 5개 판매합니다.";
		String detail = "엄청 편한 의자입니다.";
		Long stock = 10L;
		TradeType tradeType = TradeType.DELIVERY;

		List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> priceTable = List.of(
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(1, 10000, ACTIVE),
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(5, 45000, ACTIVE));

		ProductRegistrationRequestDto.TradeLocationRegistrationDto tradeLocation = new ProductRegistrationRequestDto.TradeLocationRegistrationDto(
			37.497952, 127.027619, "서울 강남구 강남역");

		List<String> imageUrls = List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg");

		// when
		Product product = productService.createProduct(
			savedMember.getOauthId(),
			savedCategory.getId(),
			title,
			detail,
			stock,
			priceTable,
			tradeType,
			tradeLocation,
			imageUrls
		);

		// then
		Product foundProduct = productRepository.findById(product.getId()).get();

		assertThat(foundProduct).isNotNull();
		assertThat(foundProduct.getTitle()).isEqualTo(title);
		assertThat(foundProduct.getDescription()).isEqualTo(detail);
		assertThat(foundProduct.getStock()).isEqualTo(stock);
		assertThat(foundProduct.getTradeType()).isEqualTo(tradeType);
		assertThat(foundProduct.getSeller().getId()).isEqualTo(savedMember.getId());
		assertThat(foundProduct.getCategory().getId()).isEqualTo(savedCategory.getId());
		assertThat(foundProduct.getLatitude()).isEqualTo(tradeLocation.getLatitude());
		assertThat(foundProduct.getLongtitude()).isEqualTo(tradeLocation.getLongitude());
		assertThat(foundProduct.getAddress()).isEqualTo(tradeLocation.getAddress());

		assertThat(foundProduct.getPrices().get(0).getStatus()).isEqualTo(ACTIVE);
		assertThat(foundProduct.getPrices().get(1).getStatus()).isEqualTo(ACTIVE);

		// ProductPrice와 ProductImage가 올바르게 저장되었는지 확인
		assertThat(foundProduct.getPrices()).hasSize(2);
		assertThat(foundProduct.getPrices().get(0).getPrice()).isEqualTo(10000);
		assertThat(foundProduct.getImages()).hasSize(2);
		assertThat(foundProduct.getImages().get(0).getImageUrl()).isEqualTo("http://example.com/image1.jpg");
		assertThat(foundProduct.getRegion().getProvince()).isEqualTo("서울특별시"); // TODO : 외부 API 연동 후 수정
		assertThat(foundProduct.getRegion().getCity()).isEqualTo("강남구"); // TODO : 외부 API 연동 후 수정
		assertThat(foundProduct.getRegion().getDistrict()).isEqualTo("역삼동"); // TODO : 외부 API 연동 후 수정
	}

	@Test
	@DisplayName("상품 수정 통합 테스트")
	@WithMockCustomUser
	void productModifySuccess() {

		// given
		// 1. 수정할 상품을 미리 저장
		Region region = new Region("서울특별시", "강남구", "역삼동");
		regionRepository.save(region);

		Product product = Product.create(null, savedMember, savedCategory, region, "수정 전 제목", "수정 전 설명", 10L,
			TradeType.DELIVERY, 37.123, 127.123, "수정 전 주소",
			List.of(ProductPrice.builder().price(1000).quantity(10).status(ACTIVE).build()), List.of());

		productRepository.save(product);

		// 2. 수정할 정보
		ProductCategory newCategory = ProductCategory.builder().name("책상").parent(savedCategory.getParent()).build();
		productCategoryRepository.save(newCategory);

		String modifiedTitle = "수정된 제목";
		String modifiedDetail = "수정된 설명";
		Long modifiedStock = 20L;
		TradeType modifiedTradeType = TradeType.DIRECT;
		List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> modifiedPriceTable = List.of(
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(1, 20000, INACTIVE),
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(5, 90000, ACTIVE));
		ProductModifyRequestDto.TradeLocationRegistrationDto modifiedTradeLocation = new ProductModifyRequestDto.TradeLocationRegistrationDto(
			37.456, 127.456, "수정된 주소");
		List<String> modifiedImageUrls = List.of("http://example.com/modified1.jpg",
			"http://example.com/modified2.jpg");

		// when
		productService.modifyProduct(product.getId(), savedMember.getId(), newCategory.getId(), modifiedTitle,
			modifiedDetail, modifiedStock, modifiedPriceTable, modifiedTradeType, modifiedTradeLocation,
			modifiedImageUrls);

		// then
		Product foundProduct = productRepository.findById(product.getId()).get();

		assertThat(foundProduct.getTitle()).isEqualTo(modifiedTitle);
		assertThat(foundProduct.getDescription()).isEqualTo(modifiedDetail);
		assertThat(foundProduct.getStock()).isEqualTo(modifiedStock);
		assertThat(foundProduct.getTradeType()).isEqualTo(modifiedTradeType);
		assertThat(foundProduct.getCategory().getId()).isEqualTo(newCategory.getId());
		assertThat(foundProduct.getLatitude()).isEqualTo(modifiedTradeLocation.getLatitude());
		assertThat(foundProduct.getLongtitude()).isEqualTo(modifiedTradeLocation.getLongitude());
		assertThat(foundProduct.getAddress()).isEqualTo(modifiedTradeLocation.getAddress());

		assertThat(foundProduct.getPrices()).hasSize(2);
		assertThat(foundProduct.getPrices().get(0).getPrice()).isEqualTo(20000);
		assertThat(foundProduct.getPrices().get(0).getQuantity()).isEqualTo(1);
		assertThat(foundProduct.getPrices().get(0).getStatus()).isEqualTo(INACTIVE);
		assertThat(foundProduct.getPrices().get(1).getPrice()).isEqualTo(90000);
		assertThat(foundProduct.getPrices().get(1).getQuantity()).isEqualTo(5);
		assertThat(foundProduct.getPrices().get(1).getStatus()).isEqualTo(ACTIVE);

		assertThat(foundProduct.getImages()).hasSize(2);
		assertThat(foundProduct.getImages().get(0).getImageUrl()).isEqualTo("http://example.com/modified1.jpg");
	}

	@Test
	@DisplayName("상품 삭제 - 성공")
	@WithMockCustomUser
	void deleteProductSuccess() {
		// given
		Region region = new Region("서울특별시", "강남구", "역삼동");
		regionRepository.save(region);

		Product product = Product.create(null, savedMember, savedCategory, region, "삭제될 상품", "삭제될 상품 설명", 1L,
			TradeType.DELIVERY, 37.123, 127.123, "삭제될 주소",
			List.of(ProductPrice.builder().price(1000).quantity(1).status(ACTIVE).build()), List.of());
		productRepository.save(product);

		// when
		productService.deleteProduct(product.getId(), savedMember.getOauthId());

		// then
		Optional<Product> foundProduct = productRepository.findById(product.getId());
		assertThat(foundProduct).isEmpty();
	}

	@Test
	@DisplayName("상품 삭제 - 실패 (상품을 찾을 수 없음)")
	@WithMockCustomUser
	void deleteProductNotFound() {
		// given
		Long nonExistentProductId = 9999L;

		// when, then
		assertThatThrownBy(() -> productService.deleteProduct(nonExistentProductId, savedMember.getId()))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", RESOURCE_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 삭제 - 실패 (회원을 찾을 수 없음)")
	void deleteProductUserNotFound() {
		// given
		Region region = new Region("서울특별시", "강남구", "역삼동");
		regionRepository.save(region);

		Product product = Product.create(null, savedMember, savedCategory, region, "삭제될 상품", "삭제될 상품 설명", 1L,
			TradeType.DELIVERY, 37.123, 127.123, "삭제될 주소",
			List.of(ProductPrice.builder().price(1000).quantity(1).status(ACTIVE).build()), List.of());
		productRepository.save(product);

		Long nonExistentMemberId = 8888L;

		// when, then
		assertThatThrownBy(() -> productService.deleteProduct(product.getId(), nonExistentMemberId))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 삭제 - 실패 (권한 없음)")
	void deleteProductUnauthorized() {
		// given
		Region region = new Region("서울특별시", "강남구", "역삼동");
		regionRepository.save(region);

		Product product = Product.create(null, savedMember, savedCategory, region, "다른 사람 상품", "다른 사람 상품 설명", 1L,
			TradeType.DELIVERY, 37.123, 127.123, "다른 사람 주소",
			List.of(ProductPrice.builder().price(1000).quantity(1).status(ACTIVE).build()), List.of());
		productRepository.save(product);

		Member unauthorizedMember = Member.builder()
			.email("unauthorized@gmail.com")
			.name("권한없는 테스터")
			.roleType(RoleType.USER)
			.birthDate(LocalDate.now())
			.oauthId(222L) // Different OAuth ID
			.build();
		memberRepository.save(unauthorizedMember);

		// when, then
		assertThatThrownBy(() -> productService.deleteProduct(product.getId(), unauthorizedMember.getOauthId()))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", UNAUTHORIZED_USER);
	}
}

