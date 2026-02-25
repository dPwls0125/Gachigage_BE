package com.gachigage.trade.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gachigage.trade.domain.Trade;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

	Page<Trade> findAllByBuyerId(Long buyerId, Pageable pageable);


}
