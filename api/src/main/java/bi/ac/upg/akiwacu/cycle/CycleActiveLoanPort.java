package bi.ac.upg.akiwacu.cycle;

/**
 * Port consommé par le domaine cycle pour appliquer R4 lors de la clôture.
 * Gloria doit fournir l'adaptateur depuis le domaine pret.
 */
public interface CycleActiveLoanPort {

    boolean existePretActif(Long cycleId, Long tontineId);
}
