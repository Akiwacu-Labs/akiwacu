package bi.ac.upg.akiwacu.remboursement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PROPRIÉTAIRE : Klein.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface RemboursementRepository extends JpaRepository<Remboursement, Long> {
}
