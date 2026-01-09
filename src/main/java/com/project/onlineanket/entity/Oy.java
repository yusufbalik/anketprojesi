package com.project.onlineanket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "oylar")
public class Oy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime oyTarihi = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "kullanici_id")
    private Kullanici kullanici;

    @ManyToOne
    @JoinColumn(name = "anket_id")
    private Anket anket;

    @ManyToOne
    @JoinColumn(name = "secenek_id")
    private Secenek secenek;

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getOyTarihi() { return oyTarihi; }
    public void setOyTarihi(LocalDateTime oyTarihi) { this.oyTarihi = oyTarihi; }
    public Kullanici getKullanici() { return kullanici; }
    public void setKullanici(Kullanici kullanici) { this.kullanici = kullanici; }
    public Anket getAnket() { return anket; }
    public void setAnket(Anket anket) { this.anket = anket; }
    public Secenek getSecenek() { return secenek; }
    public void setSecenek(Secenek secenek) { this.secenek = secenek; }
}