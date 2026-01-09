package com.project.onlineanket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "panolar")
public class Pano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String baslik;
    private String aciklama;
    private boolean herkeseAcik;
    private LocalDateTime olusturulmaTarihi = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "kullanici_id")
    private Kullanici kullanici;

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBaslik() { return baslik; }
    public void setBaslik(String baslik) { this.baslik = baslik; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
    public boolean isHerkeseAcik() { return herkeseAcik; }
    public void setHerkeseAcik(boolean herkeseAcik) { this.herkeseAcik = herkeseAcik; }
    public LocalDateTime getOlusturulmaTarihi() { return olusturulmaTarihi; }
    public void setOlusturulmaTarihi(LocalDateTime olusturulmaTarihi) { this.olusturulmaTarihi = olusturulmaTarihi; }
    public Kullanici getKullanici() { return kullanici; }
    public void setKullanici(Kullanici kullanici) { this.kullanici = kullanici; }
}