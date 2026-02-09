package com.gachigage.product.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gachigage.global.error.CustomException;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;
import com.gachigage.product.domain.*;
import com.gachigage.product.dto.ProductLikeResponseDto;
import com.gachigage.product.dto.ProductListRequestDto;
import com.gachigage.product.dto.ProductListResponseDto;
import com.gachigage.product.repository.ProductLikeRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.image.service.ImageService;
import com.gachigage.product.dto.ProductRegistrationRequestDto;
import com.gachigage.product.repository.ProductCategoryRepository;
import com.gachigage.product.repository.RegionRepository; // Added import for RegionRepository
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.gachigage.product.domain.PriceTableStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProductLikeRepository productLikeRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private NaverMapsClient naverMapsClient;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock // Added this mock
    private RegionRepository regionRepository;

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

        // The 'region' variable here is a local variable shadowing the class-level 'region' field.
        // It's used to construct 'savedProduct'.
        Region region = new Region("서울특별시", "강남구", "역삼동", "1111111111"); // TODO

        ProductCategory mainCategory = ProductCategory.builder().name("가구").build();
        subCategory = ProductCategory.builder().name("의자").parent(mainCategory).build();


        savedProduct = Product.create(
                1L, savedMember, subCategory, region, "테스트 상품", "테스트 상품 설명", 5L,
                TradeType.DELIVERY, 37.123, 127.123, "테스트 주소",
                List.of(ProductPrice.builder().price(1000).quantity(5).status(ACTIVE).build()), List.of()
        );

    }

    @Test
    @DisplayName("상품 목록 검색 및 필터링 성공 테스트")
    void getProducts_Success() {
        // Given
        ProductListRequestDto requestDto = new ProductListRequestDto(
                "테스트", 1L, null, null, null, 0, 10
        );
        Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize());

        ProductListResponseDto responseDto = new ProductListResponseDto(
                1L, "테스트 상품", "http://example.com/image.jpg",
                "서울특별시", "강남구", "일괄", TradeType.DELIVERY, 1000, 5, 0, false, LocalDateTime.now()
        );
        Page<ProductListResponseDto> mockPage = new PageImpl<>(List.of(responseDto), pageable, 1);

        when(productRepository.search(any(ProductListRequestDto.class), any(Pageable.class), any(Long.class)))
                .thenReturn(mockPage);

        // When
        Page<ProductListResponseDto> result = productService.getProducts(requestDto, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto.getTitle(), result.getContent().get(0).getTitle());
        verify(productRepository, times(1)).search(any(ProductListRequestDto.class), any(Pageable.class), any(Long.class));
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

    @Test
    @DisplayName("createProduct 호출 시 ProductImage의 order가 1씩 증가하며 저장되는지 확인")
    void createProduct_ProductImagesHaveCorrectOrder() {
        // Given
        Long memberOauthId = 111L;
        Long subCategoryId = 1L;
        List<String> imageUrls = List.of("url1", "url2", "url3");

        ProductRegistrationRequestDto.TradeLocationRegistrationDto tradeLocationDto =
                new ProductRegistrationRequestDto.TradeLocationRegistrationDto(80.0, 37.0, "Test Address");

        List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> priceTable = List.of(
                new ProductRegistrationRequestDto.ProductPriceRegistrationDto(1, 1000, ACTIVE)
        );

        when(memberRepository.findMemberByOauthId(memberOauthId)).thenReturn(Optional.of(savedMember));
        when(productCategoryRepository.findById(subCategoryId)).thenReturn(Optional.of(subCategory));
        when(naverMapsClient.reverseGeocode(anyDouble(), anyDouble())).thenReturn(Mono.just(
            mock(JsonNode.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS))
        ));

        // Mock the deep structure for region extraction
        JsonNode mockJsonNode = mock(JsonNode.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        when(mockJsonNode.has("results")).thenReturn(true);
        when(mockJsonNode.get("results").isEmpty()).thenReturn(false);
        when(mockJsonNode.get("results").get(0).get("code").get("id").asText()).thenReturn("4113510900"); // Example law code
        when(mockJsonNode.get("results").get(0).get("region").get("area1").get("name").asText()).thenReturn("경기도");
        when(mockJsonNode.get("results").get(0).get("region").get("area2").get("name").asText()).thenReturn("성남시 분당구");
        when(naverMapsClient.reverseGeocode(anyDouble(), anyDouble())).thenReturn(Mono.just(mockJsonNode));

        // Mock regionRepository to return a region when findByLawCode is called
        when(regionRepository.findByLawCode(anyString())).thenReturn(Optional.of(new Region("경기도", "성남시", "분당구", "4113510900")));
        when(regionRepository.save(any(Region.class))).thenReturn(new Region("경기도", "성남시", "분당구", "4113510900"));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            return product;
        });

        // When
        productService.createProduct(memberOauthId, subCategoryId, "Test Product", "Detail", 1L,
                priceTable, TradeType.DELIVERY, tradeLocationDto, imageUrls);

        // Then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertNotNull(capturedProduct.getProductImages());
        assertEquals(imageUrls.size(), capturedProduct.getProductImages().size());

        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage productImage = capturedProduct.getProductImages().get(i);
            assertEquals(i, productImage.getOrder());
            assertEquals(imageUrls.get(i), productImage.getImageUrl());
        }
    }
}
