package com.gachigage.product.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gachigage.global.ApiResponse;
import com.gachigage.product.domain.Product;
import com.gachigage.product.dto.CategoryResponseDto;
import com.gachigage.product.dto.ProductDetailResponseDto;
import com.gachigage.product.dto.ProductImageResponseDto;
import com.gachigage.product.dto.ProductModifyRequestDto;
import com.gachigage.product.dto.ProductRegistrationRequestDto;
import com.gachigage.product.dto.ProductRegistrationResponseDto;
import com.gachigage.product.service.ProductCategoryService;
import com.gachigage.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 관련 API")
@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;
	private final ProductCategoryService productCategoryService;

	public ProductController(ProductService productService, ProductCategoryService productCategoryService) {
		this.productService = productService;
		this.productCategoryService = productCategoryService;
	}

	@SuppressWarnings("checkstyle:Indentation")
	@Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "상품이 성공적으로 등록되었습니다.")})
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping
	public ResponseEntity<ApiResponse<ProductRegistrationResponseDto>> registerProduct(
		@RequestBody ProductRegistrationRequestDto requestDto,
		@AuthenticationPrincipal User user) {
		Product product = productService.createProduct(
			Long.parseLong(user.getUsername()),
			requestDto.getCategoryId(),
			requestDto.getTitle(),
			requestDto.getDetail(),
			requestDto.getStock(),
			requestDto.getPriceTable(),
			requestDto.getTradeType(),
			requestDto.getPreferredTradeLocations(),
			requestDto.getImageUrls()
		);
		return ResponseEntity.ok(
			ApiResponse.success(ProductRegistrationResponseDto.builder().productId(product.getId()).build()));
	}

	@Operation(summary = "상품 수정", description = "판매자가 기존 상품의 정보를 수정합니다.")
	@PutMapping("/{productId}")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<ProductModifyRequestDto>> modifyProduct(
		@AuthenticationPrincipal User user,
		@PathVariable Long productId,
		@RequestBody ProductModifyRequestDto requestDto) {
		productService.modifyProduct(
			productId,
			Long.parseLong(user.getUsername()),
			requestDto.getCategoryId(),
			requestDto.getTitle(),
			requestDto.getDetail(),
			requestDto.getStock(),
			requestDto.getPriceTable(),
			requestDto.getTradeType(),
			requestDto.getPreferredTradeLocation(),
			requestDto.getImageUrls()
		);
		return ResponseEntity.ok(ApiResponse.success());

	}

	@Operation(summary = "상품 삭제", description = "판매자가 등록한 상품을 삭제합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "상품이 성공적으로 삭제되었습니다.")})
	@SecurityRequirement(name = "bearerAuth")
	@DeleteMapping("/{productId}")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(
		@AuthenticationPrincipal User user,
		@PathVariable Long productId) {
		productService.deleteProduct(productId, Long.parseLong(user.getUsername()));
		return ResponseEntity.ok(ApiResponse.success());
	}

	@Operation(summary = "상품 상세 조회", description = "상품의 상세 정보 및 관련 상품을 조회합니다.")
	@GetMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductDetailResponseDto>> getProductDetails(@PathVariable Long productId) {
		ProductDetailResponseDto response = productService.getProductDetail(productId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(summary = "상품 이미지 업로드", description = "상품 이미지 파일을 업로드 합니다.")
	@PostMapping(value = "/images",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ProductImageResponseDto>> getProductImageUrls(
		@RequestPart("files") List<MultipartFile> imageFiles) {
		List<String> urls = productService.saveToBucketAndGetImageUrls(imageFiles);
		return ResponseEntity.ok(ApiResponse.success(new ProductImageResponseDto(urls)));
	}

	@Operation(summary = "카테고리 조회", description = "상품 카테고리 목록을 조회합니다.")
	@GetMapping("/category")
	public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getCategories() {
		List<CategoryResponseDto> categories = productCategoryService.getCategories();
		return ResponseEntity.ok(ApiResponse.success(categories));
	}

}
