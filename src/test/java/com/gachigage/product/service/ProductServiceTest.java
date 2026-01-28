package com.gachigage.product.service;

import static com.gachigage.product.domain.PriceTableStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gachigage.global.error.CustomException;
import com.gachigage.image.service.ImageService;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.domain.ProductLike;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.domain.Region;
import com.gachigage.product.domain.TradeType;
import com.gachigage.product.dto.ProductLikeResponseDto;
import com.gachigage.product.repository.ProductCategoryRepository;
import com.gachigage.product.repository.ProductLikeRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;

@SpringBootTest
@Transactional
class ProductServiceTest {

	@InjectMocks
	private ProductService productService;
	@Mock
	private ProductCategoryRepository productCategoryRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private RegionRepository regionRepository;
	@Mock
	private ImageService imageService;
	@Mock
	private ProductLikeRepository productLikeRepository;

	private Member savedMember;
	private ProductCategory subCategory;
	private Region region;
	private Product savedProduct;

	@BeforeEach
	void setUp() {
		savedMember = Member.builder()
			.email("test@gmail.com")
			.name("테스터")
			.roleType(RoleType.USER)
			.birthDate(LocalDate.now())
			.oauthId(111L)
			.build();

		Region region = new Region("서울특별시", "강남구", "역삼동");

		savedProduct = Product.create(
			1L, savedMember, subCategory, region, "테스트 상품", "테스트 상품 설명", 5L,
			TradeType.DELIVERY, 37.123, 127.123, "테스트 주소",
			List.of(ProductPrice.builder().price(1000).quantity(5).status(ACTIVE).build()), List.of()
		);

		ProductCategory mainCategory = ProductCategory.builder().name("가구").build();
		subCategory = ProductCategory.builder().name("의자").parent(mainCategory).build();

	}

	@Test
	@DisplayName("존재하지 않는 상품에 대해 좋아요 시도시 예외 발생")
	void toggleProductLike_ProductNotFound() {
		when(memberRepository.findMemberByOauthId(anyLong()))
			.thenReturn(Optional.of(savedMember));
		when(productRepository.findById(anyLong()))
			.thenReturn(Optional.empty());

		Long nonExistentProductId = 2L;

		assertThrows(CustomException.class, () ->
				productService.toggleProductLike(savedMember.getOauthId(), nonExistentProductId),
			"존재하지 않는 상품입니다");

		verify(productLikeRepository, never()).delete(any());
		verify(productLikeRepository, never()).save(any());
	}

	@Test
	@DisplayName("이미 좋아요한 경우 좋아요 취소")
	void toggleProductLike_unlike() {
		ProductLike existingLike = ProductLike.builder()
			.member(savedMember)
			.product(savedProduct)
			.build();

		when(memberRepository.findMemberByOauthId(anyLong()))
			.thenReturn(Optional.of(savedMember));
		when(productRepository.findById(anyLong()))
			.thenReturn(Optional.of(savedProduct));
		when(productLikeRepository.findByMemberAndProduct(any(), any()))
			.thenReturn(Optional.of(existingLike));

		ProductLikeResponseDto result =
			productService.toggleProductLike(savedMember.getOauthId(), savedProduct.getId());

		assertFalse(result.liked());
		verify(productLikeRepository).delete(existingLike);
	}

	@Test
	@DisplayName("회원이 상품에 좋아요를 하지 않은 경우 좋아요 추가")
	void toggleProductLike_LikeNewProduct() {
		when(memberRepository.findMemberByOauthId(anyLong()))
			.thenReturn(Optional.of(savedMember));
		when(productRepository.findById(anyLong()))
			.thenReturn(Optional.of(savedProduct));
		when(productLikeRepository.findByMemberAndProduct(any(), any()))
			.thenReturn(Optional.empty());

		int currentLikeCount = savedProduct.getLikeCount();
		ProductLikeResponseDto result = productService.toggleProductLike(savedMember.getOauthId(),
			savedProduct.getId());

		assertTrue(result.liked());
		assertEquals(result.likeCount(), ++currentLikeCount);
		verify(productLikeRepository, times(1)).save(any());
		verify(productLikeRepository, never()).delete(any());
	}

}

