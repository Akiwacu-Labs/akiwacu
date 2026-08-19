package bi.ac.upg.akiwacu.common.exception;

/**
 * PROPRIÉTAIRE : Andy.
 * Entité demandée absente (ou hors du périmètre R1 de l'appelant — voir
 * TenantContext) — traduite en 404 par GlobalExceptionHandler.
 */
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
