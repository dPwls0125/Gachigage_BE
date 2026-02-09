package com.gachigage.member.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.image.service.ImageService;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.dto.response.MyProfileResponseDto;
import com.gachigage.member.dto.response.ProfileImageResponseDto;
import com.gachigage.member.dto.response.TradeResponseDto;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductLike;
import com.gachigage.product.dto.ProductListResponseDto;
import com.gachigage.product.repository.ProductLikeRepository;
import com.gachigage.product.repository.ProductRepository;
import com.gachigage.trade.domain.Trade;
import com.gachigage.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

	private final MemberRepository memberRepository;
	private final TradeRepository tradeRepository;
	private final ImageService imageService;
	private final ProductLikeRepository productLikeRepository;
	private final ProductRepository productRepository;

	public MyProfileResponseDto getMyProfile(Long oauthId) {
		Member member = memberRepository.findMemberByOauthId(oauthId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		return MyProfileResponseDto.builder()
			.userId(member.getId())
			.name(member.getName())
			.nickname(member.getNickname())
			.profileImage(member.getImageUrl())
			.email(member.getEmail())
			.createdAt(member.getCreatedAt())
			.build();
	}

	@Transactional
	public void updateNickname(Long oauthId, String newNickname) {
		Member member = memberRepository.findMemberByOauthId(oauthId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		member.updateNickname(newNickname);
	}

	public Page<TradeResponseDto> getPurchaseHistory(Long oauthId, Pageable pageable) {
		Member member = memberRepository.findMemberByOauthId(oauthId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		return tradeRepository.findAllByBuyerId(member.getId(), pageable)
			.map(this::toTradeResponseDto);
	}

	public Page<ProductListResponseDto> getMySalesProducts(Long oauthId, Pageable pageable) {
		Member member = memberRepository.findMemberByOauthId(oauthId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Page<Product> products = productRepository.findAllBySellerId(member.getId(), pageable);

		return products.map(this::toProductListResponseDto);
	}

	private TradeResponseDto toTradeResponseDto(Trade trade) {
		Product product = trade.getProduct();

		int price = trade.getProductPrice().getPrice();
		int quantity = trade.getProductPrice().getQuantity();

		String thumbnailUrl = null;
		if (product.getImages() != null && !product.getImages().isEmpty()) {
			thumbnailUrl = product.getImages().get(0).getImageUrl();
		}

		return TradeResponseDto.builder()
			.tradeId(trade.getId())
			.productId(product.getId())
			.title(product.getTitle())
			.price(price)
			.quantity(quantity)
			.thumbnailUrl(thumbnailUrl)
			.tradeDate(trade.getCreatedAt())
			.status(String.valueOf(trade.getStatus()))
			.build();
	}

	public Page<ProductListResponseDto> getMyLikes(Long oauthId, Pageable pageable) {
		Member member = memberRepository.findMemberByOauthId(oauthId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Page<ProductLike> likes = productLikeRepository.findAllByMemberId(member.getId(), pageable);

		return likes.map(like -> {
			Product product = like.getProduct();

			String mainImageUrl = (product.getImages() != null && !product.getImages().isEmpty())
				? product.getImages().get(0).getImageUrl()
				: null;

			int price = (product.getPrices() != null && !product.getPrices().isEmpty())
				? product.getPrices().get(0).getPrice()
				: 0;

			return new ProductListResponseDto(
				product.getId(),
				product.getTitle(),
				mainImageUrl,
				product.getRegion() != null ? product.getRegion().getProvince() : null,
				product.getRegion() != null ? product.getRegion().getCity() : null,
				null,
				product.getTradeType(),
				price,
				product.getStock().intValue(),
				product.getVisitCount(),
				true,
				product.getCreatedAt()
			);
		});
	}

	private ProductListResponseDto toProductListResponseDto(Product product) {
		String mainImageUrl = (product.getImages() != null && !product.getImages().isEmpty())
			? product.getImages().get(0).getImageUrl() : null;

		int price = (product.getPrices() != null && !product.getPrices().isEmpty())
			? product.getPrices().get(0).getPrice() : 0;

		return new ProductListResponseDto(
			product.getId(),
			product.getTitle(),
			mainImageUrl,
			product.getRegion() != null ? product.getRegion().getProvince() : null,
			product.getRegion() != null ? product.getRegion().getCity() : null,
			null,
			product.getTradeType(),
			price,
			product.getStock().intValue(),
			product.getVisitCount(),
			true,
			product.getCreatedAt()
		);
	}

	@Transactional
	public ProfileImageResponseDto updateProfileImage(Long oauthId, MultipartFile file) {
		Member member = memberRepository.findMemberByOauthId(oauthId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		String imageUrl = imageService.uploadImage(List.of(file)).get(0);

		member.updateProfileImage(imageUrl);

		return ProfileImageResponseDto.builder()
			.imageUrl(imageUrl)
			.build();
	}
}
