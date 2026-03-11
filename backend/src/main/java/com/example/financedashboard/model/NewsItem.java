package com.example.financedashboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsItem {
    private String id;
    private long time; // 相对时间或时间戳
    private Map<String, String> content; // {zh: '...', en: '...'}
    private String source;
}
