package com.project.onlineanket.dto; // Kendi paket adını yaz

public class KayitIstegi {
    private String kullaniciAdi;
    private String email;
    private String sifre;

    // Getter ve Setter'lar
    public String getKullaniciAdi() { return kullaniciAdi; }
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }
}