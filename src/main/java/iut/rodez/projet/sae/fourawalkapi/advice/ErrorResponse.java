package iut.rodez.projet.sae.fourawalkapi.advice;

/**
 * Objet d'erreur utilisé pour les retours clients
 */
public class ErrorResponse {
    private int status; // code erreur
    private String message; // message associé

    // Contructeur d'une erreur réponse
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
}
