package iut.rodez.projet.sae.fourawalkapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Génère un token JWT
     */
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();

        // On récupère l'user pour avoir son ID
        // (C'est la seule fois où on tape la BDD pour l'auth, donc c'est OK)
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable lors de la génération du token"));

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", user.getId()) // ID stocké dans le token
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256) // Précision de l'algo recommandée
                .compact();
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Récupération sécurisée du Long
        return claims.get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Token JWT invalide : " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("Token JWT expiré : " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Token JWT non supporté : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("La chaîne claims JWT est vide : " + e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.err.println("Signature JWT invalide : " + e.getMessage());
        }

        return false;
    }
}