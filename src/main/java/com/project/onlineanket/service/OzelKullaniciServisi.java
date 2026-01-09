package com.project.onlineanket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.onlineanket.entity.Kullanici;
import com.project.onlineanket.repository.KullaniciRepository;

@Service
public class OzelKullaniciServisi implements UserDetailsService {

    @Autowired
    private KullaniciRepository kullaniciRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // HATA BURADAYDI: .orElse(null) ekleyerek kutudan çıkardık
        Kullanici k = kullaniciRepository.findByKullaniciAdi(username).orElse(null);

        if (k == null) {
            throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + username);
        }

        return User.builder()
            .username(k.getKullaniciAdi())
            .password(k.getSifre()) // {noop} eklemedik, çünkü zaten NoOpEncoder kullanıyoruz
            .roles(k.getRol().replace("ROLE_", "")) 
            .build();
    }
}