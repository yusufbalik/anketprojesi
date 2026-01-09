package com.project.onlineanket.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.service.KullaniciService;

@RestController
@RequestMapping("/api/kullanicilar")
public class KullaniciController {

    @Autowired
    private KullaniciService kullaniciService;

    // Tüm kullanıcıları listele
    @GetMapping
    public List<Kullanici> hepsiniGetir() {
        return kullaniciService.tumKullanicilariGetir();
    }

    // Yeni kullanıcı ekle
    @PostMapping
    public Kullanici ekle(@RequestBody Kullanici kullanici) {
        return kullaniciService.kullaniciKaydet(kullanici);
    }
}