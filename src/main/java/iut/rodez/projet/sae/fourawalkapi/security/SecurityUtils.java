package iut.rodez.projet.sae.fourawalkapi.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utilitaire statique facilitant l'accès aux données de l'utilisateur connecté
 * depuis n'importe quel point de l'application (principalement les contrôleurs).
 * Permet d'éviter la duplication des vérifications de sécurité et des casts manuels.
 */
public class SecurityUtils {

    private SecurityUtils(){}
    /**
     * Extraction sécurisée de l'identifiant (ID) de l'utilisateur courant depuis le contexte de sécurité.
     * Cette méthode fait le lien entre l'objet générique Authentication de Spring et le besoin métier (ID Long).
     *
     * @param auth L'objet Authentication injecté automatiquement par Spring Security dans les méthodes du contrôleur.
     * @return L'ID (Long) de l'utilisateur authentifié.
     * @throws ResponseStatusException 401 (UNAUTHORIZED) si l'utilisateur n'est pas correctement connecté.
     * @throws ResponseStatusException 500 (INTERNAL_SERVER_ERROR) si le format du Principal est incorrect (erreur de config).
     */
    public static Long getUserId(Authentication auth) {
        // Validation stricte de l'état de connexion avant toute manipulation
        // Vérifie si l'auth existe, si le flag 'authenticated' est true, et rejette le token "anonymousUser"
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Accès refusé : Utilisateur non connecté");
        }

        try {
            // Tentative de récupération de l'ID stocké dans le Principal
            return (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            // Sécurité défensive : Si la configuration du filtre change (ex: stockage d'un UserDetails au lieu d'un Long)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur : Impossible de récupérer l'identité utilisateur");
        }
    }
}