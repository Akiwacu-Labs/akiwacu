package bi.ac.upg.akiwacu.cycle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * PROPRIÉTAIRE : Benitha.
 * Les recherches sont toujours limitées à une tontine : c'est le contrat R1
 * utilisé par les services du domaine cycle.
 */
@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {

    Optional<Cycle> findByTontineIdAndStatut(Long tontineId, StatutCycle statut);

    boolean existsByTontineIdAndStatut(Long tontineId, StatutCycle statut);

    List<Cycle> findAllByTontineId(Long tontineId);

    Optional<Cycle> findByIdAndTontineId(Long id, Long tontineId);
}
