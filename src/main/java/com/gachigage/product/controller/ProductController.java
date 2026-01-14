package com.gachigage.product.controller;

import com.gachigage.product.dto.ProductCreateRequestDto;
import com.gachigage.product.dto.ProductDetailResponseDto;
import com.gachigage.product.dto.ProductListResponseDto;
import com.gachigage.product.dto.ProductUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    /**
     * 상품 목록 조회 (Mock)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts() {
        ProductListResponseDto item = ProductListResponseDto.builder()
                .productId(111L)
                .title("Lorem ipsum")
                .minPrice(150000)
                .maxPrice(180000)
                .thumbnailUrl("https://bucket/img1.jpg")
                .category("식기류")
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .tradeType("직거래")
                .viewCount(32)
                .createdAt(LocalDateTime.now())
                .build();

        Map<String, Object> response = Map.of(
                "type", "all",
                "page", 1,
                "size", 10,
                "items", List.of(item)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 상세 조회 (Mock)
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponseDto> getProduct(@PathVariable Long productId) {
        // In a real app, you would fetch the product and construct the response
        ProductDetailResponseDto response = ProductDetailResponseDto.builder()
                .product(null) // In a real app, you'd pass the Product entity
                .sellerName("홍길동")
                .isLiked(true)
                .relatedProducts(Collections.emptyList())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 등록 (Mock)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductCreateRequestDto requestDto) {
        // In a real app, you would save the product and get its new ID
        Long mockProductId = 111L;
        Map<String, Object> response = Map.of(
                "productId", mockProductId,
                "message", "상품이 성공적으로 등록되었습니다."
        );
        return ResponseEntity.created(URI.create("/products/" + mockProductId)).body(response);
    }

    /**
     * 상품 수정 (Mock)
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long productId, @RequestBody ProductUpdateRequestDto requestDto) {
        Map<String, Object> response = Map.of(
                "productId", productId,
                "message", "상품이 성공적으로 수정되었습니다."
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 삭제 (Mock)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long productId) {
        Map<String, Object> response = Map.of(
                "productId", productId,
                "message", "상품이 정상적으로 삭제되었습니다."
        );
        return ResponseEntity.ok(response);
    }
}
