package iut.rodez.projet.sae.fourawalkapi.advice;

import iut.rodez.projet.sae.fourawalkapi.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour l'API FourAWalk.
 * Centralise le mapping entre les erreurs Java/Métier et les codes de statut HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================
    // 1. ERREURS DE VALIDATION (FORMULAIRES / DTO)
    // =========================================================================

    /**
     * Gère les erreurs de validation @Valid.
     * @return Map des champs invalides avec leurs messages respectifs (HTTP 400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Échec de validation des données : {} erreurs trouvées.", errors.size());
        return errors;
    }

    /**
     * Gère les erreurs de logique métier lors de la saisie (ex: âge hors limites).
     */
    @ExceptionHandler(BusinessValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusinessValidation(BusinessValidationException ex) {
        logger.warn("Règle métier non respectée : {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    // =========================================================================
    // 2. GESTION DES RESSOURCES (SQL / NOSQL)
    // =========================================================================

    /**
     * Gère l'absence d'une ressource en base de données (HTTP 404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Ressource non trouvée : {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    /**
     * Gère les tentatives de création de doublons (ex: email déjà pris) (HTTP 409).
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        logger.warn("Conflit de données : {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    // =========================================================================
    // 3. SÉCURITÉ ET DROITS D'ACCÈS
    // =========================================================================

    /**
     * Gère les accès refusés (propriétaire différent, etc.) (HTTP 403).
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        logger.error("Tentative d'accès non autorisé : {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    // =========================================================================
    // 4. LOGIQUE MÉTIER ET ÉTAT DE L'APPLICATION
    // =========================================================================

    /**
     * Gère les actions impossibles dans l'état actuel (ex: supprimer créateur) (HTTP 422).
     */
    @ExceptionHandler(IllegalBusinessActionException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleIllegalAction(IllegalBusinessActionException ex) {
        logger.error("Action métier invalide : {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
    }

    /**
     * Gère spécifiquement les erreurs de capacité de sac à dos (HTTP 422).
     */
    @ExceptionHandler(CapacityExceededException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleCapacityExceeded(CapacityExceededException ex) {
        logger.warn("Capacité de backpack dépassée : {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
    }

    // =========================================================================
    // 5. ERREURS TECHNIQUES ET FALLBACK
    // =========================================================================

    /**
     * Fallback pour les arguments illégaux génériques.
     */
    @ExceptionHandler({IllegalArgumentException.class, AuthenticationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Argument illégal intercepté : {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Capturer toutes les autres exceptions non gérées (Erreur 500).
     * On masque les détails techniques à l'utilisateur pour la sécurité.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        logger.error("ERREUR CRITIQUE INTERNE : ", ex);
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne est survenue. Merci de contacter l'équipe de développeur."
        );
    }
}