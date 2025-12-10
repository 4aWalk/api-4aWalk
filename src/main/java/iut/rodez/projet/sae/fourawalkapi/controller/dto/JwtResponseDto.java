package iut.rodez.projet.sae.fourawalkapi.controller.dto;

public class JwtResponseDto {
    private String token;
    private String tokenType = "Bearer";
    private UserResponseDto user; // Le DTO d'informations publiques
}
