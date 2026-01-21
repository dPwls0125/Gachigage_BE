package com.gachigage.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gachigage.product.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
