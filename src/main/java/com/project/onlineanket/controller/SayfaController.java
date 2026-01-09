package com.project.onlineanket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SayfaController {

    // Bu komut, "anket-olustur.html" dosyasını bulup ekrana getirir
    @GetMapping("/anket-olustur")
    public String anketSayfasiniAc() {
        return "anket-olustur"; 
    }
}