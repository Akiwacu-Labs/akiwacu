package bi.ac.upg.akiwacu.caisse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PROPRIÉTAIRE : Klein.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface TransactionCaisseRepository extends JpaRepository<TransactionCaisse, Long> {
	List<TransactionCaisse> findByCycleId(Long cycleId);
}
