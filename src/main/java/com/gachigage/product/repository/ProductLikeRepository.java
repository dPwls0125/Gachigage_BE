package com.gachigage.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gachigage.product.domain.ProductLike;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
}
