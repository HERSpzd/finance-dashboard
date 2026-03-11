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
public class GlobalNewsItem {
    private String id;
    private String time; // HH:mm 格式
    private Map<String, String> content;
    private String type; // GLOBAL
}
