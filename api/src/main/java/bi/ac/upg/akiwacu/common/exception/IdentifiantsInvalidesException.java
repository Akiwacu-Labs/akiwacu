package bi.ac.upg.akiwacu.common.exception;

/**
 * PROPRIÉTAIRE : Andy.
 * Email inconnu, mot de passe incorrect ou compte désactivé — traduite en 401
 * par GlobalExceptionHandler. Un seul message générique côté client : on ne
 * révèle jamais si c'est l'email ou le mot de passe qui est en cause.
 */
public class IdentifiantsInvalidesException extends RuntimeException {

    public IdentifiantsInvalidesException(String message) {
        super(message);
    }
}
