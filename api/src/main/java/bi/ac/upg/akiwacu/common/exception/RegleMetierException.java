package bi.ac.upg.akiwacu.common.exception;

/**
 * PROPRIÉTAIRE : Andy.
 * Violation d'une règle métier (R2 à R7 selon le domaine appelant) — traduite
 * en 409 par GlobalExceptionHandler. Le message doit citer la règle, ex.
 * "R6 — le montant demandé dépasse 3 fois l'épargne du membre".
 */
public class RegleMetierException extends RuntimeException {

    public RegleMetierException(String message) {
        super(message);
    }
}
