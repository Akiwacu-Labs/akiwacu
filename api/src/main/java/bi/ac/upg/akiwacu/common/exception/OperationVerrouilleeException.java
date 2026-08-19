package bi.ac.upg.akiwacu.common.exception;

/**
 * PROPRIÉTAIRE : Andy.
 * Tentative de modification/suppression d'une opération déjà couverte par un
 * reçu (R8) — traduite en 409 par GlobalExceptionHandler.
 */
public class OperationVerrouilleeException extends RuntimeException {

    public OperationVerrouilleeException(String message) {
        super(message);
    }
}
