package iut.rodez.projet.sae.fourawalkapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Clé secrète stockée dans application.properties (CRITIQUE pour la sécurité)
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // Durée de validité du token (en millisecondes, ex: 1 jour)
    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // Obtient la clé de signature à partir du secret encodé en base64
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Génère un token JWT après une authentification réussie.
     * @param authentication L'objet d'authentification de Spring Security.
     * @return Le token JWT généré.
     */
    public String generateToken(Authentication authentication){
        String username = authentication.getName(); // C'est l'email

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        // Construction du token
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key()) // Signature avec la clé secrète
                .compact();
        return token;
    }

    /**
     * Récupère l'email (Subject) à partir du token.
     */
    public String getUsername(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Valide le token JWT.
     * @param token Le token à valider.
     * @return true si le token est valide, false sinon.
     */
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token); // Tente de parser et de vérifier la signature et l'expiration
            return true;
        } catch (MalformedJwtException ex) {
            System.err.println("Token JWT malformé.");
        } catch (ExpiredJwtException ex) {
            System.err.println("Token JWT expiré.");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Token JWT non supporté.");
        } catch (IllegalArgumentException ex) {
            System.err.println("La chaîne de claims JWT est vide.");
        }
        return false;
    }
}