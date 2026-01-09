package com.project.onlineanket.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.repository.KullaniciRepository;

@Service
public class KullaniciService {

    @Autowired
    private KullaniciRepository kullaniciRepository;

    // 1. Yeni Kullanıcı Kaydetme (Web ve Mobil ortak kullanır)
    public Kullanici kullaniciKaydet(Kullanici kullanici) {
        return kullaniciRepository.save(kullanici);
    }

    // 2. Tüm Kullanıcıları Getirme (Web Admin panelinde liste için)
    public List<Kullanici> tumKullanicilariGetir() {
        return kullaniciRepository.findAll();
    }

    // 3. Kullanıcı Adıyla Arama (Giriş işlemleri için)
    public Kullanici kullaniciBul(String ad) {
        return kullaniciRepository.findByKullaniciAdi(ad).orElse(null);
    }

    // 4. ID ile Kullanıcı Bulma (Profil ve Detay işlemleri için)
    public Kullanici kullaniciIdIleBul(Long id) {
        return kullaniciRepository.findById(id).orElse(null);
    }

    // 5. Admin Paneli İçin: Premium Durumunu Değiştir (Aç/Kapa)
    // Admin bir tuşa basarak premium'u geri alabilir veya verebilir.
    public boolean premiumDurumuDegistir(Long id) {
        Kullanici k = kullaniciRepository.findById(id).orElse(null);
        if (k != null) {
            boolean yeniDurum = !k.isPremiumMu(); // True ise False, False ise True yapar
            k.setPremiumMu(yeniDurum);
            kullaniciRepository.save(k);
            return true; // İşlem başarılı
        }
        return false; // Kullanıcı bulunamadı
    }

    // 6. Ödeme Sistemi İçin: KESİN OLARAK PREMIUM YAP
    // Bu metod "toggle" (aç/kapa) yapmaz. Sadece TRUE yapar.
    // Iyzico'dan "Başarılı" onayı geldiğinde MobilApiController bunu çağırır.
    public void premiumYap(Long id) {
        Kullanici k = kullaniciRepository.findById(id).orElse(null);
        if (k != null) {
            // Zaten premium olsa bile tekrar true set etmekte sakınca yok (Süre uzatma mantığı gibi)
            k.setPremiumMu(true); 
            kullaniciRepository.save(k);
        }
    }
}