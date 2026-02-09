package com.gachigage.product.repository;

import static com.gachigage.product.domain.QProduct.*;
import static com.gachigage.product.domain.QProductImage.*;
import static com.gachigage.product.domain.QProductLike.*;
import static com.gachigage.product.domain.QProductPrice.*;
import static com.gachigage.product.domain.QRegion.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.gachigage.product.domain.ProductCategory;
import com.gachigage.product.dto.ProductListRequestDto;
import com.gachigage.product.dto.ProductListResponseDto;
import com.gachigage.product.dto.QProductListResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final ProductCategoryRepository productCategoryRepository;

	@Override
	public Page<ProductListResponseDto> search(ProductListRequestDto requestDto, Pageable pageable,
		Long loginMemberId) {
		List<ProductListResponseDto> content = queryFactory
			.select(new QProductListResponseDto(
				product.id,
				product.title,
				productImage.imageUrl,
				region.province,
				region.city,
				new CaseBuilder()
					.when(productPrice.quantity.goe(2)).then("일괄")
					.otherwise("개별"),
				product.tradeType,
				productPrice.price,
				productPrice.quantity,
				product.visitCount,
				new CaseBuilder()
					.when(loginMemberId != null ? JPAExpressions
						.selectOne()
						.from(productLike)
						.where(productLike.product.eq(product).and(productLike.member.oauthId.eq(loginMemberId)))
						.exists()
						:
						Expressions.asBoolean(false).isTrue()
					)
					.then(true)
					.otherwise(false),
				product.createdAt
			))
			.from(productPrice)
			.join(productPrice.product, product)
			.leftJoin(productImage).on(productImage.product.eq(product).and(productImage.order.eq(0)))
			.leftJoin(product.region, region)
			.where(
				titleContains(requestDto.getQuery()),
				categoryEq(requestDto.getCategoryId()),
				priceBetween(requestDto.getPriceArrange()),
				locationEq(requestDto.getLocationDto()),
				groupEq(requestDto.getGroup()),
				stockGtZero(),
				priceTableStatusActive()
			)
			.orderBy(product.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(productPrice.count())
			.from(productPrice)
			.join(productPrice.product, product)
			.leftJoin(product.region, region)
			.where(
				titleContains(requestDto.getQuery()),
				categoryEq(requestDto.getCategoryId()),
				priceBetween(requestDto.getPriceArrange()),
				locationEq(requestDto.getLocationDto()),
				groupEq(requestDto.getGroup()),
				stockGtZero(),
				priceTableStatusActive()
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total);
	}

	private BooleanExpression titleContains(String query) {
		return query != null ? product.title.containsIgnoreCase(query) : null;
	}

	private BooleanExpression categoryEq(Long categoryId) {
		if (categoryId == null) {
			return null;
		}

		ProductCategory category = productCategoryRepository.findById(categoryId).orElse(null);
		if (category == null) {
			return null;
		}

		if (category.getParent() == null) { // This is a main category
			Set<Long> descendantCategoryIds = new HashSet<>();
			descendantCategoryIds.add(categoryId); // Include the main category itself
			getAllDescendantCategoryIds(categoryId, descendantCategoryIds);
			return product.category.id.in(descendantCategoryIds);
		} else {
			return product.category.id.eq(categoryId);
		}
	}

	private void getAllDescendantCategoryIds(Long parentId, Set<Long> descendantCategoryIds) {
		List<ProductCategory> children = productCategoryRepository.findByParentId(parentId);
		for (ProductCategory child : children) {
			descendantCategoryIds.add(child.getId());
			getAllDescendantCategoryIds(child.getId(), descendantCategoryIds);
		}
	}

	private BooleanExpression priceBetween(ProductListRequestDto.PriceArrangeDto priceArrange) {
		if (priceArrange == null || (priceArrange.getMinPrice() == null && priceArrange.getMaxPrice() == null)) {
			return null; // No price range filter applies
		}

		Integer minPrice = priceArrange.getMinPrice();
		Integer maxPrice = priceArrange.getMaxPrice();

		if (minPrice != null && maxPrice != null) {
			return productPrice.price.between(minPrice, maxPrice);
		} else if (minPrice != null) {
			return productPrice.price.goe(minPrice);
		} else { // maxPrice != null
			return productPrice.price.loe(maxPrice);
		}
	}

	private BooleanExpression locationEq(ProductListRequestDto.LocationDto locationDto) {
		if (locationDto == null) {
			return null;
		}

		String province = locationDto.getProvince();
		String city = locationDto.getCity();

		BooleanExpression provinceExpression = (province != null) ? region.province.eq(province) : null;
		BooleanExpression cityExpression = (city != null) ? region.city.eq(city) : null;

		return Expressions.allOf(provinceExpression, cityExpression);
	}

	private BooleanExpression groupEq(String group) {
		if (group == null || group.equals("전체")) {
			return null;
		} else if (group.equals("일괄")) {
			return productPrice.quantity.goe(2);
		} else if (group.equals("개별")) {
			return productPrice.quantity.eq(1);
		}
		return null;
	}

	private BooleanExpression stockGtZero() {
		return product.stock.gt(0);
	}

	private BooleanExpression priceTableStatusActive() {
		return productPrice.status.eq(com.gachigage.product.domain.PriceTableStatus.ACTIVE);
	}

	@Override
	public List<com.gachigage.product.domain.Product> findRelatedProducts(Long categoryId, String province, String city,
		Long productId, Pageable pageable) {

		BooleanExpression predicate = product.category.id.eq(categoryId)
			.and(product.id.ne(productId));

		if (province != null && city != null) {
			predicate = predicate.and(region.province.eq(province)
				.and(region.city.eq(city)));
		}

		return queryFactory
			.selectFrom(product)
			.leftJoin(product.region, region).fetchJoin()
			.join(product.category).fetchJoin()
			.where(predicate)
			.orderBy(product.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

}
