package com.project.onlineanket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.onlineanket.entity.Pano;
import java.util.List;

public interface PanoRepository extends JpaRepository<Pano, Long> {
    
    // Bir kullanıcının oluşturduğu panoları bulmak için
    List<Pano> findByKullaniciId(Long kullaniciId);
}