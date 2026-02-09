package com.gachigage.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gachigage.member.Member;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductLike;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
	Optional<ProductLike> findByMemberAndProduct(Member member, Product product);

	Optional<ProductLike> findByMemberOauthIdAndProduct(Long memberOauthId, Product product);

	List<ProductLike> findAllByMemberAndProductIn(Member member, List<Product> products);

	Page<ProductLike> findAllByMemberId(Long memberId, Pageable pageable);
}
