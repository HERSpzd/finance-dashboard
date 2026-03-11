package com.example.financedashboard.service;

import com.example.financedashboard.model.NewsItem;
import com.example.financedashboard.websocket.NewsWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CailianNewsService {

    private final List<NewsItem> cachedCailianNews = new CopyOnWriteArrayList<>();
    private final NewsWebSocketHandler newsWebSocketHandler;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            fetchCailianNews();
        } catch (Exception e) {
            log.error("Initial Cailian News fetch failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 15000) // 每 15 秒拉取一次
    public void fetchCailianNews() {
        log.info("Fetching Cailian Telegraph News...");
        try {
            String url = "https://www.cls.cn/nodeapi/telegraphList?rn=20";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode rollData = root.path("data").path("roll_data");
                
                if (rollData.isArray()) {
                    List<NewsItem> freshNews = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                    for (JsonNode node : rollData) {
                        String id = node.path("id").asText();
                        String title = node.path("title").asText("");
                        String content = node.path("content").asText("");
                        long ctime = node.path("ctime").asLong();

                        String cleanedTitle = title.replaceAll("<[^>]*>", "").trim();
                        String cleanedContent = content.replaceAll("<[^>]*>", "").trim();

                        String zhText;
                        if (cleanedTitle.isEmpty()) {
                            zhText = cleanedContent;
                        } else if (cleanedContent.isEmpty()) {
                            zhText = "【" + cleanedTitle + "】";
                        } else {
                            boolean contentContainsTitle = cleanedContent.contains(cleanedTitle);
                            boolean contentStartsWithTitle = cleanedContent.startsWith(cleanedTitle) || cleanedContent.startsWith("【" + cleanedTitle + "】");
                            if (contentContainsTitle || contentStartsWithTitle) {
                                zhText = cleanedContent;
                            } else {
                                zhText = "【" + cleanedTitle + "】" + cleanedContent;
                            }
                        }

                        zhText = normalizeLeadingBracketPrefix(zhText);

                        Map<String, String> contentMap = new HashMap<>();
                        contentMap.put("zh", zhText);
                        contentMap.put("en", "[Waiting for translation] " + zhText);

                        freshNews.add(NewsItem.builder()
                                .id(id)
                                .time(ctime * 1000) // 转换为毫秒
                                .content(contentMap)
                                .source("Cailian")
                                .build());
                    }

                    updateCacheAndPush(freshNews);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Cailian News: {}", e.getMessage());
        }
    }

    private String normalizeLeadingBracketPrefix(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return trimmed;

        Pattern multiPrefixPattern = Pattern.compile("^(?:\\s*【[^】]+】\\s*){2,}");
        Matcher multiPrefixMatcher = multiPrefixPattern.matcher(trimmed);
        if (!multiPrefixMatcher.find()) return trimmed;

        String allPrefixes = multiPrefixMatcher.group();
        Matcher firstPrefixMatcher = Pattern.compile("【[^】]+】").matcher(allPrefixes);
        if (!firstPrefixMatcher.find()) return trimmed;

        String firstPrefix = firstPrefixMatcher.group();
        String rest = trimmed.substring(allPrefixes.length()).stripLeading();
        return firstPrefix + rest;
    }

    private void updateCacheAndPush(List<NewsItem> freshNews) {
        if (freshNews.isEmpty()) return;

        boolean hasNew = false;
        synchronized (cachedCailianNews) {
            if (cachedCailianNews.isEmpty() || !freshNews.get(0).getId().equals(cachedCailianNews.get(0).getId())) {
                hasNew = true;
            }
            cachedCailianNews.clear();
            cachedCailianNews.addAll(freshNews);
        }

        if (hasNew) {
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "cailian");
            pushData.put("data", freshNews.get(0));
            newsWebSocketHandler.broadcastNews(pushData);
            log.info("CAILIAN PUSH: New telegraph detected and broadcasted.");
        }
    }

    public List<NewsItem> getCailianNews() {
        return new ArrayList<>(cachedCailianNews);
    }
}
