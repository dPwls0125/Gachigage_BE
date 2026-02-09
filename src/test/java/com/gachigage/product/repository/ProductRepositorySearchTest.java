package com.gachigage.product.repository;

import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;
import com.gachigage.product.domain.*;
import com.gachigage.product.dto.ProductListRequestDto;
import com.gachigage.product.dto.ProductListResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositorySearchTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private RegionRepository regionRepository;

    private ProductCategory categoryFurniture;
    private ProductCategory categoryChair;
    private ProductCategory categoryDesk;
    private Region regionGangnam;
    private Region regionJongno;
    private Member seller;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 기본 데이터 설정
        seller = memberRepository.save(Member.builder()
                .email("seller@test.com")
                .name("판매자")
                .roleType(RoleType.USER)
                .birthDate(LocalDate.now())
                .build());

        regionGangnam = regionRepository.save(Region.builder().province("서울특별시").city("강남구").lawCode("1111111111").build());
        regionJongno = regionRepository.save(Region.builder().province("서울특별시").city("종로구").lawCode("2222222222").build());

        categoryFurniture = productCategoryRepository.save(ProductCategory.builder().name("가구").build());
        categoryChair = productCategoryRepository.save(
                ProductCategory.builder().name("의자").parent(categoryFurniture).build());
        categoryDesk = productCategoryRepository.save(
                ProductCategory.builder().name("책상").parent(categoryFurniture).build());

        // --- 상품 데이터 ---

        // 상품 1: 강남구의 의자 (개별/일괄 판매, 이미지 2개)
        List<ProductPrice> prices1 = List.of(
                ProductPrice.builder().price(50000).quantity(1).status(PriceTableStatus.ACTIVE).build(),
                ProductPrice.builder().price(45000).quantity(10).status(PriceTableStatus.ACTIVE).build()
        );
        List<ProductImage> images1 = List.of(
                ProductImage.builder().imageUrl("/img1_2.jpg").order(1).build(),
                ProductImage.builder().imageUrl("/img1_1.jpg").order(0).build()
        );
        Product product1 = Product.create(null, seller, categoryChair, regionGangnam, "편안한 사무용 의자", "아주 편안합니다", 10L,
                TradeType.ALL, 37.5, 127.0, "서울 강남구", prices1, images1);
        productRepository.save(product1);

        // 상품 2: 종로구의 책상
        List<ProductPrice> prices2 = List.of(
                ProductPrice.builder().price(120000).quantity(1).status(PriceTableStatus.ACTIVE).build()
        );
        List<ProductImage> images2 = List.of(
                ProductImage.builder().imageUrl("/img2_1.jpg").order(0).build()
        );
        Product product2 = Product.create(null, seller, categoryDesk, regionJongno, "튼튼한 원목 책상", "원목이라 튼튼해요", 5L,
                TradeType.DIRECT, 37.6, 127.0, "서울 종로구", prices2, images2);
        productRepository.save(product2);

        // 상품 3: 강남구의 다른 의자 (고가)
        List<ProductPrice> prices3 = List.of(
                ProductPrice.builder().price(250000).quantity(1).status(PriceTableStatus.ACTIVE).build()
        );
        List<ProductImage> images3 = List.of(
                ProductImage.builder().imageUrl("/img3_1.jpg").order(0).build()
        );
        Product product3 = Product.create(null, seller, categoryChair, regionGangnam, "최고급 게이밍 의자", "최고입니다", 3L,
                TradeType.DELIVERY, 37.5, 127.1, "서울 강남구", prices3, images3);
        productRepository.save(product3);
    }

    @Test
    @DisplayName("키워드로 상품 검색 및 대표 이미지(order=0) 확인")
    void searchByKeyword() {
        // Given: '의자'라는 키워드로 검색
        ProductListRequestDto requestDto = new ProductListRequestDto("의자", null, null, null, null, 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: '의자'가 포함된 상품은 3개(사무용 의자 2개, 게이밍 의자 1개)
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(p -> p.getTitle().contains("의자"));

        // 사무용 의자의 대표 이미지가 order=0인 /img1_1.jpg 인지 확인
        assertThat(result.getContent())
                .filteredOn(p -> p.getTitle().contains("사무용"))
                .allMatch(p -> p.getMainImageUrl().equals("/img1_1.jpg"));
    }

    @Test
    @DisplayName("하위 카테고리로 상품 필터링")
    void searchBySubCategory() {
        // Given: '의자' 카테고리(ID: categoryChair.getId())로 검색
        ProductListRequestDto requestDto = new ProductListRequestDto(null, categoryChair.getId(), null, null, null, 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: '의자' 카테고리에 속한 상품은 3개
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(p -> p.getTitle().contains("의자"));
    }

    @Test
    @DisplayName("상위 카테고리로 상품 필터링 (하위 카테고리 포함)")
    void searchByPrimaryCategory() {
        // Given: '가구' 카테고리(ID: categoryFurniture.getId())로 검색
        ProductListRequestDto requestDto = new ProductListRequestDto(null, categoryFurniture.getId(), null, null, null, 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: '가구' 카테고리(의자, 책상)에 속한 모든 상품(2개 의자(사무용, 게이밍), 1개 책상)의 판매 단위가 나와야 함 (총 4개)
        // 상품1 (의자) -> 2개 (개별, 일괄)
        // 상품2 (책상) -> 1개 (개별)
        // 상품3 (의자) -> 1개 (개별)
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).anyMatch(p -> p.getTitle().contains("의자"));
        assertThat(result.getContent()).anyMatch(p -> p.getTitle().contains("책상"));
    }

    @Test
    @DisplayName("가격 범위로 필터링")
    void searchByPriceRange() {
        // Given: 100,000원 ~ 200,000원 사이의 상품 검색
        ProductListRequestDto.PriceArrangeDto price = new ProductListRequestDto.PriceArrangeDto(100000, 200000);
        ProductListRequestDto requestDto = new ProductListRequestDto(null, null, price, null, null, 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: '원목 책상' 1개만 해당
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("튼튼한 원목 책상");
        assertThat(result.getContent().get(0).getPrice()).isEqualTo(120000);
    }

    @Test
    @DisplayName("지역으로 필터링")
    void searchByLocation() {
        // Given: '서울특별시 강남구' 검색
        ProductListRequestDto.LocationDto location = new ProductListRequestDto.LocationDto("서울특별시", "강남구");
        ProductListRequestDto requestDto = new ProductListRequestDto(null, null, null, location, null, 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: 강남구에 있는 '사무용 의자'(2개)와 '게이밍 의자'(1개) 총 3개
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(p -> p.getProvince().equals("서울특별시") && p.getCity().equals("강남구"));
    }

    @Test
    @DisplayName("개별/일괄 그룹 로직 확인")
    void checkGroupLogic() {
        // Given: '사무용 의자' 검색
        ProductListRequestDto requestDto = new ProductListRequestDto("사무용 의자", null, null, null, null, 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: '사무용 의자'는 '개별'과 '일괄' 2개의 판매 단위를 가짐
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).anyMatch(p -> p.getGroup().equals("개별") && p.getPrice() == 50000);
        assertThat(result.getContent()).anyMatch(p -> p.getGroup().equals("일괄") && p.getPrice() == 45000);
    }

    @Test
    @DisplayName("복합 조건 필터링 (키워드 + 지역 + 가격)")
    void searchWithComplexFilters() {
        // Given: 강남구의 10만원 이하 '의자' 검색
        ProductListRequestDto.PriceArrangeDto price = new ProductListRequestDto.PriceArrangeDto(0, 100000);
        ProductListRequestDto.LocationDto location = new ProductListRequestDto.LocationDto("서울특별시", "강남구");
        ProductListRequestDto requestDto = new ProductListRequestDto("의자", null, price, location, null, 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: '사무용 의자' 2개(개별/일괄)만 해당
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(p -> p.getTitle().contains("사무용 의자"));
    }

    @Test
    @DisplayName("개별 그룹 필터링 테스트")
    void searchByGroupIndividualFilter() {
        // Given: '개별' 그룹으로 필터링
        ProductListRequestDto requestDto = new ProductListRequestDto(null, null, null, null, "개별", 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: 모든 상품의 '개별' 판매 단위만 나와야 함 (사무용 의자 1개, 원목 책상 1개, 게이밍 의자 1개 = 총 3개)
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(p -> p.getGroup().equals("개별"));
    }

    @Test
    @DisplayName("일괄 그룹 필터링 테스트")
    void searchByGroupBatchFilter() {
        // Given: '일괄' 그룹으로 필터링
        ProductListRequestDto requestDto = new ProductListRequestDto(null, null, null, null, "일괄", 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: '사무용 의자'의 '일괄' 판매 단위 1개만 나와야 함
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).allMatch(p -> p.getGroup().equals("일괄"));
    }

    @Test
    @DisplayName("전체 그룹 필터링 테스트 (필터 없음과 동일)")
    void searchByGroupAllFilter() {
        // Given: '전체' 그룹으로 필터링
        ProductListRequestDto requestDto = new ProductListRequestDto(null, null, null, null, "전체", 0, 10);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ProductListResponseDto> result = productRepository.search(requestDto, pageable, seller.getId());

        // Then: 모든 상품의 모든 판매 단위가 나와야 함 (사무용 의자 2개, 원목 책상 1개, 게이밍 의자 1개 = 총 4개)
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

}
