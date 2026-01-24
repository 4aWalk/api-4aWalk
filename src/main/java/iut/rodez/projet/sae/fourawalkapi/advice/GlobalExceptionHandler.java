package iut.rodez.projet.sae.fourawalkapi.advice;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe centrale pour la gestion des exceptions à travers tous les contrôleurs de l'API.
 * Elle permet de standardiser les réponses d'erreur (format JSON et statut HTTP).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs de validation déclenchées par @Valid (Bean Validation).
     * Renvoie une Map avec le nom du champ en clé et le message d'erreur en valeur.
     * Exemple de retour : { "age": "L'âge est obligatoire", "nom": "Ne peut pas être vide" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // On cast en FieldError pour récupérer le nom du champ spécifique
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * Gère les exceptions de type IllegalArgumentException (lancées manuellement dans les Services).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        System.err.println("Erreur argument illégal : " + ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Gère tes exceptions métier personnalisées (ex: HikeException).
     * Si tu n'as pas créé de classe spécifique, tu peux garder IllegalArgumentException.
     */
    @ExceptionHandler(HikeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHikeException(HikeException ex) {
        System.err.println("Erreur métier Hike : " + ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Gère les RuntimeException génériques (ex: "Utilisateur introuvable" lancé par .orElseThrow()).
     * On le mappe souvent en 400 ou 404 selon la logique, ici 400 pour simplifier.
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeException(RuntimeException ex) {
        // Utile pour tes .orElseThrow(() -> new RuntimeException("..."))
        System.err.println("Erreur Runtime : " + ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Filet de sécurité global pour les bugs imprévus (NullPointerException, SQL error, etc.).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        ex.printStackTrace(); // Important pour voir l'erreur dans la console serveur
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne du serveur est survenue. " + ex.getMessage() // En prod, masque le message
        );
    }
}