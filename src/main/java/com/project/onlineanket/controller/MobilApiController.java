package com.project.onlineanket.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalDate; 
import java.time.LocalDateTime; // ✅ EKLENDİ
import java.time.LocalTime; // ✅ EKLENDİ
import java.time.format.DateTimeFormatter; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iyzipay.model.CheckoutFormInitialize;
import com.project.onlineanket.entity.Anket;
import com.project.onlineanket.entity.Bildirim;
import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.entity.Secenek;
import com.project.onlineanket.entity.Yorum;
import com.project.onlineanket.repository.AnketRepository;
import com.project.onlineanket.repository.KullaniciRepository;
import com.project.onlineanket.service.AnketService;
import com.project.onlineanket.service.BildirimService;
import com.project.onlineanket.service.GeminiService;
import com.project.onlineanket.service.IyzipayService;
import com.project.onlineanket.service.KullaniciService;

@RestController
@RequestMapping("/api/mobil")
public class MobilApiController {

    @Autowired private AnketRepository anketRepository;
    @Autowired private KullaniciRepository kullaniciRepository;
    @Autowired private AnketService anketService;
    @Autowired private KullaniciService kullaniciService;
    @Autowired private IyzipayService iyzipayService;
    @Autowired private GeminiService geminiService;
    @Autowired private BildirimService bildirimService;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    // ⚠️ DİKKAT: Ngrok her açılıp kapandığında burayı güncellemen ŞART!
    private final String NGROK_URL = "https://conductorial-joyce-heliometric.ngrok-free.dev"; 

    // --- 1. LİSTELEME ---
    @GetMapping("/anketler")
    public ResponseEntity<List<MobilAnketDTO>> tumAnketleriGetir() {
        List<Anket> anketler = anketService.tumAnketleriGetir();
        List<MobilAnketDTO> mobilListe = anketler.stream()
            .map(a -> {
                int toplam = 0;
                if (a.getSecenekler() != null) {
                    toplam = a.getSecenekler().stream().mapToInt(Secenek::getOySayisi).sum();
                }
                
                String bitisTarihi = (a.getBitisTarihi() != null) ? a.getBitisTarihi().toString() : null;

                return new MobilAnketDTO(a.getId(), a.getBaslik(), a.getKategori(), toplam, bitisTarihi);
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(mobilListe);
    }

    // --- 2. AI ÖNERİ ---
    @GetMapping("/ai-oner")
    public ResponseEntity<?> yapayZekaOner(@RequestParam String konu) {
        try {
            String aiCevap = geminiService.anketSorusuOner(konu);
            return ResponseEntity.ok(Map.of("success", true, "oneri", aiCevap));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --- 3. DETAY ---
    @GetMapping("/anket/{id}")
    public ResponseEntity<?> anketDetayGetir(@PathVariable Long id) {
        try {
            Anket a = anketService.anketBul(id);
            if (a == null) return ResponseEntity.notFound().build();

            int toplam = 0;
            if (a.getSecenekler() != null) {
                toplam = a.getSecenekler().stream().mapToInt(Secenek::getOySayisi).sum();
            }

            List<MobilSecenekDTO> secenekler = a.getSecenekler().stream()
                .map(s -> new MobilSecenekDTO(s.getId(), s.getMetin(), s.getOySayisi(), s.getResimUrl()))
                .collect(Collectors.toList());

            List<MobilYorumDTO> yorumlar = new ArrayList<>();
            if (a.getYorumlar() != null) {
                yorumlar = a.getYorumlar().stream()
                    .map(y -> new MobilYorumDTO(
                        y.getId(), 
                        y.getKullanici().getKullaniciAdi(), 
                        y.getIcerik(), 
                        y.getTarih().toString()
                    ))
                    .collect(Collectors.toList());
            }

            Long olusturanId = (a.getOlusturan() != null) ? a.getOlusturan().getId() : 0L;
            String bitisTarihi = (a.getBitisTarihi() != null) ? a.getBitisTarihi().toString() : null;

            MobilAnketDetayDTO detay = new MobilAnketDetayDTO(
                a.getId(), a.getBaslik(), a.getAciklama(), toplam, secenekler, yorumlar, olusturanId, bitisTarihi
            );
            return ResponseEntity.ok(detay);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Hata: " + e.getMessage());
        }
    }

    // --- 4. OY VERME ---
    @PostMapping("/oy-ver/{secenekId}")
    public ResponseEntity<?> oyVer(@PathVariable Long secenekId, @RequestParam(required = false) Long kullaniciId) {
        Kullanici k = null;
        if (kullaniciId != null) {
            k = kullaniciService.kullaniciIdIleBul(kullaniciId);
            if (k == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Kullanıcı bulunamadı."));
            }
        }
        boolean sonuc = anketService.oyVer(secenekId, k);
        if (sonuc) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Oyunuz kaydedildi!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Hata oluştu veya zaten oy kullandınız."));
        }
    }

    // --- 5. GİRİŞ YAPMA ---
    @PostMapping("/login")
    public ResponseEntity<?> mobilGirisYap(@RequestBody LoginIstegi istek) {
        Kullanici k = kullaniciService.kullaniciBul(istek.getUsername());
        boolean sifreDogru = false;
        
        if (k != null) {
            if (passwordEncoder != null) {
                sifreDogru = passwordEncoder.matches(istek.getPassword(), k.getSifre());
            } else {
                sifreDogru = k.getSifre().equals(istek.getPassword());
            }
        }

        if (sifreDogru) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", k.getId());
            response.put("username", k.getKullaniciAdi()); 
            response.put("rol", k.getRol());
            response.put("premium", k.isPremiumMu());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Hatalı giriş!"));
        }
    }

    // --- 6. REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody KayitIstegi istek) {
        try {
            if (kullaniciRepository.findByKullaniciAdi(istek.getKullaniciAdi()).isPresent()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Bu kullanıcı adı zaten alınmış!"));
            }
            Kullanici yeniKullanici = new Kullanici();
            yeniKullanici.setKullaniciAdi(istek.getKullaniciAdi()); 
            yeniKullanici.setEmail(istek.getEmail());
            
            if (passwordEncoder != null) {
                yeniKullanici.setSifre(passwordEncoder.encode(istek.getSifre()));
            } else {
                yeniKullanici.setSifre(istek.getSifre());
            }
            yeniKullanici.setRol("ROLE_USER");
            yeniKullanici.setPuan(0);
            
            kullaniciRepository.save(yeniKullanici);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kayıt başarılı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Sunucu hatası: " + e.getMessage()));
        }
    }

    // --- 7. PROFİL BİLGİSİ ---
    @GetMapping("/profil-bilgisi/{id}")
    public ResponseEntity<?> profilBilgisiGetir(@PathVariable Long id) {
        Kullanici k = kullaniciService.kullaniciIdIleBul(id);
        if (k == null) return ResponseEntity.notFound().build();

        Map<String, Object> profil = new HashMap<>();
        profil.put("username", k.getKullaniciAdi()); 
        profil.put("email", k.getEmail());
        profil.put("puan", k.getPuan());
        profil.put("rutbe", k.getRutbe());
        profil.put("rutbeRenk", k.getRutbeRenk());
        profil.put("rol", k.getRol());
        profil.put("premium", k.isPremiumMu());
        
        List<Anket> kullaniciAnketleri = anketRepository.findByOlusturan(k);
        
        List<MobilAnketDTO> anketListesi = kullaniciAnketleri.stream()
            .filter(a -> a.isAktif()) 
            .map(a -> new MobilAnketDTO(
                a.getId(), 
                a.getBaslik(), 
                a.getKategori(), 
                a.getToplamOy(),
                a.getBitisTarihi() != null ? a.getBitisTarihi().toString() : null
            ))
            .collect(Collectors.toList());

        profil.put("anketler", anketListesi);
        return ResponseEntity.ok(profil);
    }

    // --- 8. ADMIN İSTATİSTİK ---
    @GetMapping("/admin/stats")
    public ResponseEntity<?> adminStats() {
        long toplamUye = kullaniciRepository.count();
        long toplamAnket = anketRepository.count();
        long toplamOy = 0;
        List<Anket> tumAnketler = anketRepository.findAll();
        for (Anket a : tumAnketler) {
            if (a.getSecenekler() != null) {
                toplamOy += a.getSecenekler().stream().mapToInt(Secenek::getOySayisi).sum();
            }
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("toplamUye", toplamUye);
        stats.put("toplamAnket", toplamAnket);
        stats.put("toplamOy", toplamOy);
        return ResponseEntity.ok(stats);
    }

    // --- 9. ADMIN ÜYELER ---
    @GetMapping("/admin/uyeler")
    public ResponseEntity<?> adminUyeler() {
        List<Kullanici> tumUyeler = kullaniciRepository.findAll();
        List<Map<String, Object>> uyeListesi = tumUyeler.stream()
            .map(u -> {
                Map<String, Object> uyeMap = new HashMap<>();
                uyeMap.put("id", u.getId());
                uyeMap.put("kullaniciAdi", u.getKullaniciAdi());
                uyeMap.put("email", u.getEmail());
                uyeMap.put("kayitTarihi", u.getKayitTarihi() != null ? u.getKayitTarihi().toString() : "");
                uyeMap.put("rol", u.getRol());
                uyeMap.put("premiumMu", u.isPremiumMu());
                return uyeMap;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(uyeListesi);
    }

    // --- 10. ADMIN KATEGORİLER ---
    @GetMapping("/admin/kategoriler")
    public ResponseEntity<?> adminKategoriler() {
        List<Anket> tumAnketler = anketRepository.findAllByAktifTrue();
        Map<String, Long> kategoriMap = tumAnketler.stream()
            .collect(Collectors.groupingBy(
                a -> a.getKategori() != null ? a.getKategori() : "Genel",
                Collectors.counting()
            ));
        
        List<Map<String, Object>> kategoriListesi = kategoriMap.entrySet().stream()
            .map(e -> {
                Map<String, Object> kategoriItem = new HashMap<>();
                kategoriItem.put("kategori", e.getKey());
                kategoriItem.put("sayi", e.getValue().intValue());
                return kategoriItem;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(kategoriListesi);
    }

    // --- 11. ANKET OLUŞTURMA (HATA DÜZELTİLDİ) ---
    @PostMapping("/anket-olustur")
    public ResponseEntity<?> mobilAnketOlustur(@RequestBody YeniAnketIstegi istek) {
        try {
            Kullanici k = kullaniciService.kullaniciIdIleBul(istek.getOlusturanId());
            if (k == null) return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı yok"));

            boolean adminMi = "ROLE_ADMIN".equals(k.getRol());
            if (!k.isPremiumMu() && !adminMi) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Premium gerekli"));
            }

            if (istek.getSecenekler() == null || istek.getSecenekler().size() < 2) {
                return ResponseEntity.badRequest().body(Map.of("message", "En az 2 seçenek girin"));
            }

            Anket yeniAnket = new Anket();
            yeniAnket.setBaslik(istek.getBaslik());
            yeniAnket.setAciklama(istek.getAciklama());
            yeniAnket.setKategori(istek.getKategori());
            yeniAnket.setAktif(true);
            yeniAnket.setOlusturan(k);

            // ✅ TARİH DÖNÜŞÜMÜ DÜZELTİLDİ (LocalDate -> LocalDateTime)
            if (istek.getBitisTarihiStr() != null && !istek.getBitisTarihiStr().isEmpty()) {
                try {
                    // String'i LocalDate'e çevir (2025-05-20)
                    LocalDate tarih = LocalDate.parse(istek.getBitisTarihiStr(), DateTimeFormatter.ISO_LOCAL_DATE);
                    
                    // LocalDate'i günün son saatine (23:59:59) ayarlayarak LocalDateTime yap
                    LocalDateTime bitisZamani = tarih.atTime(LocalTime.MAX);
                    
                    yeniAnket.setBitisTarihi(bitisZamani);
                } catch (Exception e) {
                    System.out.println("Tarih formatı hatası: " + e.getMessage());
                }
            }

            java.util.ArrayList<Secenek> secenekListesi = new java.util.ArrayList<>();
            int sira = 1;
            List<String> resimler = istek.getSecenekResimleri();
            if (resimler == null) resimler = new java.util.ArrayList<>();

            for (int i = 0; i < istek.getSecenekler().size(); i++) {
                String metin = istek.getSecenekler().get(i);
                if (metin != null && !metin.trim().isEmpty()) {
                    Secenek s = new Secenek();
                    s.setMetin(metin.trim());
                    s.setSira(sira++);
                    s.setAnket(yeniAnket);
                    if (i < resimler.size()) {
                        String resimUrl = resimler.get(i);
                        if (resimUrl != null && !resimUrl.trim().isEmpty()) {
                            s.setResimUrl(resimUrl.trim());
                        }
                    }
                    secenekListesi.add(s);
                }
            }
            
            yeniAnket.setSecenekler(secenekListesi);
            anketService.anketKaydet(yeniAnket);

            return ResponseEntity.ok(Map.of("success", true, "message", "Anket oluşturuldu", "anketId", yeniAnket.getId()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --- 12. ÖDEME BAŞLATMA ---
    @PostMapping("/odeme-baslat")
    public ResponseEntity<?> odemeBaslat(@RequestBody Map<String, Object> istek) {
        try {
            Long kullaniciId = Long.valueOf(istek.get("kullaniciId").toString());
            String paketTipi = (String) istek.get("paketTipi");
            String fiyat = (String) istek.get("fiyat");

            Kullanici k = kullaniciService.kullaniciIdIleBul(kullaniciId);
            if (k == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı bulunamadı"));
            }

            String callbackUrl = NGROK_URL + "/api/mobil/odeme-sonuc?uid=" + k.getId();
            CheckoutFormInitialize form = iyzipayService.odemeFormuOlustur(k, paketTipi, fiyat, callbackUrl);

            if ("success".equals(form.getStatus())) {
                return ResponseEntity.ok(Map.of("success", true, "htmlContent", form.getCheckoutFormContent()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", form.getErrorMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Sunucu hatası: " + e.getMessage()));
        }
    }

    // --- 13. ÖDEME SONUCU ---
    @RequestMapping(value = "/odeme-sonuc", method = {RequestMethod.GET, RequestMethod.POST})
    public String odemeSonucMobil(@RequestParam(name = "uid", required = false) Long uid) {
        if (uid != null) {
            kullaniciService.premiumYap(uid);
        }
        return "<html><head><title>Odeme Basarili</title></head>" +
               "<body style='text-align:center; padding-top:50px; font-family:sans-serif; background-color:#f0fdf4;'>" +
               "<h1 style='color:#16a34a;'>Ödeme Başarılı! 🎉</h1>" +
               "<p style='color:#374151;'>Premium üyeliğiniz aktifleşti.</p>" +
               "<p style='color:#9ca3af; font-size:12px;'>Lütfen bekleyin...</p>" +
               "</body></html>";
    }

    // --- 14. ADMIN: PREMIUM DEĞİŞTİR ---
    @PostMapping("/admin/premium-degistir")
    public ResponseEntity<?> premiumDegistirMobil(@RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("id")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı ID eksik"));
            }

            Long id = Long.valueOf(payload.get("id").toString());
            String sebep = (String) payload.get("sebep");

            Kullanici uye = kullaniciService.kullaniciIdIleBul(id);
            if (uye != null) {
                boolean eskiDurum = uye.isPremiumMu();
                boolean yeniDurum = !eskiDurum;
                
                uye.setPremiumMu(yeniDurum);
                kullaniciRepository.save(uye);

                String mesaj;
                if (yeniDurum) {
                    mesaj = "🎉 Tebrikler! Premium üyeliğiniz Yönetici tarafından aktif edildi. Keyfini çıkarın!";
                } else {
                    mesaj = "⚠️ Premium üyeliğiniz iptal edilmiştir.";
                    if (sebep != null && !sebep.trim().isEmpty()) {
                        mesaj += " Sebep: " + sebep;
                    }
                }
                
                bildirimService.bildirimGonder(uye, mesaj);

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "İşlem başarılı. Kullanıcıya bildirim gönderildi.",
                    "yeniDurum", yeniDurum
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı bulunamadı!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Hata: " + e.getMessage()));
        }
    }

    // --- 15. ADMIN: DUYURU GÖNDER ---
    @PostMapping("/admin/duyuru-gonder")
    public ResponseEntity<?> duyuruGonderMobil(@RequestBody Map<String, String> payload) {
        try {
            String mesaj = payload.get("mesaj");
            if (mesaj == null || mesaj.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mesaj boş olamaz"));
            }

            List<Kullanici> tumKullanicilar = kullaniciRepository.findAll();
            int gonderilenSayisi = 0;
            for (Kullanici k : tumKullanicilar) {
                bildirimService.bildirimGonder(k, "📢 DUYURU: " + mesaj);
                gonderilenSayisi++;
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", gonderilenSayisi + " kişiye duyuru bildirimi gönderildi."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Hata: " + e.getMessage()));
        }
    }

    // --- 16. BİLDİRİMLER ---
    @GetMapping("/bildirimler/{kullaniciId}")
    public ResponseEntity<?> bildirimleriGetir(@PathVariable Long kullaniciId) {
        Kullanici k = kullaniciService.kullaniciIdIleBul(kullaniciId);
        if (k == null) return ResponseEntity.notFound().build();

        List<Bildirim> bildirimler = bildirimService.kullaniciBildirimleriniGetir(k);
        
        List<Map<String, Object>> bildirimDTOs = bildirimler.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("icerik", b.getIcerik());
            map.put("tarih", b.getTarih().toString());
            map.put("okunduMu", b.isOkunduMu());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(bildirimDTOs);
    }

    // --- 17. BİLDİRİM SİL ---
    @PostMapping("/bildirim-sil/{id}")
    public ResponseEntity<?> bildirimSilMobil(@PathVariable Long id, @RequestParam Long kullaniciId) {
        try {
            Kullanici k = kullaniciService.kullaniciIdIleBul(kullaniciId);
            if (k != null) {
                bildirimService.bildirimSil(id, k);
                return ResponseEntity.ok(Map.of("success", true, "message", "Bildirim silindi."));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı bulunamadı."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Hata: " + e.getMessage()));
        }
    }
    
    // --- 18. YORUM YAP ---
    @PostMapping("/yorum-yap")
    public ResponseEntity<?> yorumYapMobil(@RequestBody YorumIstegi istek) {
        try {
            Kullanici k = kullaniciService.kullaniciIdIleBul(istek.getKullaniciId());
            if (k == null) return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı bulunamadı"));

            anketService.yorumYap(istek.getAnketId(), k, istek.getIcerik());

            return ResponseEntity.ok(Map.of("success", true, "message", "Yorum gönderildi"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Hata: " + e.getMessage()));
        }
    }

    // --- 19. ANKET SİLME ---
    @PostMapping("/anket-sil/{id}")
    public ResponseEntity<?> anketSilMobil(@PathVariable Long id, @RequestParam Long kullaniciId) {
        try {
            Kullanici k = kullaniciService.kullaniciIdIleBul(kullaniciId);
            Anket a = anketService.anketBul(id);

            if (k == null || a == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı veya anket bulunamadı."));
            }

            boolean isAdmin = "ROLE_ADMIN".equals(k.getRol());
            boolean isSahibi = a.getOlusturan() != null && a.getOlusturan().getId().equals(k.getId());

            if (isAdmin || isSahibi) {
                anketService.anketSil(id); 
                return ResponseEntity.ok(Map.of("success", true, "message", "Anket başarıyla silindi."));
            } else {
                return ResponseEntity.status(403).body(Map.of("message", "Bu anketi silme yetkiniz yok!"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Hata: " + e.getMessage()));
        }
    }

    // --- DTO'LAR ---
    public static class YeniAnketIstegi {
        private String baslik; private String aciklama; private String kategori;
        private List<String> secenekler; private List<String> secenekResimleri; 
        private Long olusturanId;
        private String bitisTarihiStr;

        public String getBaslik() { return baslik; } public void setBaslik(String baslik) { this.baslik = baslik; }
        public String getAciklama() { return aciklama; } public void setAciklama(String aciklama) { this.aciklama = aciklama; }
        public String getKategori() { return kategori; } public void setKategori(String kategori) { this.kategori = kategori; }
        public List<String> getSecenekler() { return secenekler; } public void setSecenekler(List<String> secenekler) { this.secenekler = secenekler; }
        public List<String> getSecenekResimleri() { return secenekResimleri; } public void setSecenekResimleri(List<String> secenekResimleri) { this.secenekResimleri = secenekResimleri; }
        public Long getOlusturanId() { return olusturanId; } public void setOlusturanId(Long olusturanId) { this.olusturanId = olusturanId; }
        public String getBitisTarihiStr() { return bitisTarihiStr; } public void setBitisTarihiStr(String bitisTarihiStr) { this.bitisTarihiStr = bitisTarihiStr; }
    }

    public static class MobilAnketDTO {
        private Long id; private String baslik; private String kategori; private int toplamOy;
        private String bitisTarihi; 

        public MobilAnketDTO(Long id, String baslik, String kategori, int toplamOy, String bitisTarihi) {
            this.id = id; this.baslik = baslik; this.kategori = kategori; this.toplamOy = toplamOy;
            this.bitisTarihi = bitisTarihi;
        }
        public Long getId() { return id; } public String getBaslik() { return baslik; }
        public String getKategori() { return kategori; } public int getToplamOy() { return toplamOy; }
        public String getBitisTarihi() { return bitisTarihi; }
    }

    public static class MobilAnketDetayDTO {
        private Long id; private String baslik; private String aciklama; private int toplamOy;
        private List<MobilSecenekDTO> secenekler;
        private List<MobilYorumDTO> yorumlar;
        private Long olusturanId; 
        private String bitisTarihi;

        public MobilAnketDetayDTO(Long id, String baslik, String aciklama, int toplamOy, List<MobilSecenekDTO> secenekler, List<MobilYorumDTO> yorumlar, Long olusturanId, String bitisTarihi) {
            this.id = id; this.baslik = baslik; this.aciklama = aciklama; 
            this.toplamOy = toplamOy; this.secenekler = secenekler; 
            this.yorumlar = yorumlar; this.olusturanId = olusturanId;
            this.bitisTarihi = bitisTarihi;
        }
        public Long getId() { return id; } public String getBaslik() { return baslik; }
        public String getAciklama() { return aciklama; } public int getToplamOy() { return toplamOy; }
        public List<MobilSecenekDTO> getSecenekler() { return secenekler; }
        public List<MobilYorumDTO> getYorumlar() { return yorumlar; }
        public Long getOlusturanId() { return olusturanId; } 
        public String getBitisTarihi() { return bitisTarihi; }
    }
    
    // ... Diğer DTO'lar ...
    public static class MobilYorumDTO { private Long id; private String kullaniciAdi; private String icerik; private String tarih; public MobilYorumDTO(Long id, String k, String i, String t) { this.id=id; this.kullaniciAdi=k; this.icerik=i; this.tarih=t; } public Long getId() { return id; } public String getKullaniciAdi() { return kullaniciAdi; } public String getIcerik() { return icerik; } public String getTarih() { return tarih; } }
    public static class YorumIstegi { private Long anketId; private Long kullaniciId; private String icerik; public Long getAnketId() { return anketId; } public void setAnketId(Long a) { this.anketId = a; } public Long getKullaniciId() { return kullaniciId; } public void setKullaniciId(Long k) { this.kullaniciId = k; } public String getIcerik() { return icerik; } public void setIcerik(String i) { this.icerik = i; } }
    public static class MobilSecenekDTO { private Long id; private String metin; private int oySayisi; private String resimUrl; public MobilSecenekDTO(Long id, String metin, int oySayisi, String resimUrl) { this.id = id; this.metin = metin; this.oySayisi = oySayisi; this.resimUrl = resimUrl; } public Long getId() { return id; } public String getMetin() { return metin; } public int getOySayisi() { return oySayisi; } public String getResimUrl() { return resimUrl; } }
    public static class LoginIstegi { private String username; private String password; public String getUsername() { return username; } public void setUsername(String username) { this.username = username; } public String getPassword() { return password; } public void setPassword(String password) { this.password = password; } }
    public static class KayitIstegi { private String kullaniciAdi; private String email; private String sifre; public String getKullaniciAdi() { return kullaniciAdi; } public void setKullaniciAdi(String k) { this.kullaniciAdi = k; } public String getEmail() { return email; } public void setEmail(String e) { this.email = e; } public String getSifre() { return sifre; } public void setSifre(String s) { this.sifre = s; } }
}