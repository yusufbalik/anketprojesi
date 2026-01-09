package com.project.onlineanket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.onlineanket.entity.Kullanici;
import java.util.Optional;

public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {
    
    // Giriş yaparken kullanıcı adını bulmak için (Hata fırlatmaz, boş dönebilir)
    Optional<Kullanici> findByKullaniciAdi(String kullaniciAdi);
    
    // Kayıt olurken "Bu isimde biri var mı?" kontrolü için
    boolean existsByKullaniciAdi(String kullaniciAdi);
}