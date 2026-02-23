package iut.rodez.projet.sae.fourawalkapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour {@link SecurityUtils}.
 * Vérifie le bon fonctionnement de l'extraction de l'ID utilisateur
 * depuis le contexte de sécurité Spring, ainsi que la gestion stricte des erreurs (401, 500).
 */
class SecurityUtilsTest {

    /**
     * Teste le cas nominal : l'utilisateur est correctement authentifié
     * et son ID (Long) est présent dans le Principal.
     */
    @Test
    void getUserId_ValidAuthentication_ShouldReturnId() {
        // GIVEN : Un utilisateur authentifié avec un ID (Long) en tant que Principal
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(42L);

        // WHEN
        Long result = SecurityUtils.getUserId(auth);

        // THEN : On récupère bien l'ID 42
        assertEquals(42L, result);
    }

    /**
     * Teste les cas où l'authentification est absente ou marquée comme non authentifiée.
     * Doit lever une exception 401 UNAUTHORIZED.
     */
    @Test
    void getUserId_NullOrNotAuthenticated_ShouldThrow401() {
        // GIVEN : Authentification nulle
        Authentication nullAuth = null;

        // GIVEN : Authentification présente mais flag "false"
        Authentication notAuth = mock(Authentication.class);
        when(notAuth.isAuthenticated()).thenReturn(false);

        // WHEN & THEN : Rejette en 401 (UNAUTHORIZED)
        assertThrows(ResponseStatusException.class, () -> SecurityUtils.getUserId(nullAuth));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> SecurityUtils.getUserId(notAuth));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    /**
     * Teste le cas spécifique où Spring Security assigne l'utilisateur "anonymousUser"
     * par défaut pour les routes non sécurisées. Doit être bloqué.
     */
    @Test
    void getUserId_AnonymousUser_ShouldThrow401() {
        // GIVEN : L'utilisateur par défaut de Spring quand on n'est pas connecté
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymousUser");

        // WHEN & THEN
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> SecurityUtils.getUserId(auth));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    /**
     * Teste le cas de sécurité défensive où le Principal ne correspond pas au type attendu (Long).
     * Doit lever une exception 500 INTERNAL_SERVER_ERROR pour alerter d'un défaut de configuration.
     */
    @Test
    void getUserId_WrongPrincipalType_ShouldThrow500() {
        // GIVEN : Un Principal qui n'est pas un Long (Erreur de configuration du dev)
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("JeSuisUneStringPasUnLong");

        // WHEN & THEN : Intercepté par le catch (ClassCastException) -> 500
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> SecurityUtils.getUserId(auth));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }
}