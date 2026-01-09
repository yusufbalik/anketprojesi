package com.project.onlineanket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime; // TARİH İÇİN GEREKLİ
import java.util.List;

@Entity
@Table(name = "kullanicilar")
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kullanici_adi", unique = true, nullable = false)
    private String kullaniciAdi;

    private String sifre;
    private String email;
    private boolean premiumMu;
    
    private String rol = "ROLE_USER"; 
    
    private int puan = 0; 

    // --- YENİ EKLENDİ: KAYIT TARİHİ ---
    private LocalDateTime kayitTarihi;

    @OneToMany(mappedBy = "kullanici")
    private List<Pano> panolar;

    @OneToMany(mappedBy = "kullanici", cascade = CascadeType.ALL)
    private List<Yorum> yorumlar;

    // --- OTOMATİK TARİH ATAMA (Kayıt olurken çalışır) ---
    @PrePersist
    protected void onCreate() {
        if (kayitTarihi == null) {
            kayitTarihi = LocalDateTime.now();
        }
    }

    // --- RÜTBE HESAPLAMA ---
    public String getRutbe() {
        if (puan < 50) return "Çaylak";
        else if (puan < 200) return "Deneyimli";
        else if (puan < 500) return "Usta";
        else return "Titanium Üstadı";
    }
    
    public String getRutbeRenk() {
        if (puan < 50) return "#64748b"; 
        else if (puan < 200) return "#3b82f6"; 
        else if (puan < 500) return "#f59e0b"; 
        else return "linear-gradient(135deg, #06b6d4, #3b82f6)"; 
    }

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKullaniciAdi() { return kullaniciAdi; }
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }
    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isPremiumMu() { return premiumMu; }
    public void setPremiumMu(boolean premiumMu) { this.premiumMu = premiumMu; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public int getPuan() { return puan; }
    public void setPuan(int puan) { this.puan = puan; }
    public List<Yorum> getYorumlar() { return yorumlar; }
    public void setYorumlar(List<Yorum> yorumlar) { this.yorumlar = yorumlar; }
    
    // Tarih Getter/Setter
    public LocalDateTime getKayitTarihi() { return kayitTarihi; }
    public void setKayitTarihi(LocalDateTime kayitTarihi) { this.kayitTarihi = kayitTarihi; }
}