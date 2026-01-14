package com.gachigage.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId; // TODO: 추후 User 엔티티와 연관관계 설정

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String detail;

    @Embedded
    private Category category;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "price_table", joinColumns = @JoinColumn(name = "product_id"))
    private List<PriceTable> priceTable = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "trade_type", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "trade_type")
    private List<String> tradeTypes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "trade_location", joinColumns = @JoinColumn(name = "product_id"))
    private List<TradeLocation> preferredTradeLocations = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_image", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    private int viewCount = 0;

    private int likeCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Product(Long sellerId, String title, String detail, Category category, List<PriceTable> priceTable, List<String> tradeTypes, List<TradeLocation> preferredTradeLocations, List<String> imageUrls) {
        this.sellerId = sellerId;
        this.title = title;
        this.detail = detail;
        this.category = category;
        this.priceTable = priceTable;
        this.tradeTypes = tradeTypes;
        this.preferredTradeLocations = preferredTradeLocations;
        this.imageUrls = imageUrls;
    }
}
