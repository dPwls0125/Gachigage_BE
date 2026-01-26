package com.gachigage.product.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gachigage.global.WithMockCustomUser;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;
import com.gachigage.product.domain.PriceTableStatus;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.domain.TradeType;
import com.gachigage.product.dto.ProductRegistrationRequestDto;
import com.gachigage.product.repository.ProductCategoryRepository;
import com.gachigage.product.repository.ProductRepository;

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

		ProductCategory mainCategory = ProductCategory
			.builder()
			.name("가구")
			.build();

		productCategoryRepository.save(mainCategory);

		ProductCategory subCategory = ProductCategory
			.builder()
			.name("의자")
			.parent(mainCategory)
			.build();

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
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(1, 10000),
			new ProductRegistrationRequestDto.ProductPriceRegistrationDto(5, 45000)
		);

		ProductRegistrationRequestDto.TradeLocationRegistrationDto tradeLocation =
			new ProductRegistrationRequestDto.TradeLocationRegistrationDto(37.497952, 127.027619, "서울 강남구 강남역");

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

		assertThat(foundProduct.getPrices().get(0).getStatus()).isEqualTo(PriceTableStatus.ACTIVE);
		assertThat(foundProduct.getPrices().get(1).getStatus()).isEqualTo(PriceTableStatus.ACTIVE);

		// ProductPrice와 ProductImage가 올바르게 저장되었는지 확인
		assertThat(foundProduct.getPrices()).hasSize(2);
		assertThat(foundProduct.getPrices().get(0).getPrice()).isEqualTo(10000);
		assertThat(foundProduct.getImages()).hasSize(2);
		assertThat(foundProduct.getImages().get(0).getImageUrl()).isEqualTo("http://example.com/image1.jpg");
		assertThat(foundProduct.getRegion().getProvince()).isEqualTo("서울특별시"); // TODO : 외부 API 연동 후 수정
		assertThat(foundProduct.getRegion().getCity()).isEqualTo("강남구"); // TODO : 외부 API 연동 후 수정
		assertThat(foundProduct.getRegion().getDistrict()).isEqualTo("역삼동"); // TODO : 외부 API 연동 후 수정
	}
}
