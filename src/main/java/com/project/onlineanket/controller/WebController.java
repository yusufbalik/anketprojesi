package com.project.onlineanket.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.iyzipay.model.CheckoutFormInitialize;
import com.project.onlineanket.entity.Anket;
import com.project.onlineanket.entity.Bildirim; // EKLENDİ
import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.entity.Secenek;
import com.project.onlineanket.entity.Yorum;
import com.project.onlineanket.repository.AnketRepository;
import com.project.onlineanket.repository.KullaniciRepository;
import com.project.onlineanket.repository.OyRepository;
import com.project.onlineanket.repository.YorumRepository;
import com.project.onlineanket.service.AnketService;
import com.project.onlineanket.service.BildirimService; // EKLENDİ
import com.project.onlineanket.service.GeminiService;
import com.project.onlineanket.service.IyzipayService;
import com.project.onlineanket.service.KullaniciService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class WebController {

    @Autowired private AnketService anketService;
    @Autowired private KullaniciService kullaniciService;
    @Autowired private AnketRepository anketRepository;
    @Autowired private KullaniciRepository kullaniciRepository;
    @Autowired private IyzipayService iyzipayService;
    @Autowired private YorumRepository yorumRepository;
    @Autowired private OyRepository oyRepository;
    @Autowired private GeminiService geminiService;
    @Autowired private BildirimService bildirimService; // EKLENDİ

    // --- ANASAYFA ---
    @GetMapping("/")
    public String anaSayfa(Model model, Principal principal) {
        model.addAttribute("anketListesi", anketService.tumAnketleriGetir());
        model.addAttribute("aktifSayfa", "anasayfa");
        kullaniciBilgisiYukle(model, principal);
        return "index";
    }

    // --- POPÜLER ---
    @GetMapping("/populer")
    public String populerAnketler(Model model, Principal principal) {
        model.addAttribute("anketListesi", anketService.populerAnketleriGetir());
        model.addAttribute("aktifSayfa", "populer");
        kullaniciBilgisiYukle(model, principal);
        return "index";
    }

    // --- GEÇMİŞ ---
    @GetMapping("/gecmis")
    public String gecmisAnketler(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        String kAdi = principal.getName();
        Kullanici k = kullaniciService.kullaniciBul(kAdi);
        model.addAttribute("anketListesi", anketService.kullanicininGecmisi(k.getId()));
        model.addAttribute("aktifSayfa", "gecmis");
        kullaniciBilgisiYukle(model, principal);
        return "index";
    }

    // --- KATEGORİ ---
    @GetMapping("/kategori/{kategoriAdi}")
    public String kategoriGetir(@PathVariable String kategoriAdi, Model model, Principal principal) {
        model.addAttribute("anketListesi", anketService.kategoriyeGoreGetir(kategoriAdi));
        model.addAttribute("aktifSayfa", kategoriAdi);
        kullaniciBilgisiYukle(model, principal);
        return "index";
    }

    // --- ARAMA ---
    @GetMapping("/ara")
    public String anketAra(@RequestParam String keyword, Model model, Principal principal) {
        model.addAttribute("anketListesi", anketService.aramayaGoreGetir(keyword));
        model.addAttribute("aramaKelimesi", keyword);
        model.addAttribute("aktifSayfa", "arama");
        kullaniciBilgisiYukle(model, principal);
        return "index";
    }

    // --- PROFIL ---
    @GetMapping("/profil")
    public String profilSayfasi(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        String kAdi = principal.getName();
        Kullanici k = kullaniciService.kullaniciBul(kAdi);
        
        List<Anket> tumAnketlerim = anketRepository.findByOlusturan(k);
        List<Anket> aktifAnketlerim = new ArrayList<>();
        for (Anket a : tumAnketlerim) {
            if (a.isAktif()) {
                aktifAnketlerim.add(a);
            }
        }
        
        model.addAttribute("benimAnketlerim", aktifAnketlerim);
        kullaniciBilgisiYukle(model, principal);
        return "profil"; 
    }

    // --- ADMIN PANELİ ---
    @GetMapping("/admin")
    public String adminPaneli(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        String kAdi = principal.getName();
        Kullanici k = kullaniciService.kullaniciBul(kAdi);
        
        if ("ROLE_ADMIN".equals(k.getRol())) {
            // 1. Temel Sayaçlar
            model.addAttribute("toplamUye", kullaniciRepository.count());
            model.addAttribute("toplamAnket", anketRepository.count());
            model.addAttribute("toplamOy", oyRepository.count());
            
            // Tüm üyeler tablosu için
            List<Kullanici> tumUyeler = kullaniciRepository.findAll();
            model.addAttribute("tumUyeler", tumUyeler);

            // 2. GRAFİK VERİSİ: Üye Artış Trendi (Son 7 Gün)
            List<String> trendGunler = new ArrayList<>();
            List<Integer> trendSayilar = new ArrayList<>();
            LocalDate bugun = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

            for (int i = 6; i >= 0; i--) {
                LocalDate tarih = bugun.minusDays(i);
                trendGunler.add(tarih.format(formatter));

                int gunlukSayi = 0;
                for (Kullanici u : tumUyeler) {
                    if (u.getKayitTarihi() != null && u.getKayitTarihi().toLocalDate().equals(tarih)) {
                        gunlukSayi++;
                    }
                }
                trendSayilar.add(gunlukSayi);
            }
            model.addAttribute("trendGunler", trendGunler);
            model.addAttribute("trendSayilar", trendSayilar);

            // 3. GRAFİK VERİSİ: Kategoriler
            List<Anket> tumAnketler = anketRepository.findAll();
            Map<String, Integer> kategoriMap = new HashMap<>();
            
            for (Anket a : tumAnketler) {
                String kat = (a.getKategori() != null && !a.getKategori().isEmpty()) ? a.getKategori() : "Genel";
                kategoriMap.put(kat, kategoriMap.getOrDefault(kat, 0) + 1);
            }
            
            model.addAttribute("kategoriIsimleri", kategoriMap.keySet());
            model.addAttribute("kategoriSayilari", kategoriMap.values());

            kullaniciBilgisiYukle(model, principal);
            return "admin-panel"; 
        } 
        return "error/403"; 
    }

    // --- ADMIN İŞLEMLERİ (GÜNCELLENDİ: ARTIK POST VE SEBEPLİ) ---
    @PostMapping("/admin/premium-degistir")
    public String premiumDegistir(@RequestParam Long id, @RequestParam(required = false) String sebep, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        
        Kullanici admin = kullaniciService.kullaniciBul(principal.getName());
        if (!"ROLE_ADMIN".equals(admin.getRol())) return "error/403";
        
        Kullanici uye = kullaniciRepository.findById(id).orElse(null);
        if (uye != null) {
            boolean yeniDurum = !uye.isPremiumMu(); // Tersi yap
            uye.setPremiumMu(yeniDurum);
            kullaniciRepository.save(uye);
            
            // BİLDİRİM GÖNDERME MANTIĞI
            String mesaj;
            if (yeniDurum) {
                mesaj = "Tebrikler! Premium üyeliğiniz Yönetici tarafından aktif edildi. 🎉";
            } else {
                // İptal ediliyorsa ve sebep varsa ekle
                mesaj = "Premium üyeliğiniz iptal edildi.";
                if (sebep != null && !sebep.trim().isEmpty()) {
                    mesaj += " Sebep: " + sebep;
                }
            }
            bildirimService.bildirimGonder(uye, mesaj);
            
            redirectAttributes.addFlashAttribute("basariliMesaj", "Kullanıcının durumu güncellendi ve bildirim gönderildi.");
        }
        return "redirect:/admin";
    }

    // --- YENİ: DUYURU GÖNDER ---
    @PostMapping("/admin/duyuru-gonder")
    public String topluDuyuru(@RequestParam String mesaj, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        Kullanici admin = kullaniciService.kullaniciBul(principal.getName());
        if (!"ROLE_ADMIN".equals(admin.getRol())) return "error/403";

        List<Kullanici> tumKullanicilar = kullaniciRepository.findAll();
        for (Kullanici k : tumKullanicilar) {
            bildirimService.bildirimGonder(k, "📢 DUYURU: " + mesaj);
        }
        redirectAttributes.addFlashAttribute("basariliMesaj", "Duyuru tüm üyelere gönderildi.");
        return "redirect:/admin";
    }

    // --- YENİ: BİLDİRİMLERİ OKUNDU İŞARETLE ---
    @GetMapping("/bildirimleri-oku")
    public String bildirimleriOku(Principal principal) {
        if (principal != null) {
            Kullanici k = kullaniciService.kullaniciBul(principal.getName());
            bildirimService.hepsiniOkunduIsaretle(k);
        }
        return "redirect:/";
    }
 // --- BİLDİRİM SİL ---
    @GetMapping("/bildirim-sil/{id}")
    public String bildirimSil(@PathVariable Long id, Principal principal) {
        if (principal != null) {
            Kullanici k = kullaniciService.kullaniciBul(principal.getName());
            bildirimService.bildirimSil(id, k);
        }
        return "redirect:/"; // İşlem bitince anasayfayı yenile
    }
    @GetMapping("/yorum-sil/{id}")
    public String yorumSil(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        Kullanici k = kullaniciService.kullaniciBul(principal.getName());
        if ("ROLE_ADMIN".equals(k.getRol())) {
            yorumRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("basariliMesaj", "Yorum başarıyla silindi.");
        }
        return "redirect:/";
    }

    // --- ANKET SİLME ---
    @GetMapping("/anket-sil/{id}")
    public String anketSil(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (principal == null) return "redirect:/login";
        
        Kullanici k = kullaniciService.kullaniciBul(principal.getName());
        Anket a = anketRepository.findById(id).orElse(null);
        
        if (a != null) {
            if ("ROLE_ADMIN".equals(k.getRol()) || (a.getOlusturan() != null && a.getOlusturan().getId().equals(k.getId()))) {
                anketService.anketSil(id);
                redirectAttributes.addFlashAttribute("basariliMesaj", "Anket başarıyla silindi.");
            } else {
                redirectAttributes.addFlashAttribute("hataMesaji", "Bu anketi silme yetkiniz yok!");
            }
        }
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // --- ANKET OLUŞTURMA ---
    @GetMapping("/yeni-anket")
    public String anketOlusturmaSayfasi(Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        String kAdi = principal.getName();
        Kullanici k = kullaniciService.kullaniciBul(kAdi);
        if ("ROLE_ADMIN".equals(k.getRol()) || k.isPremiumMu()) {
            return "anket-olustur"; 
        } else {
            redirectAttributes.addFlashAttribute("hataMesaji", "Anket oluşturmak için Premium Üye olmalısınız!");
            return "redirect:/premium-ol";
        }
    }
    
    @PostMapping("/anket-olustur")
    public String anketOlustur(@RequestParam String baslik, 
                               @RequestParam(required = false) String aciklama, 
                               @RequestParam String kategori, 
                               @RequestParam(required = false) String bitisTarihiStr, 
                               @RequestParam List<String> secenekMetinleri, 
                               @RequestParam(required = false) List<String> secenekResimleri,
                               Principal principal) {
        
        Kullanici k = kullaniciService.kullaniciBul(principal.getName());
        Anket yeniAnket = new Anket(); 
        yeniAnket.setBaslik(baslik); 
        yeniAnket.setAciklama(aciklama); 
        yeniAnket.setKategori(kategori); 
        yeniAnket.setAktif(true); 
        yeniAnket.setOlusturan(k); 
        
        if (bitisTarihiStr != null && !bitisTarihiStr.isEmpty()) { 
            try {
                yeniAnket.setBitisTarihi(LocalDate.parse(bitisTarihiStr).atTime(23, 59, 59)); 
            } catch (Exception e) {
                yeniAnket.setBitisTarihi(null); 
            }
        }
        
        List<Secenek> secenekListesi = new ArrayList<>();
        for (int i = 0; i < secenekMetinleri.size(); i++) {
            String metin = secenekMetinleri.get(i);
            if (metin != null && !metin.trim().isEmpty()) { 
                Secenek s = new Secenek(); 
                s.setMetin(metin); 
                s.setAnket(yeniAnket);
                if (secenekResimleri != null && i < secenekResimleri.size()) {
                    String resim = secenekResimleri.get(i);
                    if (resim != null && !resim.trim().isEmpty()) {
                        s.setResimUrl(resim);
                    }
                }
                secenekListesi.add(s); 
            } 
        }
        yeniAnket.setSecenekler(secenekListesi); 
        anketService.anketKaydet(yeniAnket);
        return "redirect:/profil"; 
    }

    // --- YAPAY ZEKA ENDPOINT ---
    @GetMapping("/api/ai-oner")
    @ResponseBody
    public ResponseEntity<?> yapayZekaOnerWeb(@RequestParam String konu) {
        try {
            String aiCevap = geminiService.anketSorusuOner(konu);
            return ResponseEntity.ok(Map.of("success", true, "oneri", aiCevap));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --- OY VERME ---
    @GetMapping("/oy-ver/{secenekId}") 
    public String oyVer(@PathVariable Long secenekId, Principal principal, RedirectAttributes redirectAttributes) { 
        if (principal == null) return "redirect:/login"; 
        String kAdi = principal.getName(); 
        Kullanici kullanici = kullaniciService.kullaniciBul(kAdi); 
        boolean basarili = anketService.oyVer(secenekId, kullanici); 
        if (!basarili) { redirectAttributes.addFlashAttribute("hataMesaji", "Zaten oy kullandınız veya anket süresi doldu!"); } 
        else { redirectAttributes.addFlashAttribute("basariliMesaj", "Oyunuz kaydedildi!"); } 
        return "redirect:/"; 
    }

    @GetMapping("/api/oy-ver/{secenekId}")
    @ResponseBody 
    public ResponseEntity<Map<String, Object>> oyVerApi(@PathVariable Long secenekId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Lütfen giriş yapın.");
            return ResponseEntity.ok(response);
        }
        Kullanici k = kullaniciService.kullaniciBul(principal.getName());
        boolean basarili = anketService.oyVer(secenekId, k);
        if (basarili) {
            response.put("success", true);
            response.put("message", "Oyunuz kaydedildi!");
        } else {
            response.put("success", false);
            response.put("message", "Zaten oy kullandınız veya süre doldu.");
        }
        return ResponseEntity.ok(response);
    }

    // --- DİĞER İŞLEMLER ---
    @GetMapping("/login") public String girisSayfasi() { return "login"; }
    @GetMapping("/register") public String kayitSayfasi() { return "register"; }
    @PostMapping("/register") public String kayitOl(@RequestParam String kullaniciAdi, @RequestParam String sifre, @RequestParam String email, RedirectAttributes redirectAttributes) { if (kullaniciService.kullaniciBul(kullaniciAdi) != null) { redirectAttributes.addFlashAttribute("hata", "Bu kullanıcı adı zaten kullanılıyor!"); return "redirect:/register"; } Kullanici k = new Kullanici(); k.setKullaniciAdi(kullaniciAdi); k.setSifre(sifre); k.setEmail(email); k.setRol("ROLE_USER"); k.setPremiumMu(false); kullaniciService.kullaniciKaydet(k); return "redirect:/login"; }
    @GetMapping("/logout") public String cikisYap(HttpServletRequest request, HttpServletResponse response) { Authentication auth = SecurityContextHolder.getContext().getAuthentication(); if (auth != null) { new SecurityContextLogoutHandler().logout(request, response, auth); } return "redirect:/login?logout"; }
    @PostMapping("/yorum-yap") public String yorumYap(@RequestParam Long anketId, @RequestParam String yorumMetni, Principal principal, RedirectAttributes redirectAttributes) { if (principal == null) return "redirect:/login"; if (yorumMetni == null || yorumMetni.trim().isEmpty()) { return "redirect:/"; } Kullanici k = kullaniciService.kullaniciBul(principal.getName()); Anket a = anketRepository.findById(anketId).orElse(null); if (a != null) { Yorum yeniYorum = new Yorum(); yeniYorum.setIcerik(yorumMetni); yeniYorum.setKullanici(k); yeniYorum.setAnket(a); yorumRepository.save(yeniYorum); redirectAttributes.addFlashAttribute("basariliMesaj", "Yorumunuz paylaşıldı!"); } return "redirect:/"; }
    @GetMapping("/premium-ol") public String premiumSayfasi(Model model, Principal principal) { kullaniciBilgisiYukle(model, principal); return "premium"; }
    @GetMapping("/odeme-sayfasi") public String odemeBaslat(@RequestParam String paketTipi, Model model, Principal principal) { if (principal == null) return "redirect:/login"; String kAdi = principal.getName(); Kullanici k = kullaniciService.kullaniciBul(kAdi); String fiyat = "99"; if ("Yıllık".equals(paketTipi)) fiyat = "890"; CheckoutFormInitialize checkoutForm = iyzipayService.odemeFormuOlustur(k, paketTipi, fiyat); if ("success".equals(checkoutForm.getStatus())) { model.addAttribute("iyzicoFormIcerigi", checkoutForm.getCheckoutFormContent()); model.addAttribute("paketTipi", paketTipi); model.addAttribute("fiyat", fiyat); kullaniciBilgisiYukle(model, principal); return "odeme"; } else { model.addAttribute("hataMesaji", "Ödeme sistemi başlatılamadı: " + checkoutForm.getErrorMessage()); kullaniciBilgisiYukle(model, principal); return "premium"; } }
    @PostMapping("/odeme-sonuc") public String odemeSonuc(@RequestParam(required = false) String token, Principal principal, RedirectAttributes redirectAttributes) { if (principal == null) return "redirect:/login"; String kAdi = principal.getName(); Kullanici k = kullaniciService.kullaniciBul(kAdi); k.setPremiumMu(true); kullaniciRepository.save(k); redirectAttributes.addFlashAttribute("basariliMesaj", "Ödeme Başarılı! Premium üyeliğiniz aktif edildi. 🎉"); return "redirect:/"; }

    private void kullaniciBilgisiYukle(Model model, Principal principal) {
        if (principal != null) {
            String kAdi = principal.getName();
            model.addAttribute("kullaniciAdi", kAdi);
            Kullanici k = kullaniciService.kullaniciBul(kAdi);
            if (k != null) {
                model.addAttribute("isAdmin", "ROLE_ADMIN".equals(k.getRol()));
                model.addAttribute("isPremium", k.isPremiumMu());
                model.addAttribute("kullaniciPuan", k.getPuan());
                model.addAttribute("kullaniciRutbe", k.getRutbe());
                model.addAttribute("kullaniciRutbeRenk", k.getRutbeRenk());
                
                // --- BİLDİRİMLERİ EKLE ---
                List<Bildirim> bildirimler = bildirimService.kullaniciBildirimleriniGetir(k);
                model.addAttribute("bildirimListesi", bildirimler);
                model.addAttribute("okunmamisBildirimSayisi", bildirimService.okunmamisSayisi(k));
            }
        } else {
            model.addAttribute("isAdmin", false);
            model.addAttribute("isPremium", false);
            model.addAttribute("kullaniciPuan", 0);
        }
    }
}