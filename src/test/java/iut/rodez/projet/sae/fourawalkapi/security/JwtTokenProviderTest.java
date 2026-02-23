package iut.rodez.projet.sae.fourawalkapi.security;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour {@link JwtTokenProvider}.
 * Valide la création (signature, injection des claims) et la vérification
 * (expiration, intégrité cryptographique) des jetons JWT.
 * Utilise ReflectionTestUtils pour simuler les variables d'environnement.
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;

    /* Clé de test encodée en Base64 (Doit faire au moins 256 bits pour l'algorithme HS256) */
    private final String testSecret = "Y2VjaWVzdHVuZWNsZXNlY3JldGV0cmVzbG9uZ3VlcG91cmxlc3Rlc3RzamF2YQ==";

    @BeforeEach
    void setUp() {
        // Injection manuelle des @Value pour éviter de charger le contexte Spring complet
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", 3600000L); // 1 heure

        testUser = new User();
        testUser.setId(99L);
        testUser.setMail("test@test.com");
    }

    /**
     * Teste la génération d'un token à partir d'une entité User.
     * Vérifie que le token est non nul, valide et contient bien l'ID de l'utilisateur.
     */
    @Test
    void generateToken_WithUser_ShouldCreateValidToken() {
        // WHEN : On génère le token
        String token = jwtTokenProvider.generateToken(testUser);

        // THEN : Le token n'est pas nul et peut être validé
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(99L, jwtTokenProvider.getUserId(token));
    }

    /**
     * Teste la génération d'un token directement depuis l'objet d'authentification Spring.
     * Simule l'appel à la base de données pour récupérer l'utilisateur complet.
     */
    @Test
    void generateToken_WithAuthentication_ShouldCreateValidToken() {
        // GIVEN : Un objet Authentication Spring
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@test.com");
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(testUser));

        // WHEN
        String token = jwtTokenProvider.generateToken(auth);

        // THEN
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    /**
     * Teste la robustesse de la validation JWT.
     * Vérifie qu'un token signé avec une clé secrète différente est rejeté.
     */
    @Test
    void validateToken_WithInvalidSignature_ShouldReturnFalse() {
        // GIVEN : Un token généré avec une AUTRE clé secrète
        JwtTokenProvider badProvider = new JwtTokenProvider(userRepository);
        ReflectionTestUtils.setField(badProvider, "jwtSecret", "dW5lYXV0cmVjbGV0b3V0YXVzc2lsb25ndWVwb3VydGVzdGVybGVycmV1cg==");
        ReflectionTestUtils.setField(badProvider, "jwtExpirationDate", 3600000L);

        String badToken = badProvider.generateToken(testUser);

        // WHEN : On le valide avec notre provider (qui a la bonne clé)
        boolean isValid = jwtTokenProvider.validateToken(badToken);

        // THEN : Rejeté (Signature invalide)
        assertFalse(isValid);
    }

    /**
     * Vérifie qu'une chaîne de caractères qui ne respecte pas le format JWT
     * est correctement interceptée et rejetée.
     */
    @Test
    void validateToken_MalformedToken_ShouldReturnFalse() {
        // WHEN & THEN : Une simple string n'est pas un JWT
        assertFalse(jwtTokenProvider.validateToken("CeciNestPasUnToken"));
    }
}