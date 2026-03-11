package com.example.financedashboard.service;

import com.example.financedashboard.model.GlobalNewsItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class TechTrendsService {

    private final List<GlobalNewsItem> cachedTechNews = new CopyOnWriteArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @PostConstruct
    public void init() {
        try {
            fetchTechTrends();
        } catch (Exception e) {
            log.error("Initial Tech Trends fetch failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 300000) // 每 5 分钟拉取一次
    public void fetchTechTrends() {
        log.info("Fetching Tech Trends from V2EX API...");
        try {
            String url = "https://www.v2ex.com/api/topics/show.json?node_name=programmer";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                if (root.isArray() && root.size() > 0) {
                    List<GlobalNewsItem> freshNews = new ArrayList<>();
                    for (JsonNode item : root) {
                        try {
                            String title = item.path("title").asText();
                            long created = item.path("created").asLong();
                            String id = item.path("id").asText();

                            String formattedTime = Instant.ofEpochSecond(created).atZone(ZoneId.systemDefault()).format(timeFormatter);

                            Map<String, String> contentMap = new HashMap<>();
                            contentMap.put("zh", "[V2EX] " + title);
                            contentMap.put("en", "Waiting for translation...");

                            freshNews.add(GlobalNewsItem.builder()
                                    .id(id)
                                    .time(formattedTime)
                                    .content(contentMap)
                                    .type("TECH")
                                    .build());
                        } catch (Exception e) {
                            log.error("Failed to parse a V2EX item: {}", e.getMessage());
                        }
                    }
                    cachedTechNews.clear();
                    cachedTechNews.addAll(freshNews);
                    log.info("Successfully fetched {} tech trends from V2EX.", freshNews.size());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Tech Trends from V2EX: {}", e.getMessage());
            pushMockErrorData();
        }
    }

    private void pushMockErrorData() {
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("zh", "[系统提示] V2EX 节点连接异常");
        contentMap.put("en", "[System] Failed to connect to V2EX.");

        GlobalNewsItem errorItem = GlobalNewsItem.builder()
                .id("error-v2ex-" + System.currentTimeMillis())
                .time(OffsetDateTime.now().format(timeFormatter))
                .content(contentMap)
                .type("TECH")
                .build();
        
        cachedTechNews.clear();
        cachedTechNews.add(errorItem);
    }

    public List<GlobalNewsItem> getTechNews() {
        return new ArrayList<>(cachedTechNews);
    }
}
