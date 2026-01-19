package iut.rodez.projet.sae.fourawalkapi.advice;

/**
 * DTO standard pour les réponses d'erreur JSON.
 */
public class ErrorResponse {
    private int status;
    private String message;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters nécessaires pour la sérialisation JSON
    public int getStatus() { return status; }
    public String getMessage() { return message; }
}
