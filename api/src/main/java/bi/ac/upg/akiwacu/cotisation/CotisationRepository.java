package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.common.TenantContext;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * PROPRIÉTAIRE : Benitha.
 * Les lectures exposées ici restent tenant-scopées : le tontineId est un
 * paramètre obligatoire, conformément à R1.
 */
@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, Long> {

    /**
     * D-27 / R6 — total des cotisations d'un membre sur un cycle donné.
     *
     * Le total est limité à la tontine courante pour éviter qu'une requête
     * construite avec des identifiants étrangers ne traverse le tenant.
     * Une absence de cotisation vaut zéro afin de simplifier le calcul de R6.
     */
    default BigDecimal sommeParMembreEtCycle(Long membreId, Long cycleId) {
        return sommeParMembreEtCycle(membreId, cycleId, TenantContext.getTontineId());
    }

    @Query("""
            SELECT COALESCE(SUM(c.montant), 0)
            FROM Cotisation c
            WHERE c.membre.id = :membreId
              AND c.cycle.id = :cycleId
              AND c.membre.tontine.id = :tontineId
            """)
    BigDecimal sommeParMembreEtCycle(@Param("membreId") Long membreId,
                                     @Param("cycleId") Long cycleId,
                                     @Param("tontineId") Long tontineId);
}
