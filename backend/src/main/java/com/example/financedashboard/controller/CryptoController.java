package com.example.financedashboard.controller;

import com.example.financedashboard.model.CryptoItem;
import com.example.financedashboard.service.CryptoDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CryptoController {

    private final CryptoDataService cryptoDataService;

    @GetMapping("/realtime")
    public List<CryptoItem> getRealtimeCryptos() {
        return cryptoDataService.getRealtimeCryptos();
    }
}
