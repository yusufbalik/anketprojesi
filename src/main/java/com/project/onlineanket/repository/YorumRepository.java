package com.project.onlineanket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.onlineanket.entity.Yorum;

public interface YorumRepository extends JpaRepository<Yorum, Long> {
    // Şimdilik ekstra bir metoda gerek yok, standart işlemler yeterli.
}