package iut.rodez.projet.sae.fourawalkapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * Définit le BCryptPasswordEncoder comme Bean injectable.
     * BCrypt est l'algorithme recommandé et gère le salage automatiquement.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}