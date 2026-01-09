package com.project.onlineanket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.project.onlineanket.service.GeminiService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    // API: Yapay Zekadan veri getiren kısım (Burası veri taşır)
    @GetMapping("/api/gemini/oner")
    public ResponseEntity<Map<String, String>> oneriGetir(@RequestParam String konu) {
        String cevap = geminiService.anketSorusuOner(konu);
        
        Map<String, String> response = new HashMap<>();
        response.put("oneri", cevap);
        
        return ResponseEntity.ok(response);
    }
    
    // ❌ SİLDİK: Sayfa açma kodunu buradan çıkardık, PageController'a koyduk.
}