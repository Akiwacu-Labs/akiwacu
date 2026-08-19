package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Garde partagée de la règle R2.
 *
 * <p>Les domaines cotisation, prêt, remboursement et caisse l'appellent
 * avant toute opération financière. Une seule implémentation garantit ainsi
 * le même comportement et le même message d'erreur dans toute l'application.</p>
 */
@Service
@RequiredArgsConstructor
public class CycleGuardService {

    private final CycleRepository cycleRepository;

    /**
     * Vérifie qu'un cycle {@link StatutCycle#OUVERT} existe pour cette tontine.
     *
     * @param tontineId identifiant de la tontine issu du contexte authentifié
     * @return le cycle ouvert à utiliser par l'opération appelante
     * @throws RegleMetierException si aucun cycle actif n'existe — R2 (HTTP 409)
     */
    public Cycle assertCycleActif(Long tontineId) {
        return cycleRepository.findByTontineIdAndStatut(tontineId, StatutCycle.OUVERT)
                .orElseThrow(() -> new RegleMetierException(
                        "R2 : aucun cycle actif pour cette tontine, l'opération est impossible"));
    }
}
