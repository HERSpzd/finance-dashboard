package com.example.financedashboard.service;

import com.example.financedashboard.model.StockItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockDataService {

    // 使用Map来存储，方便按symbol更新，并保留休市数据
    private final Map<String, StockItem> cachedStocks = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public enum MarketStatus {
        OPEN, CLOSED, LUNCH_BREAK
    }

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("GBK")));
        try {
            // 首次启动时全量获取一次
            fetchAllStockData();
        } catch (Exception e) {
            log.error("Initial Sina Stock fetch failed: {}", e.getMessage());
        }
    }
    
    private void fetchAllStockData() {
        String url = "http://hq.sinajs.cn/list=sh000001,sz399001,sz399006,sh000300,gb_dji,gb_ixic,gb_inx";
        fetchData(url);
    }

    @Scheduled(fixedRate = 2000)
    public void scheduledFetchStockData() {
        MarketStatus aShareStatus = getAShareMarketStatus();
        MarketStatus usShareStatus = getUSShareMarketStatus();

        List<String> symbolsToFetch = new ArrayList<>();
        if (aShareStatus != MarketStatus.CLOSED) {
            symbolsToFetch.addAll(List.of("sh000001", "sz399001", "sz399006", "sh000300"));
        }
        if (usShareStatus == MarketStatus.OPEN) {
            symbolsToFetch.addAll(List.of("gb_dji", "gb_ixic", "gb_inx"));
        }

        if (symbolsToFetch.isEmpty()) {
            if (!cachedStocks.isEmpty() && cachedStocks.values().stream().anyMatch(s -> s.getMarketStatus() != MarketStatus.CLOSED.name())) {
                log.debug("All stock markets are closed. Updating status of cached stocks to CLOSED.");
                cachedStocks.values().forEach(stock -> stock.setMarketStatus(MarketStatus.CLOSED.name()));
            }
            return;
        }
        
        String url = "http://hq.sinajs.cn/list=" + String.join(",", symbolsToFetch);
        fetchData(url);
    }

    private void fetchData(String url) {
        log.debug("Fetching Sina Stock Data from URL: {}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Referer", "https://finance.sina.com.cn");
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();

            if (response != null && !response.isEmpty()) {
                parseSinaResponseAndUpdateCache(response);
            }
        } catch (Exception e) {
            log.error("Error fetching Sina Stock Data: {}", e.getMessage());
        }
    }

    private void parseSinaResponseAndUpdateCache(String rawData) {
        String[] lines = rawData.split("\\n");
        MarketStatus aShareStatus = getAShareMarketStatus();
        MarketStatus usShareStatus = getUSShareMarketStatus();

        for (String line : lines) {
            try {
                if (!line.contains("\"")) continue;
                
                String fullSymbol = line.substring(line.indexOf("str_") + 4, line.indexOf("="));
                String content = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                String[] parts = content.split(",");
                
                if (parts.length < 4) continue;

                String market;
                MarketStatus currentMarketStatus;
                String symbol;

                if (fullSymbol.startsWith("sh") || fullSymbol.startsWith("sz")) {
                    market = "A_SHARE";
                    currentMarketStatus = aShareStatus;
                    symbol = fullSymbol.substring(0, 2).toUpperCase() + fullSymbol.substring(2);
                } else if (fullSymbol.startsWith("gb_")) {
                    market = "US_SHARE";
                    currentMarketStatus = usShareStatus;
                    symbol = fullSymbol.substring(3).toUpperCase();
                } else {
                    continue;
                }

                String name = parts[0];
                BigDecimal currentPrice;
                BigDecimal changeValue;
                BigDecimal changePercent;

                if ("A_SHARE".equals(market)) {
                    currentPrice = new BigDecimal(parts[3]);
                    BigDecimal yesterdayClose = new BigDecimal(parts[2]);
                    changeValue = currentPrice.subtract(yesterdayClose);
                    changePercent = yesterdayClose.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                                    changeValue.divide(yesterdayClose, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                } else { // US_SHARE
                    currentPrice = new BigDecimal(parts[1]);
                    changeValue = new BigDecimal(parts[4]);
                    changePercent = new BigDecimal(parts[2]);
                }

                boolean isUp = changeValue.compareTo(BigDecimal.ZERO) >= 0;

                Map<String, String> displayContent = new HashMap<>();
                displayContent.put("zh", String.format("%s: %.2f (%s%.2f%%)", name, currentPrice, isUp ? "+" : "", changePercent));
                displayContent.put("en", String.format("%s: %.2f (%s%.2f%%)", symbol, currentPrice, isUp ? "+" : "", changePercent));

                StockItem stockItem = StockItem.builder()
                        .symbol(symbol)
                        .name(name)
                        .currentPrice(currentPrice)
                        .changeValue(changeValue)
                        .changePercent(changePercent)
                        .isUp(isUp)
                        .displayContent(displayContent)
                        .market(market)
                        .marketStatus(currentMarketStatus.name())
                        .build();
                
                cachedStocks.put(symbol, stockItem);

            } catch (Exception e) {
                log.error("Failed to parse stock line: {}, error: {}", line, e.getMessage());
            }
        }
        log.debug("Successfully updated/created {} stock indices in cache.", lines.length);
    }

    public List<StockItem> getRealtimeStocks() {
        return cachedStocks.values().stream()
                .sorted(Comparator.comparing(StockItem::getMarket).reversed()
                .thenComparing(StockItem::getSymbol))
                .collect(Collectors.toList());
    }

    private MarketStatus getAShareMarketStatus() {
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        ZonedDateTime now = ZonedDateTime.now(shanghaiZone);
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return MarketStatus.CLOSED;
        }

        LocalTime morningOpen = LocalTime.of(9, 30);
        LocalTime morningClose = LocalTime.of(11, 30);
        LocalTime afternoonOpen = LocalTime.of(13, 0);
        LocalTime afternoonClose = LocalTime.of(15, 0);

        if ((time.isAfter(morningOpen) && time.isBefore(morningClose)) ||
            (time.isAfter(afternoonOpen) && time.isBefore(afternoonClose))) {
            return MarketStatus.OPEN;
        } else if (time.isAfter(morningClose) && time.isBefore(afternoonOpen)) {
            return MarketStatus.LUNCH_BREAK;
        } else {
            return MarketStatus.CLOSED;
        }
    }

    private MarketStatus getUSShareMarketStatus() {
        ZoneId newYorkZone = ZoneId.of("America/New_York");
        ZonedDateTime now = ZonedDateTime.now(newYorkZone);
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return MarketStatus.CLOSED;
        }

        LocalTime openTime = LocalTime.of(9, 30);
        LocalTime closeTime = LocalTime.of(16, 0);

        if (time.isAfter(openTime) && time.isBefore(closeTime)) {
            return MarketStatus.OPEN;
        } else {
            return MarketStatus.CLOSED;
        }
    }
}
