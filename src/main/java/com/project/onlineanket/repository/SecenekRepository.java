package com.project.onlineanket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.onlineanket.entity.Secenek;
import java.util.List;

public interface SecenekRepository extends JpaRepository<Secenek, Long> {
    
    // Bir ankete ait şıkları getirmek istersen diye hazırda dursun
    List<Secenek> findByAnketId(Long anketId);
}