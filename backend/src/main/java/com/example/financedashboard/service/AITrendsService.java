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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class AITrendsService {

    private final List<GlobalNewsItem> cachedAINews = new CopyOnWriteArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @PostConstruct
    public void init() {
        try {
            fetchAITrends();
        } catch (Exception e) {
            log.error("Initial AI Trends fetch failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000) // 每 1 分钟拉取一次
    public void fetchAITrends() {
        log.info("Fetching AI Trends from Hacker News Algolia API...");
        try {
            String url = "https://hn.algolia.com/api/v1/search_by_date?query=AI OR OpenAI OR LLM&tags=story&hitsPerPage=10";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("User-Agent", "CyberDashboard/1.0");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode hits = root.path("hits");
                
                if (hits.isArray() && hits.size() > 0) {
                    List<GlobalNewsItem> freshNews = new ArrayList<>();
                    for (JsonNode hit : hits) {
                        try {
                            String title = hit.path("title").asText();
                            long createdAtI = hit.path("created_at_i").asLong();
                            String objectID = hit.path("objectID").asText();

                            String formattedTime = Instant.ofEpochSecond(createdAtI)
                                    .atZone(ZoneId.systemDefault())
                                    .format(timeFormatter);

                            Map<String, String> contentMap = new HashMap<>();
                            contentMap.put("zh", "[翻译功能敬请期待] " + title);
                            contentMap.put("en", title);

                            freshNews.add(GlobalNewsItem.builder()
                                    .id(objectID)
                                    .time(formattedTime)
                                    .content(contentMap)
                                    .type("AI")
                                    .build());
                        } catch (Exception e) {
                            log.error("Failed to parse a Hacker News item: {}", e.getMessage());
                        }
                    }
                    cachedAINews.clear();
                    cachedAINews.addAll(freshNews);
                    log.info("Successfully fetched {} AI trends from Hacker News.", freshNews.size());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching AI Trends: {}", e.getMessage());
        }
    }

    public List<GlobalNewsItem> getAINews() {
        return new ArrayList<>(cachedAINews);
    }
}
