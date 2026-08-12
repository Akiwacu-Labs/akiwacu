package bi.ac.upg.akiwacu.membre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PROPRIÉTAIRE : Andy.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface MembreRepository extends JpaRepository<Membre, Long> {
}
