package bi.ac.upg.akiwacu.common;

import bi.ac.upg.akiwacu.common.dto.ErreurResponse;
import bi.ac.upg.akiwacu.common.exception.IdentifiantsInvalidesException;
import bi.ac.upg.akiwacu.common.exception.OperationVerrouilleeException;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * PROPRIÉTAIRE : Andy.
 * Traduit les exceptions métier en réponses HTTP. Chaque règle R1-R8 lève une
 * exception typée depuis la couche service ; c'est ici, et seulement ici,
 * qu'elle devient un code HTTP. Voir CLAUDE.md pour la table des codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> gererValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest requete) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(erreur -> erreur.getField() + " : " + erreur.getDefaultMessage())
                .toList();
        var corps = ErreurResponse.deValidation("Un ou plusieurs champs sont invalides",
                requete.getRequestURI(), details);
        return ResponseEntity.badRequest().body(corps);
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<ErreurResponse> gererIdentifiantsInvalides(IdentifiantsInvalidesException ex,
                                                                       HttpServletRequest requete) {
        return construire(HttpStatus.UNAUTHORIZED, ex.getMessage(), requete);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErreurResponse> gererAuthentification(AuthenticationException ex,
                                                                   HttpServletRequest requete) {
        return construire(HttpStatus.UNAUTHORIZED, "Authentification requise", requete);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErreurResponse> gererAccesRefuse(AccessDeniedException ex,
                                                              HttpServletRequest requete) {
        return construire(HttpStatus.FORBIDDEN, "Rôle insuffisant pour cette opération", requete);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> gererRessourceIntrouvable(RessourceIntrouvableException ex,
                                                                       HttpServletRequest requete) {
        return construire(HttpStatus.NOT_FOUND, ex.getMessage(), requete);
    }

    @ExceptionHandler({RegleMetierException.class, OperationVerrouilleeException.class})
    public ResponseEntity<ErreurResponse> gererConflitMetier(RuntimeException ex,
                                                                HttpServletRequest requete) {
        return construire(HttpStatus.CONFLICT, ex.getMessage(), requete);
    }

    private ResponseEntity<ErreurResponse> construire(HttpStatus statut, String message,
                                                         HttpServletRequest requete) {
        var corps = ErreurResponse.de(statut.value(), statut.getReasonPhrase(), message,
                requete.getRequestURI());
        return ResponseEntity.status(statut).body(corps);
    }
}
