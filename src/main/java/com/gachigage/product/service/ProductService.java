package com.gachigage.product.service;

import static com.gachigage.global.error.ErrorCode.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.gachigage.global.error.CustomException;
import com.gachigage.image.service.ImageService;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.domain.ProductImage;
import com.gachigage.product.domain.ProductLike;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.domain.Region;
import com.gachigage.product.domain.TradeType;
import com.gachigage.product.dto.ProductDetailResponseDto;
import com.gachigage.product.dto.ProductLikeResponseDto;
import com.gachigage.product.dto.ProductListRequestDto;
import com.gachigage.product.dto.ProductListResponseDto;
import com.gachigage.product.dto.ProductModifyRequestDto;
import com.gachigage.product.dto.ProductRegistrationRequestDto;
import com.gachigage.product.repository.ProductCategoryRepository;
import com.gachigage.product.repository.ProductLikeRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

	private final ProductCategoryRepository productCategoryRepository;
	private final ProductRepository productRepository;
	private final MemberRepository memberRepository;
	private final RegionRepository regionRepository;
	private final ImageService imageService;
	private final ProductLikeRepository productLikeRepository;
	private final NaverMapsClient naverMapsClient;

	@Transactional(readOnly = true)
	public Page<ProductListResponseDto> getProducts(ProductListRequestDto requestDto, Long loginMemberId) {

		if (requestDto.getSize() == null || requestDto.getSize() < 0) {
			throw new CustomException(INVALID_INPUT_VALUE, "페이지 크기는 0 이상이어야 합니다.");
		}

		if (requestDto.getPage() == null) {
			throw new CustomException(INVALID_INPUT_VALUE, "조회할 페이지 인덱스를 입력해주세요");
		}

		Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize());
		return productRepository.search(requestDto, pageable, loginMemberId);
	}

	@Transactional
	public ProductLikeResponseDto toggleProductLike(Long loginMemberId, Long productId) {
		Member member = memberRepository.findMemberByOauthId(loginMemberId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 상품입니다"));

		return productLikeRepository.findByMemberAndProduct(member, product).map(productLike -> {
			productLikeRepository.delete(productLike);
			product.decrementLikeCount();
			return new ProductLikeResponseDto(false, product.getLikeCount());
		}).orElseGet(() -> {
			ProductLike productLike = ProductLike.builder().member(member).product(product).build();
			productLikeRepository.save(productLike);
			product.incrementLikeCount();
			return new ProductLikeResponseDto(true, product.getLikeCount());
		});
	}

	@Transactional
	public void modifyProduct(
		Long productId,
		Long loginMemberId,
		Long subCategoryId,
		String title,
		String detail,
		Long stock,
		List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> priceTableDtos,
		TradeType tradeType,
		ProductModifyRequestDto.TradeLocationRegistrationDto tradeLocationDto,
		List<String> imageUrls
	) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 상품입니다"));

		Member member = memberRepository.findMemberByOauthId(loginMemberId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));

		validateAccessorEqualsSeller(product, member);

		ProductCategory category;
		if (subCategoryId == null) {
			category = productCategoryRepository.findById(8L)
				.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 카테고리입니다"));
		} else {
			category = productCategoryRepository.findById(subCategoryId)
				.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 카테고리입니다"));
		}

		List<ProductPrice> prices = priceTableDtos.stream()
			.map(dto -> ProductPrice.builder()
				.quantity(dto.getQuantity())
				.price(dto.getPrice())
				.status(dto.getStatus())
				.build())
			.toList();

		List<ProductImage> images = createProductImages(imageUrls);

		Region region = resolveRegion(product, tradeLocationDto);

		product.modify(
			category,
			title,
			detail,
			stock,
			tradeType,
			tradeLocationDto != null ? tradeLocationDto.getLatitude() : null,
			tradeLocationDto != null ? tradeLocationDto.getLongitude() : null,
			tradeLocationDto != null ? tradeLocationDto.getAddress() : null,
			prices,
			images,
			region
		);
	}

	private List<ProductImage> createProductImages(List<String> imageUrls) {
		if (imageUrls == null || imageUrls.isEmpty()) {
			throw new CustomException(INVALID_INPUT_VALUE, "이미지 등록은 필수입니다.");
		}

		List<ProductImage> images = new java.util.ArrayList<>();
		for (int i = 0; i < imageUrls.size(); i++) {
			images.add(ProductImage.builder()
				.imageUrl(imageUrls.get(i))
				.order(i)
				.build());
		}
		return images;
	}

	private Region resolveRegion(
		Product product,
		ProductModifyRequestDto.TradeLocationRegistrationDto dto
	) {
		if (dto == null) {
			return product.getRegion();
		}

		boolean hasCoordinates =
			dto.getLatitude() != null && dto.getLongitude() != null;

		if (hasCoordinates) {
			return getRegion(dto.getLongitude(), dto.getLatitude());
		}

		return product.getRegion();
	}

	@Transactional
	public Product createProduct(Long memberOauthId, Long subCategoryId, String title, String detail, Long stock,
		List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> priceTable, TradeType tradeType,
		ProductRegistrationRequestDto.TradeLocationRegistrationDto preferredTradeLocation, List<String> imageUrls) {

		ProductCategory category = productCategoryRepository.findById(subCategoryId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 카테고리입니다"));
		Member seller = memberRepository.findMemberByOauthId(memberOauthId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));

		Region region = null;
		Double longitude = null;
		Double latitude = null;
		String address = null;

		if (preferredTradeLocation != null) {
			longitude = preferredTradeLocation.getLongitude();
			latitude = preferredTradeLocation.getLatitude();
			address = preferredTradeLocation.getAddress();
			region = getRegion(longitude, latitude);
		}

		List<ProductPrice> priceTables = priceTable.stream()
			.map(priceDto -> ProductPrice.builder()
				.quantity(priceDto.getQuantity())
				.price(priceDto.getPrice())
				.status(priceDto.getStatus())
				.build())
			.toList();

		if (imageUrls == null) {
			throw new CustomException(INVALID_INPUT_VALUE, "이미지 등록은 필수입니다.");
		}

		List<ProductImage> productImages = new java.util.ArrayList<>();
		for (int i = 0; i < imageUrls.size(); i++) {
			productImages.add(ProductImage.builder().imageUrl(imageUrls.get(i)).order(i).build());
		}

		Product product = Product.create(null, seller, category, region, title, detail, stock, tradeType, latitude,
			longitude, address, priceTables, productImages);

		productRepository.save(product);
		return product;
	}

	@Transactional
	public ProductDetailResponseDto getProductDetail(Long productId, Long loginMemberId) {

		// 1. 상품 조회
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 상품입니다"));

		// 2. 조회수 증가 (쓰기 트랜잭션이므로 readOnly 제거)
		product.increaseVisitCount();

		// 3. 연관 상품 조회
		Region region = product.getRegion();
		List<Product> relatedProducts = searchRelatedProducts(
			product.getCategory().getId(),
			region != null ? region.getProvince() : null,
			region != null ? region.getCity() : null,
			productId,
			PageRequest.of(0, 4)
		);

		// 4. 로그인 사용자 조회 (한 번만)
		Member member = null;
		if (loginMemberId != null) {
			member = memberRepository.findMemberByOauthId(loginMemberId)
				.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));
		}

		// 5. 현재 상품 좋아요 여부
		boolean isProductLiked = false;
		if (member != null) {
			isProductLiked = productLikeRepository
				.findByMemberAndProduct(member, product)
				.isPresent();
		}

		// 6. 연관 상품 좋아요 목록 한 번에 조회
		Set<Long> likedRelatedProductIds;

		if (member != null && !relatedProducts.isEmpty()) {
			likedRelatedProductIds = productLikeRepository
				.findAllByMemberAndProductIn(member, relatedProducts)
				.stream()
				.map(productLike -> productLike.getProduct().getId())
				.collect(Collectors.toSet());
		} else {
			likedRelatedProductIds = Set.of();
		}

		// 7. 연관 상품 DTO 변환 (순수 map)
		List<ProductDetailResponseDto.RelatedProductDto> relatedProductDtos =
			relatedProducts.stream()
				.map(relatedProduct ->
					ProductDetailResponseDto.RelatedProductDto.fromEntity(
						relatedProduct,
						likedRelatedProductIds.contains(relatedProduct.getId())
					)
				)
				.collect(Collectors.toList());

		// 8. 최종 DTO 반환
		return ProductDetailResponseDto.fromEntity(
			product,
			isProductLiked,
			relatedProductDtos,
			isSeller(product, member)
		);
	}

	public List<String> saveToBucketAndGetImageUrls(List<MultipartFile> files) {
		return imageService.uploadImage(files);
	}

	@Transactional
	public void deleteProduct(Long productId, Long loginMemberId) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 상품입니다"));

		Member member = memberRepository.findMemberByOauthId(loginMemberId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));

		if (!product.getSeller().getOauthId().equals(member.getOauthId())) {
			throw new CustomException(UNAUTHORIZED_USER, "상품 삭제 권한이 없는 사용자입니다.");
		}
		productRepository.delete(product);
	}

	private List<Product> searchRelatedProducts(Long subCategoryId, String province, String city, Long productId,
		Pageable pageable) {
		List<Product> products = productRepository.findRelatedProducts(subCategoryId, province, city, productId,
			pageable); // TODO : refact
		// TODO : Related 4이하일때 MainCategory로 추가 조회
		return products;
	}

	private Region getRegion(double longitude, double latitude) {

		JsonNode response = naverMapsClient
			.reverseGeocode(longitude, latitude)
			.block();

		log.info("Naver Maps API Response: {}", response);

		if (response == null || !response.has("results") || response.get("results").isEmpty()) {
			throw new CustomException(RESOURCE_NOT_FOUND, "지역 정보를 찾을 수 없습니다.");
		}

		JsonNode legalResult = response.get("results").get(0);
		String lawCode = legalResult.get("code").get("id").asText();

		return regionRepository.findByLawCode(lawCode).orElseGet(() -> createRegionFromApi(legalResult));
	}

	private Region createRegionFromApi(JsonNode legalResult) {

		String province = legalResult.get("region")
			.get("area1")
			.get("name")
			.asText();

		String area2 = legalResult.get("region")
			.get("area2")
			.get("name")
			.asText();

		String lawCode = legalResult.get("code").get("id").asText();

		String city;
		String district = null;

		if (area2.contains(" ")) {
			String[] parts = area2.split(" ");
			city = parts[0];
			district = parts[1];
		} else {
			city = area2;
		}

		log.info("Creating Region - lawCode: {}, province: {}, city: {}, district: {}", lawCode, province, city,
			district);

		Region region = Region.builder()
			.lawCode(lawCode)
			.province(province)
			.city(city)
			.district(district)
			.build();

		return regionRepository.save(region);
	}

	private void validateAccessorEqualsSeller(Product product, Member member) {
		if (!product.getSeller().getId().equals(member.getId())) {
			throw new CustomException(UNAUTHORIZED_USER, "상품 판매자만 점근 가능합니다.");
		}
	}

	private boolean isSeller(Product product, Member member) {
		if (member == null) {
			return false;
		}
		return product.getSeller().getId().equals(member.getId());
	}
}
