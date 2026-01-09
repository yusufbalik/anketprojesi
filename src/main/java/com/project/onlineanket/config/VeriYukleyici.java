package com.project.onlineanket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.onlineanket.entity.Anket;
import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.entity.Secenek;
import com.project.onlineanket.repository.AnketRepository;
import com.project.onlineanket.repository.KullaniciRepository;
import com.project.onlineanket.repository.SecenekRepository;

@Component
public class VeriYukleyici implements CommandLineRunner {

    @Autowired private KullaniciRepository kullaniciRepository;
    @Autowired private AnketRepository anketRepository;
    @Autowired private SecenekRepository secenekRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // EĞER VERİTABANI ZATEN DOLUYSA HİÇBİR ŞEY YAPMA (ÇIK)
        if (kullaniciRepository.count() > 0) {
            System.out.println("--- Veriler zaten var, yükleme atlandı ---");
            return; 
        }

        // --- AŞAĞISI SADECE İLK KEZ ÇALIŞIR ---

        // 1. ADMİN KULLANICISINI EKLE
        Kullanici admin = new Kullanici();
        admin.setKullaniciAdi("yusuf");
        admin.setEmail("yusuf@gmail.com");
        admin.setSifre("12345"); 
        admin.setPremiumMu(true);
        // ÖNEMLİ DÜZELTME: Spring Security için "ROLE_" ön eki şarttır!
        admin.setRol("ROLE_ADMIN"); 
        kullaniciRepository.save(admin);

        // 2. ÖRNEK ANKET EKLE
        Anket anket = new Anket();
        anket.setBaslik("En sevdiğiniz programlama dili?");
        anket.setAciklama("Hangisini daha çok seviyorsunuz?");
        anket.setAktif(true);
        anket = anketRepository.save(anket);

        // 3. ŞIKLARI EKLE
        Secenek s1 = new Secenek();
        s1.setMetin("Java");
        s1.setSira(1);
        s1.setAnket(anket);
        secenekRepository.save(s1);

        Secenek s2 = new Secenek();
        s2.setMetin("Python");
        s2.setSira(2);
        s2.setAnket(anket);
        secenekRepository.save(s2);

        System.out.println("--- İLK KURULUM TAMAMLANDI (Kullanıcı: yusuf / Şifre: 12345) ---");
    }
}