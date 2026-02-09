package com.gachigage.member.service;


import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.dto.response.SellerProfileResponseDto;
import com.gachigage.member.dto.response.TradeResponseDto;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductStatus;
import com.gachigage.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public SellerProfileResponseDto getSellerProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return SellerProfileResponseDto.builder()
                .userId(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getImageUrl())
                .build();
    }
    public Page<TradeResponseDto> getSellerProducts(Long memberId, String status, Pageable pageable) {
        Page<Product> products;


        if (status != null && !status.isBlank()) {

            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            products = productRepository.findAllBySellerIdAndStatus(memberId, productStatus, pageable);
        }

        else {
            products = productRepository.findAllBySellerId(memberId, pageable);
        }


        return products.map(this::toTradeResponseDto);
    }

    private TradeResponseDto toTradeResponseDto(Product product) {

        String thumbnailUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getImageUrl()
                : null;


        int price = (product.getPrices() != null && !product.getPrices().isEmpty())
                ? product.getPrices().get(0).getPrice()
                : 0;

        return TradeResponseDto.builder()
                .tradeId(null)
                .productId(product.getId())
                .title(product.getTitle())
                .price(price)
                .thumbnailUrl(thumbnailUrl)
                .status(String.valueOf(product.getStatus()))
                .build();
    }
}
