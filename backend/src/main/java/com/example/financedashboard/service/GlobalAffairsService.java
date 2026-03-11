package com.example.financedashboard.service;

import com.example.financedashboard.model.GlobalNewsItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class GlobalAffairsService {

    private final List<GlobalNewsItem> cachedGlobalNews = new CopyOnWriteArrayList<>();
    private final RestTemplate restTemplate = createSecureRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // 目标 API: Reddit /r/worldnews 原生接口
    private final String REDDIT_URL = "https://www.reddit.com/r/worldnews/new.json?limit=15";
    private final String USER_AGENT = "CyberFinanceDashboard/1.0 (by /u/developer)";

    private static RestTemplate createSecureRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); 
        factory.setReadTimeout(5000);    
        return new RestTemplate(factory);
    }

    @PostConstruct
    public void init() {
        try {
            fetchGlobalAffairs();
        } catch (Exception e) {
            log.error("Initial Global Affairs fetch failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000) // 每 1 分钟拉取一次
    public void fetchGlobalAffairs() {
        log.info("Fetching Global Affairs from Reddit (/r/worldnews)...");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            // 必须设置独特的 User-Agent 否则会报 429
            headers.add("User-Agent", USER_AGENT);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(REDDIT_URL, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode children = root.path("data").path("children");
                
                if (children.isArray() && children.size() > 0) {
                    processRedditItems(children);
                    log.info("Successfully fetched {} global news items from Reddit.", children.size());
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Reddit API Fetch Error: {}", e.getMessage());
        }

        // 最终防御：压入系统警报 Mock 数据
        if (cachedGlobalNews.isEmpty()) {
            pushAlertMockData();
        }
    }

    private void processRedditItems(JsonNode children) {
        List<GlobalNewsItem> freshNews = new ArrayList<>();
        for (JsonNode child : children) {
            JsonNode data = child.path("data");
            String title = data.path("title").asText("");
            long createdUtc = data.path("created_utc").asLong();
            String id = data.path("id").asText(UUID.randomUUID().toString());

            // 转换 Unix 时间戳为 HH:mm
            String formattedTime = java.time.Instant.ofEpochSecond(createdUtc)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(timeFormatter);

            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("zh", title);
            contentMap.put("en", title);

            freshNews.add(GlobalNewsItem.builder()
                    .id(id)
                    .time(formattedTime)
                    .content(contentMap)
                    .type("GLOBAL")
                    .build());
        }
        cachedGlobalNews.clear();
        cachedGlobalNews.addAll(freshNews);
    }

    private void pushAlertMockData() {
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("zh", "[SYSTEM] 国际新闻信号源被干扰，正在重新建立安全连接...");
        contentMap.put("en", "Signal lost. Reconnecting...");

        GlobalNewsItem alertItem = GlobalNewsItem.builder()
                .id("999")
                .time(java.time.LocalTime.now().format(timeFormatter))
                .content(contentMap)
                .type("ALERT")
                .build();
        
        cachedGlobalNews.clear();
        cachedGlobalNews.add(alertItem);
    }

    public List<GlobalNewsItem> getGlobalNews() {
        return new ArrayList<>(cachedGlobalNews);
    }
}
