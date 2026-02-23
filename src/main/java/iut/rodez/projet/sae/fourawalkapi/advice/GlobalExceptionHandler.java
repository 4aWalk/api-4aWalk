package iut.rodez.projet.sae.fourawalkapi.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les erreurs de validation déclenchées par @Valid (Bean Validation).
     * Renvoie une Map avec le nom du champ en clé et le message d'erreur en valeur.
     * Exemple de retour : { "age": "L'âge est obligatoire", "nom": "Ne peut pas être vide" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
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
        logger.error("Erreur argument illégal : " + ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Gère les RuntimeException génériques (ex: "Utilisateur introuvable" lancé par .orElseThrow()).
     * On mappe le code retour avec 400
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeException(RuntimeException ex) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Sécurité permettant de gérer les Exception générale
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        //ex.printStackTrace(); // Important pour voir l'erreur dans la console serveur
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),"Une erreur est interne est survenu");

    }
}