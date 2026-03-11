package com.example.financedashboard.model;

import java.math.BigDecimal;

public class MarketData {
    private String name;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;

    public MarketData(String name, BigDecimal price, BigDecimal change, BigDecimal changePercent) {
        this.name = name;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }
}
