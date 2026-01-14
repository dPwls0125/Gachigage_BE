package com.gachigage.product.dto;

import com.gachigage.product.domain.Category;
import com.gachigage.product.domain.PriceTable;
import com.gachigage.product.domain.TradeLocation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequestDto {

    private Category category;
    private String title;
    private String detail;
    private List<PriceTable> priceTable;
    private List<String> tradeTypes;
    private List<TradeLocation> preferredTradeLocations;
    private List<String> imageUrls;

}
