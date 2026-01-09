package com.project.onlineanket.service;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime; // ✅ DÜZELTİLDİ: Date yerine LocalDateTime import edildi

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.onlineanket.entity.Anket;
import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.entity.Oy;
import com.project.onlineanket.entity.Secenek;
import com.project.onlineanket.entity.Yorum;
import com.project.onlineanket.repository.AnketRepository;
import com.project.onlineanket.repository.KullaniciRepository;
import com.project.onlineanket.repository.OyRepository;
import com.project.onlineanket.repository.SecenekRepository;
import com.project.onlineanket.repository.YorumRepository;

@Service
public class AnketService {

    @Autowired private AnketRepository anketRepository;
    @Autowired private OyRepository oyRepository;
    @Autowired private SecenekRepository secenekRepository;
    @Autowired private KullaniciRepository kullaniciRepository;
    @Autowired private YorumRepository yorumRepository;

    public List<Anket> tumAnketleriGetir() {
        return anketRepository.findAllByAktifTrue(); 
    }

    public List<Anket> kategoriyeGoreGetir(String kategori) {
        return anketRepository.findByKategoriAndAktifTrue(kategori);
    }
    
    public List<Anket> aramayaGoreGetir(String kelime) {
        return anketRepository.findByBaslikContainingIgnoreCaseAndAktifTrue(kelime);
    }

    // --- POPÜLER ANKETLER ---
    public List<Anket> populerAnketleriGetir() {
        List<Anket> hepsi = anketRepository.findAllByAktifTrue();
        hepsi.sort((a1, a2) -> Integer.compare(a2.getToplamOy(), a1.getToplamOy()));
        
        if (hepsi.size() > 3) {
            return hepsi.subList(0, 3);
        }
        return hepsi;
    }

    public List<Anket> kullanicininGecmisi(Long kullaniciId) {
        List<Oy> tumOylar = oyRepository.findAll();
        List<Anket> katildigimAnketler = new ArrayList<>();
        for (Oy oy : tumOylar) {
            if (oy.getKullanici().getId().equals(kullaniciId)) {
                Anket oAnket = oy.getAnket();
                if (!katildigimAnketler.contains(oAnket)) {
                    katildigimAnketler.add(oAnket);
                }
            }
        }
        return katildigimAnketler;
    }

    public Anket anketKaydet(Anket anket) {
        anket.setAktif(true); 
        Kullanici olusturan = anket.getOlusturan();
        if (olusturan != null) {
            olusturan.setPuan(olusturan.getPuan() + 15);
            kullaniciRepository.save(olusturan);
        }
        return anketRepository.save(anket);
    }
    
    public void anketSil(Long id) {
        Anket a = anketRepository.findById(id).orElse(null);
        if (a != null) {
            a.setAktif(false); 
            anketRepository.save(a);
        }
    }

    public boolean oyVer(Long secenekId, Kullanici kullanici) {
        Secenek secenek = secenekRepository.findById(secenekId).orElse(null);
        if (secenek == null) return false;

        Anket anket = secenek.getAnket();
        if (!anket.isAktif()) return false;

        if (kullanici == null) return false;

        if (oyRepository.existsByKullaniciIdAndAnketId(kullanici.getId(), anket.getId())) {
            return false; 
        }

        Oy yeniOy = new Oy();
        yeniOy.setKullanici(kullanici);
        yeniOy.setAnket(anket);
        yeniOy.setSecenek(secenek);
        oyRepository.save(yeniOy);

        secenek.setOySayisi(secenek.getOySayisi() + 1);
        secenekRepository.save(secenek);
        
        kullanici.setPuan(kullanici.getPuan() + 5);
        kullaniciRepository.save(kullanici);

        return true; 
    }

    // --- ANKET BULMA (Helper) ---
    public Anket anketBul(Long id) {
        return anketRepository.findById(id).orElse(null);
    }

    // ==========================================
    // ✅ GÜNCELLENEN METOD: YORUM YAPMA
    // ==========================================
    public void yorumYap(Long anketId, Kullanici kullanici, String icerik) {
        Anket anket = anketRepository.findById(anketId).orElse(null);
        
        if (anket != null && kullanici != null) {
            Yorum yeniYorum = new Yorum();
            yeniYorum.setAnket(anket);
            yeniYorum.setKullanici(kullanici);
            yeniYorum.setIcerik(icerik);
            
            // ✅ DÜZELTİLDİ: new Date() yerine LocalDateTime.now() kullanıldı
            yeniYorum.setTarih(LocalDateTime.now()); 
            
            yorumRepository.save(yeniYorum); 
        } else {
            throw new RuntimeException("Anket veya Kullanıcı bulunamadı, yorum yapılamadı.");
        }
    }
}