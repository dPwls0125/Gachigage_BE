package com.gachigage.product.service;

import static com.gachigage.global.error.ErrorCode.*;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gachigage.global.error.CustomException;
import com.gachigage.image.service.ImageService;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.product.domain.PriceTableStatus;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.domain.ProductImage;
import com.gachigage.product.domain.ProductLike;
import com.gachigage.product.domain.ProductPrice;
import com.gachigage.product.domain.Region;
import com.gachigage.product.domain.TradeType;
import com.gachigage.product.dto.ProductDetailResponseDto;
import com.gachigage.product.dto.ProductLikeResponseDto;
import com.gachigage.product.dto.ProductModifyRequestDto;
import com.gachigage.product.dto.ProductRegistrationRequestDto;
import com.gachigage.product.repository.ProductCategoryRepository;
import com.gachigage.product.repository.ProductLikeRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.product.repository.RegionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductCategoryRepository productCategoryRepository;
	private final ProductRepository productRepository;
	private final MemberRepository memberRepository;
	private final RegionRepository regionRepository;
	private final ImageService imageService;
	private final ProductLikeRepository productLikeRepository;

	@Transactional
	public ProductLikeResponseDto toggleProductLike(Long loginMemberId, Long productId) {
		Member member = memberRepository.findMemberByOauthId(loginMemberId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 상품입니다"));

		return productLikeRepository.findByMemberAndProduct(member, product)
			.map(productLike -> {
				productLikeRepository.delete(productLike);
				product.decrementLikeCount();
				return new ProductLikeResponseDto(false, product.getLikeCount());
			})
			.orElseGet(() -> {
				ProductLike productLike = ProductLike.builder()
					.member(member)
					.product(product)
					.build();
				productLikeRepository.save(productLike);
				product.incrementLikeCount();
				return new ProductLikeResponseDto(true, product.getLikeCount());
			});
	}

	@Transactional
	public Product modifyProduct(
		Long productId,
		Long loginMemberId,
		Long subCategoryId,
		String title,
		String detail,
		Long stock,
		List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> priceTableDtos,
		TradeType tradeType,
		ProductModifyRequestDto.TradeLocationRegistrationDto preferredTradeLocationDto,
		List<String> imageUrls
	) {

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 상품입니다"));

		Member member = memberRepository.findById(loginMemberId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));

		if (!product.getSeller().getId().equals(member.getId())) {
			throw new CustomException(UNAUTHORIZED_USER, "상품 수정 권한이 없는 사용자입니다.");
		}

		ProductCategory newCategory = productCategoryRepository.findById(subCategoryId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 카테고리입니다"));

		List<ProductPrice> newPrices = priceTableDtos.stream()
			.map(priceDto -> ProductPrice.builder()
				.quantity(priceDto.getQuantity())
				.price(priceDto.getPrice())
				.status(priceDto.getStatus())
				.build()).toList();

		List<ProductImage> newProductImages = imageUrls.stream()
			.map(url -> ProductImage.builder()
				.imageUrl(url)
				.build()).toList();

		product.modify(
			newCategory,
			title,
			detail,
			stock,
			tradeType,
			preferredTradeLocationDto.getLatitude(),
			preferredTradeLocationDto.getLongitude(),
			preferredTradeLocationDto.getAddress(),
			newPrices,
			newProductImages
		);

		return product;
	}

	@Transactional
	public Product createProduct(
		Long memberOauthId,
		Long subCategoryId,
		String title,
		String detail,
		Long stock,
		List<ProductRegistrationRequestDto.ProductPriceRegistrationDto> priceTable,
		TradeType tradeType,
		ProductRegistrationRequestDto.TradeLocationRegistrationDto preferredTradeLocation,
		List<String> imageUrls) {

		ProductCategory category = productCategoryRepository.findById(subCategoryId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 카테고리입니다"));
		Member seller = memberRepository.findMemberByOauthId(memberOauthId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND, "존재하지 않는 회원입니다"));

		// Todo : 시도동으로 외부 API 연동 후 region 설정 _ 이미 존재하는 region인지 확인 필요
		Region region = regionRepository.save(new Region("서울특별시", "강남구", "역삼동")); // dummy data

		List<ProductPrice> priceTables = priceTable.stream()
			.map(priceDto -> ProductPrice.builder()
				.quantity(priceDto.getQuantity())
				.price(priceDto.getPrice())
				.status(PriceTableStatus.ACTIVE)
				.build()).toList();

		List<ProductImage> productImages = imageUrls.stream()
			.map(url -> ProductImage.builder()
				.imageUrl(url)
				.build()).toList();

		Product product = Product.create(
			null,
			seller, category, region, title, detail, stock, tradeType,
			preferredTradeLocation.getLatitude(),
			preferredTradeLocation.getLongitude(),
			preferredTradeLocation.getAddress(),
			priceTables,
			productImages
		);

		productRepository.save(product);
		return product;
	}

	@Transactional(readOnly = true)
	public ProductDetailResponseDto getProductDetail(Long productId) {

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND, "존재하지 않는 상품입니다"));

		Long currentCategoryId = product.getCategory().getId();
		String currentProvince = product.getRegion().getProvince();
		String currentCity = product.getRegion().getCity();

		// TODO : viewCount 증가 로직 관련 test 추가 필요
		product.increaseVisitCount();

		List<Product> relatedProductsEntities = searchRelatedProducts(
			currentCategoryId,
			currentProvince,
			currentCity,
			productId,
			PageRequest.of(0, 4)
		);

		return ProductDetailResponseDto.fromEntity(
			product,
			relatedProductsEntities
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

	private List<Product> searchRelatedProducts(
		Long subCategoryId,
		String province,
		String city,
		Long productId,
		Pageable pageable) {
		List<Product> products = productRepository
			.findRelatedProducts(subCategoryId, province, city, productId, pageable); // TODO : refact
		// TODO : Relatedr 4이하일때 MainCategory로 추가 조회
		return products;
	}
}
