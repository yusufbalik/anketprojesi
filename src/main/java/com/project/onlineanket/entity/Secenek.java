package com.project.onlineanket.entity;

import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "secenekler")
public class Secenek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metin;
    
    // --- DÜZELTME: Resim linkleri uzun olabileceği için sınırı 2048 yaptık ---
    @Column(length = 2048) 
    private String resimUrl; 
    
    private int sira;
    private int oySayisi = 0;

    @ManyToOne
    @JoinColumn(name = "anket_id")
    @JsonIgnore 
    private Anket anket;

    @OneToMany(mappedBy = "secenek", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Oy> oylar;

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMetin() { return metin; }
    public void setMetin(String metin) { this.metin = metin; }
    
    public String getResimUrl() { return resimUrl; }
    public void setResimUrl(String resimUrl) { this.resimUrl = resimUrl; }
    
    public int getSira() { return sira; }
    public void setSira(int sira) { this.sira = sira; }
    
    public int getOySayisi() { return oySayisi; }
    public void setOySayisi(int oySayisi) { this.oySayisi = oySayisi; }
    
    public Anket getAnket() { return anket; }
    public void setAnket(Anket anket) { this.anket = anket; }

    public List<Oy> getOylar() { return oylar; }
    public void setOylar(List<Oy> oylar) { this.oylar = oylar; }
}