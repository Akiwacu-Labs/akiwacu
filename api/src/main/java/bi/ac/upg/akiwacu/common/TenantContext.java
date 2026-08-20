package bi.ac.upg.akiwacu.common;

/**
 * PROPRIÉTAIRE : Andy.
 * Base de la règle R1 (voir docs/DECISIONS.md D-20). Porte le tontineId du
 * jeton JWT courant, un par thread de requête HTTP. Rempli par
 * auth.JwtAuthenticationFilter, jamais par le corps d'une requête ni par un
 * @PathVariable non vérifié.
 *
 * clear() est appelé dans un `finally` par le filtre : Tomcat réutilise ses
 * threads d'une requête à l'autre, donc un ThreadLocal non nettoyé fuiterait
 * le tontineId d'un utilisateur vers la requête suivante traitée par le même
 * thread — exactement le genre de fuite que R1 doit empêcher.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> TONTINE_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTontineId(Long tontineId) {
        TONTINE_ID.set(tontineId);
    }

    /**
     * @throws IllegalStateException si aucun tontineId n'a été posé pour ce thread —
     *         un service qui appelle ceci en dehors d'une requête authentifiée
     *         a un bug, pas un tontineId à null à propager plus loin.
     */
    public static Long getTontineId() {
        Long tontineId = TONTINE_ID.get();
        if (tontineId == null) {
            throw new IllegalStateException(
                    "TenantContext.getTontineId() appelé hors d'une requête authentifiée");
        }
        return tontineId;
    }

    public static void clear() {
        TONTINE_ID.remove();
    }

    /**
     * true si un tontineId est posé pour ce thread. Réservé à
     * TenantFilterAspect : certaines transactions sont légitimement
     * antérieures à l'authentification (ex. AuthService.login()) et ne
     * doivent pas déclencher l'exception de getTontineId().
     */
    public static boolean estDefini() {
        return TONTINE_ID.get() != null;
    }
}
