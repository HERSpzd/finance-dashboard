package com.example.financedashboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoItem {
    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal priceChangePercent;
    private boolean isUp;
    private Map<String, String> displayContent;
}
