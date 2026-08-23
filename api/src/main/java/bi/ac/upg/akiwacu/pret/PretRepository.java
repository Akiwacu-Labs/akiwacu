package bi.ac.upg.akiwacu.pret;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PROPRIÉTAIRE : Gloria.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface PretRepository extends JpaRepository<Pret, Long> {

    Optional<Pret> findByDemandePretId(Long demandePretId);
}
