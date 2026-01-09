package com.project.onlineanket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "yorumlar")
public class Yorum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500) // Yorum çok uzun olmasın
    private String icerik;

    private LocalDateTime tarih = LocalDateTime.now();

    // Yorumu kim yazdı?
    @ManyToOne
    @JoinColumn(name = "kullanici_id")
    private Kullanici kullanici;

    // Hangi ankete yazıldı?
    @ManyToOne
    @JoinColumn(name = "anket_id")
    private Anket anket;

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIcerik() { return icerik; }
    public void setIcerik(String icerik) { this.icerik = icerik; }
    public LocalDateTime getTarih() { return tarih; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }
    public Kullanici getKullanici() { return kullanici; }
    public void setKullanici(Kullanici kullanici) { this.kullanici = kullanici; }
    public Anket getAnket() { return anket; }
    public void setAnket(Anket anket) { this.anket = anket; }
}