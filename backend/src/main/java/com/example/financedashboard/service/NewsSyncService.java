package com.example.financedashboard.service;

import com.example.financedashboard.model.NewsItem;
import com.example.financedashboard.websocket.NewsWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsSyncService {

    private final List<NewsItem> cachedWallStreetNews = Collections.synchronizedList(new ArrayList<>());
    private final List<NewsItem> cachedYahooNews = Collections.synchronizedList(new ArrayList<>());
    private final NewsWebSocketHandler newsWebSocketHandler;
    
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        try {
            syncNews();
        } catch (Exception e) {
            log.error("Initial NewsSync failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 30000) // 提高频率到 30 秒
    public void syncNews() {
        log.info("Starting Real-time API synchronization (30s interval)...");
        
        // 1. 直连华尔街见闻底层 API
        fetchWallStreetCNRealtime();

        // 2. 直连雅虎财经底层 JSON API
        fetchYahooRealtime(); 
    }

    private void fetchYahooRealtime() {
        log.debug("Fetching International News (Nasdaq API) for Yahoo Card...");
        try {
            // 使用 Nasdaq 官方 API 获取国际市场新闻，确保内容与华尔街见闻完全不同
            String url = "https://api.nasdaq.com/api/news/topic/article?topic=markets&count=20";
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                List<Map<String, Object>> rows = (List<Map<String, Object>>) data.get("rows");
                
                List<NewsItem> news = rows.stream().map(item -> {
                    try {
                        String title = (String) item.get("title");
                        String id = String.valueOf(item.get("id"));
                        
                        // 解析 Nasdaq 的 "ago" 字段 (例如 "7 minutes ago")
                        long timestamp = System.currentTimeMillis();
                        String ago = (String) item.get("ago");
                        if (ago != null && ago.contains("minute")) {
                            int mins = Integer.parseInt(ago.replaceAll("[^0-9]", ""));
                            timestamp -= (long) mins * 60 * 1000;
                        } else if (ago != null && ago.contains("hour")) {
                            int hours = Integer.parseInt(ago.replaceAll("[^0-9]", ""));
                            timestamp -= (long) hours * 60 * 60 * 1000;
                        }

                        Map<String, String> contentMap = new HashMap<>();
                        contentMap.put("en", title);
                        contentMap.put("zh", "[国际快讯] " + title); // 标记为国际源
                        
                        return NewsItem.builder()
                                .id(id)
                                .time(timestamp)
                                .content(contentMap)
                                .source("Nasdaq")
                                .build();
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

                updateCacheAndPush("yahoo", cachedYahooNews, news);
            }
        } catch (Exception e) {
            log.error("Nasdaq API Fetch Error: {}", e.getMessage());
        }
    }

    private void fetchWallStreetCNRealtime() {
        log.debug("Fetching WallStreetCN from Realtime API...");
        try {
            // 目标 URL (导师提供的底层接口)
            String url = "https://api-haiguan.wallstreetcn.com/apiv1/content/fabricate-articles?channel=global-channel&limit=20";
            
            // 3. 伪装与反爬虫：添加 User-Agent
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            headers.add("Referer", "https://wallstreetcn.com/");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发起 GET 请求
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            
            if (response != null && "20000".equals(String.valueOf(response.get("code")))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
                
                // 4. 解析 JSON 数据映射
                List<NewsItem> news = items.stream().map(item -> {
                    try {
                        // 导师提到资源在 resource 字段下
                        Map<String, Object> resource = (Map<String, Object>) item.get("resource");
                        if (resource == null) return null;

                        String title = (String) resource.get("title");
                        String contentText = (String) resource.get("content_text");
                        // 优先取正文，没正文取标题
                        String zhContent = (contentText != null && !contentText.isEmpty()) ? contentText : title;
                        
                        Map<String, String> contentMap = new HashMap<>();
                        contentMap.put("zh", zhContent);
                        contentMap.put("en", "[等待翻译] " + zhContent);
                        
                        return NewsItem.builder()
                                .id(String.valueOf(resource.get("id")))
                                .time(Long.parseLong(String.valueOf(resource.get("display_time"))) * 1000)
                                .content(contentMap)
                                .source("WallStreetCN")
                                .build();
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

                // 检查更新并推送 WebSocket
                updateCacheAndPush("wallstreet", cachedWallStreetNews, news);
            }
        } catch (Exception e) {
            log.error("API Fetch Error: {}. Switching to secondary API fallback...", e.getMessage());
            fetchWallStreetCNFallback(); // 如果主 API 失败（如 DNS 问题），尝试备用 API
        }
    }

    private void fetchWallStreetCNFallback() {
        try {
            // 备用稳定接口
            String url = "https://api-prod.wallstreetcn.com/apiv1/content/articles?channel=global&limit=20";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "20000".equals(String.valueOf(response.get("code")))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
                List<NewsItem> news = items.stream().map(item -> {
                    String title = (String) item.get("title");
                    String contentShort = (String) item.get("content_short");
                    String zhContent = (title != null && !title.isEmpty()) ? title : contentShort;
                    Map<String, String> contentMap = new HashMap<>();
                    contentMap.put("zh", zhContent);
                    contentMap.put("en", "[Translation pending] " + zhContent);
                    return NewsItem.builder()
                            .id(String.valueOf(item.get("id")))
                            .time(Long.parseLong(String.valueOf(item.get("display_time"))) * 1000)
                            .content(contentMap)
                            .source("WallStreetCN-Fallback")
                            .build();
                }).collect(Collectors.toList());
                updateCacheAndPush("wallstreet", cachedWallStreetNews, news);
            }
        } catch (Exception e) {
            log.error("Secondary API also failed: {}", e.getMessage());
        }
    }

    private void updateCacheAndPush(String type, List<NewsItem> cache, List<NewsItem> freshData) {
        if (freshData.isEmpty()) return;

        boolean hasNew = false;
        synchronized (cache) {
            if (cache.isEmpty() || !freshData.get(0).getId().equals(cache.get(0).getId())) {
                hasNew = true;
            }
            cache.clear();
            cache.addAll(freshData);
        }

        if (hasNew) {
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", type);
            pushData.put("data", freshData.get(0));
            newsWebSocketHandler.broadcastNews(pushData);
            log.info("REALTIME PUSH: New {} news detected and broadcasted.", type);
        }
    }

    public List<NewsItem> getWallStreetNews() {
        return new ArrayList<>(cachedWallStreetNews);
    }

    public List<NewsItem> getYahooNews() {
        return new ArrayList<>(cachedYahooNews);
    }
}
