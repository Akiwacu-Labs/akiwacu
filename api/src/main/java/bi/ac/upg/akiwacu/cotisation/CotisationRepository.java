package bi.ac.upg.akiwacu.cotisation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PROPRIÉTAIRE : Benitha.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, Long> {
}
