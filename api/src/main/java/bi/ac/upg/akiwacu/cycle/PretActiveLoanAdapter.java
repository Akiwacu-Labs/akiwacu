package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.pret.Pret;
import bi.ac.upg.akiwacu.pret.StatutPret;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Adaptateur de lecture du domaine prêt vers le contrat attendu par le cycle.
 * Le cycle ne dépend ainsi pas des détails du repository de Gloria.
 */
@Component
public class PretActiveLoanAdapter implements CycleActiveLoanPort {

    private final EntityManager entityManager;

    public PretActiveLoanAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean existePretActif(Long cycleId, Long tontineId) {
        Long nombre = entityManager.createQuery("""
                        select count(p) from Pret p
                        where p.cycle.id = :cycleId
                          and p.cycle.tontine.id = :tontineId
                          and p.statut in :statuts
                        """, Long.class)
                .setParameter("cycleId", cycleId)
                .setParameter("tontineId", tontineId)
                .setParameter("statuts", EnumSet.of(StatutPret.ACTIF, StatutPret.EN_RETARD))
                .getSingleResult();
        return nombre > 0;
    }
}
