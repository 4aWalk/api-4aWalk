package iut.rodez.projet.sae.fourawalkapi.security;

import iut.rodez.projet.sae.fourawalkapi.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService; // Notre UserService implémente UserDetailsService

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserService userService) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Récupérer le token depuis l'en-tête HTTP
        String token = getJwtFromRequest(request);

        // 2. Valider le token et charger l'utilisateur
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {

            // 1. Récupérer l'ID directement depuis le token
            Long userId = tokenProvider.getUserId(token);

            // 2. Créer un objet d'authentification minimal sans charger la base de données
            // On met l'ID comme "principal" (identifiant de l'utilisateur connecté)
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, // On stocke l'ID ici
                    null,
                    java.util.Collections.emptyList() // Liste d'autorités vide
            );

            // 3. Définir les détails de la requête
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4. Définir l'authentification dans le SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continuer la chaîne de filtres (vers le contrôleur si tout va bien)
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT de l'en-tête 'Authorization'.
     * Le format attendu est: Authorization: Bearer <token>
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Vérifie si l'en-tête contient "Bearer " et extrait le token
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
