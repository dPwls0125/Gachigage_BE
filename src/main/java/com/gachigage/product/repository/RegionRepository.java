package com.gachigage.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gachigage.product.domain.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
