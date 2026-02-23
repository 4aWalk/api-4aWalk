package iut.rodez.projet.sae.fourawalkapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de la configuration de sécurité (SecurityConfig).
 * Utilise le contexte Spring Boot complet pour éviter les erreurs de beans manquants.
 * Nécessite de lancer le docker en local pour faire fonctionner la sécurité
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Teste que l'endpoint d'inscription est accessible sans authentification.
     * @throws Exception Si la requête HTTP simulée échoue
     */
    @Test
    void register_IsPublic_ShouldPassSecurity() throws Exception {
        mockMvc.perform(post("/users/register"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    // On vérifie que la sécurité ne bloque pas (Pas de 401 ou 403)
                    assertNotEquals(401, statusCode, "L'accès ne doit pas être Unauthorized (401)");
                    assertNotEquals(403, statusCode, "L'accès ne doit pas être Forbidden (403)");
                });
    }

    /**
     * Teste que l'endpoint de connexion (login) est accessible publiquement.
     * @throws Exception Si la requête HTTP simulée échoue
     */
    @Test
    void login_IsPublic_ShouldPassSecurity() throws Exception {
        mockMvc.perform(post("/users/login"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assertNotEquals(401, statusCode, "L'accès ne doit pas être Unauthorized (401)");
                    assertNotEquals(403, statusCode, "L'accès ne doit pas être Forbidden (403)");
                });
    }

    /**
     * Teste que n'importe quelle autre requête est bloquée (403 Forbidden)
     * si aucun jeton d'authentification n'est fourni.
     * @throws Exception Si la requête HTTP simulée échoue
     */
    @Test
    void anyOtherRequest_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/courses/my"))
                .andExpect(status().isForbidden());
    }

    /**
     * Vérifie que le PasswordEncoder instancié par la configuration
     * est bien du type BCryptPasswordEncoder.
     */
    @Test
    void passwordEncoder_ShouldBeBCrypt() {
        SecurityConfig config = new SecurityConfig(null);
        assertTrue(config.passwordEncoder() instanceof BCryptPasswordEncoder,
                "Le bean PasswordEncoder doit être une instance de BCryptPasswordEncoder");
    }
}