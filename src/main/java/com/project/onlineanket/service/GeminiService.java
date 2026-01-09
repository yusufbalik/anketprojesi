package com.project.onlineanket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    // ✅ YENİ ANAHTARIN (Bu doğru, dokunma):
    private String apiKey = "AIzaSyCeAaYO-OnJUZ-j2Y5pnkYagZnXRxA8O5A"; 

    // 🚀 DÜZELTME BURADA:
    // 2026 yılındayız, '1.5-flash' artık yok.
    // Senin için en güncel ve çalışan kararlı sürümü yazdım: 'gemini-2.5-flash'
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public String anketSorusuOner(String konu) {
        try {
            // Header Ayarı
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey.trim());

            // İstek Metni
            String istekMetni = "Bana '" + konu + "' konusuyla ilgili 1 adet anket sorusu ve 4 şık öner. " +
                                "Cevabı şu formatta ver: Soru: [Soru] || A: [Şık1] || B: [Şık2] || C: [Şık3] || D: [Şık4]";

            Map<String, Object> body = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", istekMetni)
                    ))
                )
            );

            // Gönder
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            
            System.out.println("Google'a istek atılıyor... (Model: gemini-2.5-flash)");
            
            String response = restTemplate.postForObject(baseUrl, request, String.class);

            // Cevabı Al
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            System.out.println("HATA: " + e.getMessage());
            e.printStackTrace();
            return "Hata oluştu: " + e.getMessage();
        }
    }
}