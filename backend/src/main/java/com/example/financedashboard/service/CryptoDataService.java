package com.example.financedashboard.service;

import com.example.financedashboard.model.CryptoItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoDataService {

    private final List<CryptoItem> cachedCryptos = new CopyOnWriteArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 主流币种全称映射
    private static final Map<String, String> CRYPTO_NAMES = Map.of(
        "BTC", "Bitcoin",
        "ETH", "Ethereum",
        "SOL", "Solana",
        "BNB", "Binance Coin",
        "XRP", "Ripple",
        "ADA", "Cardano",
        "DOGE", "Dogecoin"
    );

    @PostConstruct
    public void init() {
        try {
            fetchBinanceCryptoData();
        } catch (Exception e) {
            log.error("Initial Binance Crypto fetch failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3000) // 3 秒频率刷新
    public void fetchBinanceCryptoData() {
        log.debug("Fetching Binance Realtime Crypto Data...");
        try {
            // 使用 URI 变量和 Map 来让 RestTemplate 自动处理编码，确保万无一失
            String url = "https://api.binance.com/api/v3/ticker/24hr?symbols={symbols}";
            String symbolsParam = "[\"BTCUSDT\",\"ETHUSDT\",\"SOLUSDT\",\"BNBUSDT\",\"XRPUSDT\",\"ADAUSDT\",\"DOGEUSDT\"]";
            
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("symbols", symbolsParam);

            String response = restTemplate.getForObject(url, String.class, uriVariables);

            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                if (root.isArray()) {
                    List<CryptoItem> freshCryptos = new ArrayList<>();
                    for (JsonNode node : root) {
                        String symbol = node.path("symbol").asText();
                        String baseAsset = symbol.replace("USDT", ""); // 提取基础货币，如 BTC
                        String fullName = CRYPTO_NAMES.getOrDefault(baseAsset, baseAsset); // 获取全称

                        BigDecimal lastPrice = new BigDecimal(node.path("lastPrice").asText());
                        BigDecimal changePercent = new BigDecimal(node.path("priceChangePercent").asText());

                        // 格式化价格和涨跌幅
                        String formattedPrice = lastPrice.setScale(2, RoundingMode.HALF_UP).toPlainString();
                        String formattedChangePercent = (changePercent.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") +
                                changePercent.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";

                        Map<String, String> displayContent = new HashMap<>();
                        displayContent.put("zh", String.format("%s (%s) $%s | %s", fullName, baseAsset, formattedPrice, formattedChangePercent));
                        displayContent.put("en", String.format("%s (%s) $%s | %s", fullName, baseAsset, formattedPrice, formattedChangePercent));

                        freshCryptos.add(CryptoItem.builder()
                                .symbol(symbol)
                                .lastPrice(lastPrice)
                                .priceChangePercent(changePercent)
                                .isUp(changePercent.compareTo(BigDecimal.ZERO) >= 0)
                                .displayContent(displayContent)
                                .build());
                    }
                    cachedCryptos.clear();
                    cachedCryptos.addAll(freshCryptos);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Binance Crypto Data: {}", e.getMessage());
        }
    }

    public List<CryptoItem> getRealtimeCryptos() {
        return new ArrayList<>(cachedCryptos);
    }
}
