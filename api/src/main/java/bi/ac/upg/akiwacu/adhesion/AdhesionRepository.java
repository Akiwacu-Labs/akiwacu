package bi.ac.upg.akiwacu.adhesion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PROPRIÉTAIRE : Juste.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface AdhesionRepository extends JpaRepository<Adhesion, Long> {
}
