package iut.rodez.projet.sae.fourawalkapi.dto;

/**
 * DTO de réponse renvoyé après une connexion réussie.
 * Combine le JSON Web Token (JWT) et les informations publiques de l'utilisateur.
 */
public class JwtResponseDto {

    private String token;
    private final String tokenType = "Bearer"; // Type de token (constant)
    private UserResponseDto user; // Détails publics de l'utilisateur

    /**
     * Constructeur pour une réponse de connexion réussie.
     * @param token Le JWT généré.
     * @param user Le DTO contenant les informations publiques de l'utilisateur.
     */
    public JwtResponseDto(String token, UserResponseDto user) {
        this.token = token;
        this.user = user;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public UserResponseDto getUser() {
        return user;
    }

    // Setters (optionnels, mais souvent inclus pour la compatibilité Jackson)
    public void setToken(String token) {
        this.token = token;
    }

    // Le setter pour tokenType n'est pas nécessaire car il est final

    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}
