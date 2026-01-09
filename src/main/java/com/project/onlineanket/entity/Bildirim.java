package com.project.onlineanket.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class Bildirim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String icerik;
    private LocalDateTime tarih;
    private boolean okunduMu; // Yeni bildirim mi?

    @ManyToOne
    @JoinColumn(name = "kullanici_id")
    private Kullanici kullanici; // Kime gönderildi?

    public Bildirim() {
        this.tarih = LocalDateTime.now();
        this.okunduMu = false;
    }

    public Bildirim(String icerik, Kullanici kullanici) {
        this.icerik = icerik;
        this.kullanici = kullanici;
        this.tarih = LocalDateTime.now();
        this.okunduMu = false;
    }

    // Getter - Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIcerik() { return icerik; }
    public void setIcerik(String icerik) { this.icerik = icerik; }
    public LocalDateTime getTarih() { return tarih; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }
    public boolean isOkunduMu() { return okunduMu; }
    public void setOkunduMu(boolean okunduMu) { this.okunduMu = okunduMu; }
    public Kullanici getKullanici() { return kullanici; }
    public void setKullanici(Kullanici kullanici) { this.kullanici = kullanici; }
}