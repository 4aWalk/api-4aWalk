package iut.rodez.projet.sae.fourawalkapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Composant utilitaire responsable de la gestion des JSON Web Tokens (JWT).
 * Il gère la génération (création), la validation (signature/expiration) et l'extraction d'informations (parsing).
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    /* Clé secrète utilisée pour signer les tokens (définie dans application.properties) */
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    /* Durée de validité du token en millisecondes */
    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Décode la clé secrète encodée en Base64 pour l'algorithme de signature HMAC-SHA.
     * @return L'objet Key cryptographique.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Génère un token JWT complet pour un utilisateur spécifique.
     * Cette méthode construit le "payload" du token avec l'email (subject) et l'ID (claim personnalisé).
     *
     * @param user L'entité utilisateur pour laquelle on crée le token.
     * @return La chaîne de caractères du token JWT signé.
     */
    public String generateToken(User user) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(user.getMail())        // Identifiant principal (Email)
                .claim("userId", user.getId())     // Stockage de l'ID pour récupération rapide sans BDD
                .setIssuedAt(new Date())           // Date de création
                .setExpiration(expireDate)         // Date d'expiration
                .signWith(key(), SignatureAlgorithm.HS256) // Signature cryptographique
                .compact();
    }

    /**
     * Surcharge pour générer un token directement depuis l'objet d'authentification Spring Security.
     * Utile lors de la phase de login.
     *
     * @param authentication L'objet contenant les détails de l'utilisateur connecté.
     * @return Le token JWT signé.
     */
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();

        // Récupération de l'entité complète pour accéder à l'ID
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable lors de la génération du token"));

        return generateToken(user);
    }

    /**
     * Extrait l'identifiant (ID) de l'utilisateur stocké dans le token (Claim "userId").
     * Cette méthode est critique pour le filtre d'authentification.
     *
     * @param token Le token JWT à décoder.
     * @return L'ID de l'utilisateur (Long).
     */
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token) // Vérifie la signature et parse le token
                .getBody();

        // Extraction sûre grâce au typage
        return claims.get("userId", Long.class);
    }

    /**
     * Vérifie l'intégrité et la validité du token JWT.
     * Contrôle la signature, l'expiration et le format.
     *
     * @param token Le token JWT à valider.
     * @return true si le token est valide, false sinon.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT invalide : " + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expiré : " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La chaîne claims JWT est vide : " + e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Signature JWT invalide : " + e.getMessage());
        }
        return false;
    }
}