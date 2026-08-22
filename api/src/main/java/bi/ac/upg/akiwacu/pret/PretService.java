package bi.ac.upg.akiwacu.pret;

import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * PROPRIÉTAIRE : Gloria.
 * Porte les règles métier liées au montant demandé d'un prêt.
 */
@Service
public class PretService {

    private static final BigDecimal MULTIPLICATEUR_R6 = BigDecimal.valueOf(3);

    private final CotisationRepository cotisationRepository;

    public PretService(CotisationRepository cotisationRepository) {
        this.cotisationRepository = cotisationRepository;
    }

    /**
     * R6 — le capital demandé ne dépasse pas trois fois les cotisations
     * du membre sur le cycle actif. Les prêts en cours ne sont pas déduits.
     */
    @Transactional(readOnly = true)
    public void verifierLimiteMontant(Long membreId, Long cycleId, BigDecimal montantDemande) {
        BigDecimal epargne = cotisationRepository.sommeParMembreEtCycle(membreId, cycleId);
        BigDecimal plafond = epargne.multiply(MULTIPLICATEUR_R6);

        if (montantDemande.compareTo(plafond) > 0) {
            throw new RegleMetierException(
                    "R6 : le montant demandé (%s BIF) dépasse trois fois l'épargne du membre (%s BIF)"
                            .formatted(montantDemande, epargne));
        }
    }
}
