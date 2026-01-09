package com.project.onlineanket.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.onlineanket.entity.Anket;
import com.project.onlineanket.service.AnketService;

@RestController
@RequestMapping("/api/anketler")
public class AnketController {

    @Autowired
    private AnketService anketService;

    // Tüm anketleri JSON olarak döndürür
    @GetMapping
    public List<Anket> hepsiniGetir() {
        // Serviste oluşturduğumuz "tumAnketleriGetir" metodunu çağırıyoruz
        return anketService.tumAnketleriGetir();
    }

    // Yeni anket ekler (Postman vb. araçlar için)
    @PostMapping
    public Anket ekle(@RequestBody Anket anket) {
        return anketService.anketKaydet(anket);
    }
}