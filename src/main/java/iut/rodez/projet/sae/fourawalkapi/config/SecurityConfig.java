package iut.rodez.projet.sae.fourawalkapi.config;

import iut.rodez.projet.sae.fourawalkapi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuration principale de Spring Security pour l'API REST.
 * Définit les règles d'accès, le mode de session et l'intégration du filtre JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;

    // Injection du filtre JWT
    public SecurityConfig(JwtAuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    /**
     * Définit le PasswordEncoder (BCrypt) utilisé pour le hachage et la vérification des mots de passe.
     * @return Le BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose l'AuthenticationManager pour l'utiliser lors de l'authentification (/login).
     * @param config La configuration de l'authentification.
     * @return L'AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Définit la chaîne de filtres de sécurité.
     * C'est ici que nous définissons que l'API est stateless et que nous intégrons le JWT.
     * @param http L'objet HttpSecurity pour configurer la sécurité.
     * @return La chaîne de filtres de sécurité.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactiver le CSRF (Cross-Site Request Forgery) pour les API stateless
                .csrf(csrf -> csrf.disable())

                // 2. Définir la politique de session comme sans état (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Définir les autorisations d'accès aux Endpoints
                .authorizeHttpRequests(authorize -> authorize

                        // --- Endpoints Publics (Autorisés sans token) ---
                        .requestMatchers(
                                // Utilisateurs : Inscription et Connexion
                                new AntPathRequestMatcher("/api/v1/users/register", HttpMethod.POST.toString()),
                                new AntPathRequestMatcher("/api/v1/users/login", HttpMethod.POST.toString())

                                // Ajout d'autres Endpoints publics futurs si nécessaire (ex: documentation API)
                        ).permitAll()

                        // --- Endpoints Privés (Nécessitent un token JWT valide) ---
                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // 4. Intégrer notre filtre JWT
                // Ce filtre s'exécute AVANT le filtre d'authentification par défaut de Spring.
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}