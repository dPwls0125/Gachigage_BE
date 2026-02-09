package com.gachigage.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gachigage.product.domain.ProductPrice;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {
}
