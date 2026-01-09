package com.project.onlineanket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.onlineanket.entity.Anket;
import com.project.onlineanket.entity.Kullanici;
import java.util.List;

public interface AnketRepository extends JpaRepository<Anket, Long> {
    
    // Ana sayfa için sadece aktifleri getirir (Doğruydu, dokunmadık)
    List<Anket> findAllByAktifTrue();

    // 🛠️ DÜZELTME BURADA: 
    // Eskiden: findByKategori(String kategori); -> Silinenleri de getiriyordu.
    // Şimdi: Hem kategorisi tutacak HEM DE aktif olacak.
    List<Anket> findByKategoriAndAktifTrue(String kategori);

    // Arama yaparken de silinenler gelmesin diye burayı da güncelledim
    List<Anket> findByBaslikContainingIgnoreCaseAndAktifTrue(String keyword);

    // Kullanıcının anketlerini getirirken hepsi (silinenler dahil) gelebilir, burası kalabilir
    List<Anket> findByOlusturan(Kullanici olusturan);
}