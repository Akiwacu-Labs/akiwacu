package bi.ac.upg.akiwacu.adhesion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PROPRIÉTAIRE : Juste.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface AdhesionRepository extends JpaRepository<Adhesion, Long> {

    List<Adhesion> findAllByMembreTontineId(Long tontineId);

    Optional<Adhesion> findByIdAndMembreTontineId(Long id, Long tontineId);

    boolean existsByMembreIdAndCycleIdAndMembreTontineId(Long membreId, Long cycleId, Long tontineId);
}
