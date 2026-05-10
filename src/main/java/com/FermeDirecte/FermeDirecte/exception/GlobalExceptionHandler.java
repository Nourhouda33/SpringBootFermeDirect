package com.FermeDirecte.FermeDirecte.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -------------------------------------------------------
    // Standard error response body
    // -------------------------------------------------------
    @Data
    @AllArgsConstructor
    public static class ApiError {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
    }

    @Data
    @AllArgsConstructor
    public static class ValidationApiError {
        private int status;
        private String error;
        private Map<String, String> fieldErrors;
        private LocalDateTime timestamp;
    }

    // -------------------------------------------------------
    // Handlers
    // -------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Ressource non trouvée: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError(404, "Ressource non trouvée", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        log.warn("Requête invalide: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(400, "Requête invalide", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Accès refusé: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiError(403, "Accès refusé", 
                "Vous n'avez pas les permissions nécessaires pour effectuer cette action.", 
                LocalDateTime.now()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentative de connexion échouée");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiError(401, "Authentification échouée", 
                "Email ou mot de passe incorrect. Veuillez vérifier vos identifiants.", 
                LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        log.warn("Erreur de validation: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ValidationApiError(400, "Données invalides", errors, LocalDateTime.now()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex) {
        log.warn("Ressource dupliquée: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiError(409, "Ressource déjà existante", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex) {
        
        // Log complet pour debug
        log.error("=== DataIntegrityViolationException ===");
        log.error("Message: {}", ex.getMessage());
        if (ex.getCause() != null) log.error("Cause: {}", ex.getCause().getMessage());
        if (ex.getMostSpecificCause() != null) log.error("Root cause: {}", ex.getMostSpecificCause().getMessage());

        String rootMsg = ex.getMostSpecificCause() != null 
            ? ex.getMostSpecificCause().getMessage() 
            : ex.getMessage();

        String userMessage;

        if (rootMsg != null && rootMsg.contains("Data too long")) {
            userMessage = "L'image est trop grande. Utilisez une URL d'image au lieu d'une image en base64.";
        } else if (rootMsg != null && rootMsg.contains("Duplicate entry")) {
            if (rootMsg.contains("email")) {
                userMessage = "Cet email est déjà utilisé.";
            } else if (rootMsg.contains("categories")) {
                userMessage = "Une catégorie avec ce nom existe déjà.";
            } else if (rootMsg.contains("code")) {
                userMessage = "Ce code existe déjà.";
            } else {
                // Extraire le nom du champ dupliqué depuis le message MySQL
                userMessage = "Doublon détecté : " + rootMsg;
            }
        } else if (rootMsg != null && rootMsg.contains("cannot be null")) {
            userMessage = "Champ obligatoire manquant : " + rootMsg;
        } else if (rootMsg != null && rootMsg.contains("foreign key")) {
            userMessage = "Référence invalide (clé étrangère).";
        } else {
            userMessage = "Erreur base de données : " + rootMsg;
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiError(409, "Erreur de données", userMessage, LocalDateTime.now()));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex) {
        log.warn("Erreur d'authentification: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiError(401, "Authentification requise", 
                "Votre session a expiré ou vos identifiants sont invalides. Veuillez vous reconnecter.", 
                LocalDateTime.now()));
    }

    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<ApiError> handleJwtException(io.jsonwebtoken.JwtException ex) {
        log.warn("Erreur JWT: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiError(401, "Token invalide", 
                "Votre session a expiré. Veuillez vous reconnecter.", 
                LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, WebRequest request) {
        // Log l'erreur complète pour le débogage
        log.error("Erreur inattendue - Type: {}, Message: {}", ex.getClass().getName(), ex.getMessage(), ex);
        
        // Message utilisateur générique mais informatif
        String userMessage = "Une erreur inattendue s'est produite. Veuillez réessayer.";
        
        // Détecter certaines erreurs courantes et fournir des messages plus clairs
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Connection refused") || 
                ex.getMessage().contains("Unable to connect")) {
                userMessage = "Impossible de se connecter à la base de données. Veuillez contacter l'administrateur.";
            } else if (ex.getMessage().contains("Timeout")) {
                userMessage = "La requête a pris trop de temps. Veuillez réessayer.";
            } else if (ex.getMessage().contains("JWT")) {
                userMessage = "Erreur d'authentification. Veuillez vous reconnecter.";
            } else if (ex.getMessage().contains("transient")) {
                userMessage = "Erreur de sauvegarde des données. Veuillez réessayer.";
            }
        }
        
        // En mode développement, inclure le message d'erreur technique
        String detailedMessage = userMessage;
        if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            detailedMessage = userMessage + " (Détail: " + ex.getMessage() + ")";
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(500, "Erreur serveur", detailedMessage, LocalDateTime.now()));
    }
}
