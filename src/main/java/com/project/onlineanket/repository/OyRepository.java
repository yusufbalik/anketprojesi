package com.project.onlineanket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.onlineanket.entity.Oy;

public interface OyRepository extends JpaRepository<Oy, Long> {

    // Bu kullanıcı bu ankete daha önce oy vermiş mi? (Evet/Hayır)
    boolean existsByKullaniciIdAndAnketId(Long kullaniciId, Long anketId);
}