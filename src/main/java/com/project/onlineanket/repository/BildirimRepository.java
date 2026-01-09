package com.project.onlineanket.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.onlineanket.entity.Bildirim;
import com.project.onlineanket.entity.Kullanici;

public interface BildirimRepository extends JpaRepository<Bildirim, Long> {
    // Kullanıcının bildirimlerini tarihe göre tersten (en yeni en üstte) getir
    List<Bildirim> findByKullaniciOrderByTarihDesc(Kullanici kullanici);
    
    // Okunmamış bildirim sayısı (Badge için)
    long countByKullaniciAndOkunduMuFalse(Kullanici kullanici);
}