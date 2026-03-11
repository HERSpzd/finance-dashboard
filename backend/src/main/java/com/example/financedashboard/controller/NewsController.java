package com.example.financedashboard.controller;

import com.example.financedashboard.model.GlobalNewsItem;
import com.example.financedashboard.model.NewsItem;
import com.example.financedashboard.service.AITrendsService;
import com.example.financedashboard.service.CailianNewsService;
import com.example.financedashboard.service.GlobalAffairsService;
import com.example.financedashboard.service.NewsSyncService;
import com.example.financedashboard.service.TechTrendsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许 Vue 前端跨域访问
public class NewsController {

    private final NewsSyncService newsSyncService;
    private final CailianNewsService cailianNewsService;
    private final GlobalAffairsService globalAffairsService;
    private final AITrendsService aiTrendsService;
    private final TechTrendsService techTrendsService;

    @GetMapping("/wallstreet")
    public List<NewsItem> getWallStreetNews() {
        return newsSyncService.getWallStreetNews();
    }

    @GetMapping("/yahoo")
    public List<NewsItem> getYahooNews() {
        return newsSyncService.getYahooNews();
    }

    @GetMapping("/cailian")
    public List<NewsItem> getCailianNews() {
        return cailianNewsService.getCailianNews();
    }

    @GetMapping("/global")
    public List<GlobalNewsItem> getGlobalNews() {
        return globalAffairsService.getGlobalNews();
    }

    @GetMapping("/ai")
    public List<GlobalNewsItem> getAINews() {
        return aiTrendsService.getAINews();
    }

    @GetMapping("/tech")
    public List<GlobalNewsItem> getTechNews() {
        return techTrendsService.getTechNews();
    }
}
