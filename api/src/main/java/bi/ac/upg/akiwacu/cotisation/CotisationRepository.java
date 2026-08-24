package bi.ac.upg.akiwacu.cotisation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * PROPRIÉTAIRE : Benitha.
 * Les lectures exposées ici restent tenant-scopées : le tontineId est un
 * paramètre obligatoire, conformément à R1.
 */
@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, Long> {

    List<Cotisation> findAllByMembreTontineId(Long tontineId);

    Optional<Cotisation> findByIdAndMembreTontineId(Long id, Long tontineId);

    /**
     * D-27 / R6 — total des cotisations d'un membre sur un cycle donné.
     *
     * Le total est limité à la tontine courante via TenantContext. Aucun
     * appelant ne peut donc fournir un tontineId arbitraire.
     * Une absence de cotisation vaut zéro afin de simplifier le calcul de R6.
     */
    @Query("""
            SELECT COALESCE(SUM(c.montant), 0)
            FROM Cotisation c
            WHERE c.membre.id = :membreId
              AND c.cycle.id = :cycleId
              AND c.membre.tontine.id =
                  ?#{T(bi.ac.upg.akiwacu.common.TenantContext).getTontineId()}
              AND c.cycle.tontine.id =
                  ?#{T(bi.ac.upg.akiwacu.common.TenantContext).getTontineId()}
            """)
    BigDecimal sommeParMembreEtCycle(@Param("membreId") Long membreId,
                                     @Param("cycleId") Long cycleId);
}
