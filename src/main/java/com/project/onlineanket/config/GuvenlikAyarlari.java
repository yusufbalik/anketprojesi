package com.project.onlineanket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class GuvenlikAyarlari {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ✅ KESİN ÇÖZÜM:
                // 1. Statik dosyalar (CSS, JS, Resimler)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // 2. Giriş ve Kayıt sayfaları
                .requestMatchers("/login", "/register", "/error").permitAll()
                // 3. MOBİL API ve ADMİN API (GET/POST hepsi serbest)
                .requestMatchers("/api/mobil/**", "/api/public/**", "/admin/**").permitAll()
                
                // Geri kalan her şey (Web arayüzü) giriş gerektirir
                .anyRequest().authenticated()
            )
            // Form login sadece tarayıcı için kalsın
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .permitAll()
            )
            // ⚠️ CSRF Korumasını tamamen kapatıyoruz (Mobilden POST atmak için şart)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}