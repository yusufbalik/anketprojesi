package com.project.onlineanket.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.onlineanket.entity.Bildirim;
import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.repository.BildirimRepository;

@Service
public class BildirimService {

    @Autowired
    private BildirimRepository bildirimRepository;

    // Kullanıcıya yeni bildirim gönderir
    public void bildirimGonder(Kullanici k, String mesaj) {
        Bildirim b = new Bildirim(mesaj, k);
        bildirimRepository.save(b);
    }

    // Kullanıcının tüm bildirimlerini (yeniden eskiye) getirir
    public List<Bildirim> kullaniciBildirimleriniGetir(Kullanici k) {
        return bildirimRepository.findByKullaniciOrderByTarihDesc(k);
    }

    // Okunmamış bildirim sayısını döner (Badge için)
    public long okunmamisSayisi(Kullanici k) {
        return bildirimRepository.countByKullaniciAndOkunduMuFalse(k);
    }

    // Tüm bildirimleri okundu olarak işaretler
    public void hepsiniOkunduIsaretle(Kullanici k) {
        List<Bildirim> liste = bildirimRepository.findByKullaniciOrderByTarihDesc(k);
        for (Bildirim b : liste) {
            if (!b.isOkunduMu()) {
                b.setOkunduMu(true);
                bildirimRepository.save(b);
            }
        }
    }
 // Bildirim Silme (Güvenlik Kontrollü)
    public void bildirimSil(Long id, Kullanici k) {
        Bildirim b = bildirimRepository.findById(id).orElse(null);
        // Bildirim varsa VE bu bildirim gerçekten silmek isteyen kullanıcıya aitse sil
        if (b != null && b.getKullanici().getId().equals(k.getId())) {
            bildirimRepository.delete(b);
        }
    }
}