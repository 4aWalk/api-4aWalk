package iut.rodez.projet.sae.fourawalkapi.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Classe centrale pour la gestion des exceptions à travers tous les contrôleurs de l'API.
 * Elle permet de standardiser les réponses d'erreur (format JSON et statut HTTP).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les exceptions de type IllegalArgumentException (lancées pour les erreurs de validation métier).
     * Mappe l'erreur à un statut HTTP 400 (BAD_REQUEST).
     * @param ex L'exception lancée par le Service.
     * @return Un objet simple contenant le message d'erreur.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        // Loggez l'erreur ici si nécessaire
        System.err.println("Validation échouée : " + ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Gère les exceptions génériques (un problème inattendu).
     * Mappe l'erreur à un statut HTTP 500 (INTERNAL_SERVER_ERROR).
     * @param ex L'exception.
     * @return Un objet simple pour le message 500.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        System.err.println("Erreur Interne non gérée : " + ex.getMessage());
        // En production, il est bon de masquer les détails de l'erreur interne
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne du serveur est survenue. Veuillez réessayer plus tard."
        );
    }

    // TODO: Ajoutez ici des gestionnaires pour d'autres exceptions courantes (ex: EntityNotFoundException -> 404)
}
