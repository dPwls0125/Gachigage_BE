package com.gachigage.trade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gachigage.trade.domain.TradeItem;

@Repository
public interface TradeItemRepository extends JpaRepository<TradeItem, Long> {
}
