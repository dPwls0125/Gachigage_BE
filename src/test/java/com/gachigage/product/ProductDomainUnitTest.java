package com.gachigage.product;

import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.member.Member;
import com.gachigage.product.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductDomainUnitTest {

    @Mock
    private Member mockSeller;
    @Mock
    private ProductCategory mockProductCategory;
    @Mock
    private Region mockRegion;
    @Mock
    private ProductPrice mockProductPrice;
    @Mock
    private ProductImage mockProductImage;

    private Product product;

    @BeforeEach
    void setUp() {
        when(mockProductCategory.getParent()).thenReturn(mock(ProductCategory.class));
        when(mockProductPrice.getQuantity()).thenReturn(10);
        when(mockProductPrice.getPrice()).thenReturn(1000);

        List<ProductPrice> prices = new ArrayList<>();
        prices.add(mockProductPrice);

        List<ProductImage> images = new ArrayList<>();
        images.add(mockProductImage);

        product = Product.create(
                1L,
                mockSeller,
                mockProductCategory,
                mockRegion,
                "Test Product",
                "Description",
                100L,
                TradeType.DELIVERY,
                37.5665,
                126.9780,
                "Seoul",
                prices,
                images
        );
    }

    @Test
    @DisplayName("상품 생성 성공")
    void createProduct_Success() {
        assertThat(product).isNotNull();
        assertThat(product.getTitle()).isEqualTo("Test Product");
        assertThat(product.getCategory()).isEqualTo(mockProductCategory);
        assertThat(product.getImages()).contains(mockProductImage);
        assertThat(product.getPrices()).contains(mockProductPrice);
        assertThat(product.getVisitCount()).isEqualTo(0);
        assertThat(product.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 생성 시, 상위 카테고리는 기타 카테고리만 선택 가능 (하위 카테고리 필수)")
    void createProduct_InvalidParentCategory() {
        when(mockProductCategory.getParent()).thenReturn(null);
        when(mockProductCategory.getName()).thenReturn("주방·조리 장비");

        List<ProductPrice> prices = new ArrayList<>();
        prices.add(mockProductPrice);
        List<ProductImage> images = new ArrayList<>();
        images.add(mockProductImage);

        CustomException exception = assertThrows(CustomException.class, () ->
                Product.create(
                        1L,
                        mockSeller,
                        mockProductCategory,
                        mockRegion,
                        "Test Product",
                        "Description",
                        100L,
                        TradeType.DELIVERY,
                        37.5665,
                        126.9780,
                        "Seoul",
                        prices,
                        images
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(exception.getMessage()).contains("대분류 카테고리는 기타 카테고리만 선택할 수 있습니다.");
    }

    @Test
    @DisplayName("상품 생성 시, 이미지가 8개를 초과하면 예외 발생")
    void createProduct_TooManyImages() {
        List<ProductImage> tooManyImages = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            tooManyImages.add(mock(ProductImage.class));
        }
        List<ProductPrice> prices = new ArrayList<>();
        prices.add(mockProductPrice);

        CustomException exception = assertThrows(CustomException.class, () ->
                Product.create(
                        1L,
                        mockSeller,
                        mockProductCategory,
                        mockRegion,
                        "Test Product",
                        "Description",
                        100L,
                        TradeType.DELIVERY,
                        37.5665,
                        126.9780,
                        "Seoul",
                        prices,
                        tooManyImages
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(exception.getMessage()).contains("상품 이미지는 최대 8개까지 등록할 수 있습니다.");
    }

    @Test
    @DisplayName("상품 생성 시, 가격 정보가 없으면 예외 발생")
    void createProduct_NoPrices() {
        List<ProductPrice> emptyPrices = new ArrayList<>();
        List<ProductImage> images = new ArrayList<>();
        images.add(mockProductImage);

        CustomException exception = assertThrows(CustomException.class, () ->
                Product.create(
                        1L,
                        mockSeller,
                        mockProductCategory,
                        mockRegion,
                        "Test Product",
                        "Description",
                        100L,
                        TradeType.DELIVERY,
                        37.5665,
                        126.9780,
                        "Seoul",
                        emptyPrices,
                        images
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(exception.getMessage()).contains("상품은 최소 하나의 가격 정보를 가져야 합니다.");
    }

    @Test
    @DisplayName("조회수 증가 성공")
    void increaseVisitCount_Success() {
        int initialVisitCount = product.getVisitCount();
        product.increaseVisitCount();
        assertThat(product.getVisitCount()).isEqualTo(initialVisitCount + 1);
    }

    @Test
    @DisplayName("좋아요 수 증가 성공")
    void incrementLikeCount_Success() {
        int initialLikeCount = product.getLikeCount();
        product.incrementLikeCount();
        assertThat(product.getLikeCount()).isEqualTo(initialLikeCount + 1);
    }

    @Test
    @DisplayName("좋아요 수 감소 성공")
    void decrementLikeCount_Success() {
        product.incrementLikeCount(); // Increase first to ensure it's > 0
        int initialLikeCount = product.getLikeCount();
        product.decrementLikeCount();
        assertThat(product.getLikeCount()).isEqualTo(initialLikeCount - 1);
    }

    @Test
    @DisplayName("좋아요 수는 0 미만으로 감소하지 않는다")
    void decrementLikeCount_DoesNotGoBelowZero() {
        while (product.getLikeCount() > 0) {
            product.decrementLikeCount();
        }
        product.decrementLikeCount();
        assertThat(product.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 수정 성공")
    void modifyProduct_Success() {
        ProductCategory newCategory = mock(ProductCategory.class);

        List<ProductPrice> newPrices = new ArrayList<>();
        ProductPrice newPrice1 = mock(ProductPrice.class);
        when(newPrice1.getQuantity()).thenReturn(5);
        when(newPrice1.getPrice()).thenReturn(500);
        newPrices.add(newPrice1);

        List<ProductImage> newImages = new ArrayList<>();
        ProductImage newImage1 = mock(ProductImage.class);
        newImages.add(newImage1);

        product.modify(
                newCategory,
                "Modified Product",
                "Modified Description",
                50L,
                TradeType.DELIVERY,
                37.0,
                127.0,
                "New Address",
                newPrices,
                newImages,
                new Region("", "", "", "1234567890")
        );

        assertThat(product.getCategory()).isEqualTo(newCategory);
        assertThat(product.getTitle()).isEqualTo("Modified Product");
        assertThat(product.getDescription()).isEqualTo("Modified Description");
        assertThat(product.getStock()).isEqualTo(50L);
        assertThat(product.getTradeType()).isEqualTo(TradeType.DELIVERY);
        assertThat(product.getLatitude()).isEqualTo(37.0);
        assertThat(product.getLongitude()).isEqualTo(127.0);
        assertThat(product.getAddress()).isEqualTo("New Address");
        assertThat(product.getPrices()).containsExactly(mockProductPrice, newPrice1);
        assertThat(product.getImages()).containsExactly(newImage1);

        verify(newPrice1).setProduct(product);
        verify(newImage1).setProduct(product);
    }

    @Test
    @DisplayName("addPrice: null 가격 정보 예외 발생")
    void addPrice_NullPrice_ThrowsException() {

        ProductCategory newCategory = mock(ProductCategory.class);

        List<ProductPrice> pricesWithNull = new ArrayList<>();
        pricesWithNull.add(null);
        pricesWithNull.add(mockProductPrice);

        List<ProductImage> images = new ArrayList<>();
        images.add(mockProductImage);

        // When modifying, if addPrice gets a null, it should throw CustomException
        CustomException exception = assertThrows(CustomException.class, () ->
                product.modify(
                        newCategory,
                        "Modified Product",
                        "Modified Description",
                        100L,
                        TradeType.DELIVERY,
                        37.0,
                        127.0,
                        "New Address",
                        pricesWithNull,
                        images,
                        new Region("", "", "", "1234567890")
                )
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(exception.getMessage()).contains("가격 정보이 비어있을 수 없습니다.");
    }

    @Test
    @DisplayName("addPrice: 수량 또는 가격이 0 이하일 때 예외 발생")
    void addPrice_InvalidQuantityOrPrice_ThrowsException() {
        ProductCategory newCategory = mock(ProductCategory.class);

        ProductPrice invalidPrice = mock(ProductPrice.class);
        when(invalidPrice.getQuantity()).thenReturn(0);

        List<ProductPrice> prices = new ArrayList<>();
        prices.add(invalidPrice);

        List<ProductImage> images = new ArrayList<>();
        images.add(mockProductImage);

        CustomException exception = assertThrows(CustomException.class, () ->
                product.modify(
                        newCategory,
                        "Modified Product",
                        "Modified Description",
                        100L,
                        TradeType.DELIVERY,
                        37.0,
                        127.0,
                        "New Address",
                        prices,
                        images,
                        new Region("", "", "", "1234567890")
                )
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(exception.getMessage()).contains("가격 정보의 수량과 가격은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("addPrice: 수량이 재고를 초과할 때 예외 발생")
    void addPrice_QuantityExceedsStock_ThrowsException() {
        ProductCategory newCategory = mock(ProductCategory.class);
        ProductPrice exceedingPrice = mock(ProductPrice.class);
        when(exceedingPrice.getQuantity()).thenReturn(200);
        when(exceedingPrice.getPrice()).thenReturn(1000);

        List<ProductPrice> prices = new ArrayList<>();
        prices.add(exceedingPrice);

        List<ProductImage> images = new ArrayList<>();
        images.add(mockProductImage);

        CustomException exception = assertThrows(CustomException.class, () ->
                product.modify(
                        newCategory,
                        "Modified Product",
                        "Modified Description",
                        100L,
                        TradeType.DELIVERY,
                        37.0,
                        127.0,
                        "New Address",
                        prices,
                        images,
                        new Region("", "", "", "1234567890")
                )
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(exception.getMessage()).contains("가격 정보의 수량은 재고 수량을 초과할 수 없습니다.");
    }
}
