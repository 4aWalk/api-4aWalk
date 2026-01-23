package iut.rodez.projet.sae.fourawalkapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    // Injection de UserRepository pour récupérer l'ID lors de la génération
    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Génère un token JWT à partir de l'objet Authentication.
     */
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();

        // On récupère l'ID de l'utilisateur en base une seule fois à la création du token
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour le token"));

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", user.getId()) // L'ID est injecté ici !
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    /**
     * Récupère l'ID utilisateur (Long) depuis le token.
     */
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    public String getUserIdAsString(String token) {
        Long id = getUserId(token);
        return String.valueOf(id);
    }

    /**
     * Valide la signature et l'expiration du token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            System.err.println("Erreur de validation JWT : " + ex.getMessage());
        }
        return false;
    }
}