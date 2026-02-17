package iut.rodez.projet.sae.fourawalkapi.security;

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
import java.util.Collections;

/**
 * Composant de vérification filtre du token d'authentification
 * Intercepte chaque requête HTTP pour vérifier la présence et la validité d'un JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /* Récupérateur de token */
    private final JwtTokenProvider tokenProvider;

    /**
     * Injection de dépendance du récupérateur de jeton
     * @param tokenProvider Recupérateur de token d'authentification
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Méthode principale exécutée automatiquement pour chaque requête entrante stateless
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Tentative d'extraction de la chaîne du token depuis le header Authorization
            String token = getJwtFromRequest(request);

            // Vérifie si le token existe et s'il est cryptographiquement valide (signature + expiration)
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {

                // Extraction de l'ID utilisateur contenu dans le payload du token
                // Cela évite de faire une requête SQL en base de données pour retrouver l'user
                Long userId = tokenProvider.getUserId(token);

                // Création de l'objet d'authentification interne à Spring Security
                // On passe l'ID (userId) en tant que 'Principal' (identité)
                // Le mot de passe (credentials) est null car déjà validé par le token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList() // Liste des rôles (vide pour l'instant)
                );

                // Ajout des détails techniques de la requête
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Enregistrement de l'objet d'authentification dans le contexte de sécurité du Thread actuel
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // En cas d'erreur (token invalide, malformé)
            logger.error("Impossible de définir l'authentification utilisateur", ex);
        }

        // Transmission de la requête au maillon suivant de la chaîne (autre filtre ou contrôleur)
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT pur depuis le header HTTP.
     * Le format standard est "Bearer <token>".
     * @param request requête contenant le token à extraire
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Vérifie si le header n'est pas vide et commence bien par le préfixe standard
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Retourne la chaîne de caractères après "Bearer"
            return bearerToken.substring(7);
        }
        return null;
    }
}