package iut.rodez.projet.sae.fourawalkapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour le filtre web {@link JwtAuthenticationFilter}.
 * Simule des requêtes HTTP pour s'assurer que le filtre intercepte les requêtes,
 * extrait correctement le Bearer token et peuple le SecurityContext de Spring.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // Important : S'assurer que le contexte de sécurité est vierge avant chaque test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Nettoyage après test pour ne pas polluer les autres tests exécutés par le runner
        SecurityContextHolder.clearContext();
    }

    /**
     * Teste le flux nominal : la requête contient un token valide dans l'en-tête Authorization.
     * Le contexte de sécurité doit être mis à jour avec l'ID de l'utilisateur.
     */
    @Test
    void doFilterInternal_ValidToken_ShouldSetSecurityContext() throws ServletException, IOException {
        // GIVEN : Une requête avec un bon header Authorization
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserId(token)).thenReturn(42L);

        // WHEN : La requête passe dans le filtre
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // THEN : Le SecurityContext a été rempli avec l'ID 42
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(42L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // Vérifie que la requête a bien continué son chemin vers les autres filtres/contrôleurs
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Teste le comportement face à un token invalide ou falsifié.
     * Le contexte de sécurité doit rester vide, mais la requête doit continuer (pour être bloquée plus tard en 401).
     */
    @Test
    void doFilterInternal_InvalidToken_ShouldNotSetContext() throws ServletException, IOException {
        // GIVEN : Une requête avec un token refusé par le provider
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(false);

        // WHEN
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // THEN : Le SecurityContext reste vide
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Teste le comportement lorsqu'aucun token n'est fourni (ex: accès à une route publique).
     * Le filtre doit laisser passer la requête sans modifier le contexte de sécurité.
     */
    @Test
    void doFilterInternal_NoToken_ShouldNotSetContext() throws ServletException, IOException {
        // GIVEN : Une requête SANS header Authorization
        // L'objet MockHttpServletRequest est vide par défaut

        // WHEN
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // THEN : Le SecurityContext reste vide
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}