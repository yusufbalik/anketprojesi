package com.project.onlineanket.controller;

import com.project.onlineanket.entity.Anket;
import com.project.onlineanket.entity.Secenek;
import com.project.onlineanket.service.AnketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime; // Anket entity'si LocalDateTime kullandığı için bunu seçtik
import java.util.ArrayList;
import java.util.List;

@Controller
public class AnketFormController {

    @Autowired
    private AnketService anketService;

    @PostMapping("/anket/kaydet")
    public String anketiKaydet(
            @RequestParam String soruMetni,  
            @RequestParam String secenekA,   
            @RequestParam String secenekB,   
            @RequestParam String secenekC,   
            @RequestParam String secenekD    
    ) {
        // 1. Yeni Anket Nesnesi Oluştur
        Anket yeniAnket = new Anket();
        
        // DÜZELTME BURADA YAPILDI:
        // Senin Anket.java dosyasında 'metin' yok, 'baslik' var.
        yeniAnket.setBaslik(soruMetni); 
        
        // Tarih formatını LocalDateTime yaptık
        yeniAnket.setOlusturulmaTarihi(LocalDateTime.now());
        yeniAnket.setAktif(true); // Varsayılan olarak aktif olsun

        // 2. Şıkları Oluştur
        List<Secenek> siklar = new ArrayList<>();

        // Şık A
        Secenek s1 = new Secenek();
        s1.setMetin(secenekA); // Secenek.java'da 'metin' olduğu için bu doğru
        s1.setSira(1);         // Veritabanında sırayla görünsün diye
        s1.setAnket(yeniAnket); 
        siklar.add(s1);

        // Şık B
        Secenek s2 = new Secenek();
        s2.setMetin(secenekB);
        s2.setSira(2);
        s2.setAnket(yeniAnket);
        siklar.add(s2);

        // Şık C
        Secenek s3 = new Secenek();
        s3.setMetin(secenekC);
        s3.setSira(3);
        s3.setAnket(yeniAnket);
        siklar.add(s3);

        // Şık D
        Secenek s4 = new Secenek();
        s4.setMetin(secenekD);
        s4.setSira(4);
        s4.setAnket(yeniAnket);
        siklar.add(s4);

        // 3. Şıkları Ankete Ekle
        yeniAnket.setSecenekler(siklar);

        // 4. Veritabanına Kaydet
        anketService.anketKaydet(yeniAnket);

        // 5. İşlem bitince sayfayı yenile
        return "redirect:/anket-olustur?basarili";
    }
}