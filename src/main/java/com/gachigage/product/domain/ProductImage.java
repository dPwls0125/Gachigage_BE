package com.gachigage.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int order; // TODO : s3 구현시, ordering 필요.

    @Builder
    public ProductImage(Product product, String imageUrl, int order) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.order = order;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
