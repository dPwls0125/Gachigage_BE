package com.gachigage.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gachigage.member.Member;
import com.gachigage.product.domain.Product;
import com.gachigage.product.domain.ProductLike;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
	Optional<ProductLike> findByMemberAndProduct(Member member, Product product);
}
