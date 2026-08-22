package bi.ac.upg.akiwacu.recu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PROPRIÉTAIRE : Juste.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface RecuRepository extends JpaRepository<Recu, Long> {

    Optional<Recu> findByTypeOperationAndOperationId(TypeOperationRecu typeOperation, Long operationId);

    @org.springframework.data.jpa.repository.Query(value = "select nextval('seq_numero_recu')", nativeQuery = true)
    long prochainNumero();
}
