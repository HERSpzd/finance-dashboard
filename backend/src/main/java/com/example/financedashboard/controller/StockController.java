package com.example.financedashboard.controller;

import com.example.financedashboard.model.StockItem;
import com.example.financedashboard.service.StockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final StockDataService stockDataService;

    @GetMapping("/realtime")
    public List<StockItem> getRealtimeStocks() {
        return stockDataService.getRealtimeStocks();
    }
}
