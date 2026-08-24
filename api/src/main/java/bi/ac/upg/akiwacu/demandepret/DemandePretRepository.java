package bi.ac.upg.akiwacu.demandepret;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PROPRIÉTAIRE : Gloria.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface DemandePretRepository extends JpaRepository<DemandePret, Long> {

    List<DemandePret> findByMembreTontineId(Long tontineId);
}
