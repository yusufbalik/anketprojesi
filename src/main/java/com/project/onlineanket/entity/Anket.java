package com.project.onlineanket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "anketler")
public class Anket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String baslik;
    private String aciklama;
    private String kategori; 

    private boolean aktif = true;
    private LocalDateTime olusturulmaTarihi = LocalDateTime.now();
    private LocalDateTime bitisTarihi;

    @ManyToOne
    @JoinColumn(name = "olusturan_id")
    private Kullanici olusturan;

    @OneToMany(mappedBy = "anket", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Secenek> secenekler;

    // --- YENİ EKLENDİ: Ankete yapılan yorumlar ---
    // Sıralama: En yeni yorum en üstte görünsün diye OrderBy ekledik
    @OneToMany(mappedBy = "anket", cascade = CascadeType.ALL)
    @OrderBy("tarih DESC") 
    private List<Yorum> yorumlar;

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBaslik() { return baslik; }
    public void setBaslik(String baslik) { this.baslik = baslik; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }
    public LocalDateTime getOlusturulmaTarihi() { return olusturulmaTarihi; }
    public void setOlusturulmaTarihi(LocalDateTime olusturulmaTarihi) { this.olusturulmaTarihi = olusturulmaTarihi; }
    public LocalDateTime getBitisTarihi() { return bitisTarihi; }
    public void setBitisTarihi(LocalDateTime bitisTarihi) { this.bitisTarihi = bitisTarihi; }
    public Kullanici getOlusturan() { return olusturan; }
    public void setOlusturan(Kullanici olusturan) { this.olusturan = olusturan; }
    public List<Secenek> getSecenekler() { return secenekler; }
    public void setSecenekler(List<Secenek> secenekler) { this.secenekler = secenekler; }

    public List<Yorum> getYorumlar() { return yorumlar; }
    public void setYorumlar(List<Yorum> yorumlar) { this.yorumlar = yorumlar; }

    public boolean isSuresiDoldu() {
        if (bitisTarihi == null) return false;
        return LocalDateTime.now().isAfter(bitisTarihi);
    }

    public int getToplamOy() {
        int toplam = 0;
        if (secenekler != null) { for (Secenek s : secenekler) { toplam += s.getOySayisi(); } }
        return toplam;
    }
}