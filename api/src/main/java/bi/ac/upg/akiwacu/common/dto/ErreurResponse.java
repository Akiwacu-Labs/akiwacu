package bi.ac.upg.akiwacu.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * PROPRIÉTAIRE : Andy.
 * Corps de réponse JSON uniforme pour toute erreur renvoyée par l'API.
 * `details` ne porte que les erreurs de validation champ par champ (400) ;
 * il est vide sinon.
 */
public record ErreurResponse(
        Instant horodatage,
        int statut,
        String erreur,
        String message,
        String chemin,
        List<String> details) {

    public static ErreurResponse de(int statut, String erreur, String message, String chemin) {
        return new ErreurResponse(Instant.now(), statut, erreur, message, chemin, List.of());
    }

    public static ErreurResponse deValidation(String message, String chemin, List<String> details) {
        return new ErreurResponse(Instant.now(), 400, "Validation échouée", message, chemin, details);
    }
}
