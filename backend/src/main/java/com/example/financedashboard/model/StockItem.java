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
public class StockItem {
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal changeValue;
    private BigDecimal changePercent;
    private boolean isUp;
    private Map<String, String> displayContent;
    private String market; // A_SHARE, US_SHARE
    private String marketStatus; // OPEN, CLOSED, LUNCH_BREAK
}
