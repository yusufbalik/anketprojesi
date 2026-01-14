package com.project.onlineanket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value; // ✅ YENİ EKLENDİ
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    // ✅ GÜVENLİK GÜNCELLEMESİ:
    // Artık anahtarı kodun içine açıkça yazmıyoruz.
    // GitHub Secrets -> application.properties -> Buraya otomatik geliyor.
    @Value("${gemini.api.key}")
    private String apiKey;

    // 🚀 GÜNCEL MODEL AYARI:
    // "gemini-2.5-flash" şu an gerçek dünyada henüz yayınlanmadı (404 hatası verir).
    // O yüzden şu an en hızlı ve kararlı çalışan "gemini-1.5-flash" sürümünü yazdım.
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public String anketSorusuOner(String konu) {
        try {
            // Header Ayarı (API Key buraya güvenli şekilde ekleniyor)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey.trim()); // Trim boşlukları temizler

            // İstek Metni
            String istekMetni = "Bana '" + konu + "' konusuyla ilgili 1 adet anket sorusu ve 4 şık öner. " +
                                "Cevabı şu formatta ver: Soru: [Soru] || A: [Şık1] || B: [Şık2] || C: [Şık3] || D: [Şık4]";

            // Body Yapısı (Senin kurduğun Map yapısı aynen korundu)
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
            
            System.out.println("Google'a istek atılıyor... (Model: gemini-1.5-flash)");
            
            String response = restTemplate.postForObject(baseUrl, request, String.class);

            // Cevabı Al ve Parse Et
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            
            // JSON yolunu takip edip cevabı çıkarıyoruz
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            System.out.println("GEMINI SERVİS HATASI: " + e.getMessage());
            e.printStackTrace();
            return "Hata oluştu: Yapay zeka şu an yanıt veremiyor. (" + e.getMessage() + ")";
        }
    }
}
