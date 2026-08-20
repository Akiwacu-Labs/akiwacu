package bi.ac.upg.akiwacu.membre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PROPRIÉTAIRE : Andy.
 */
@Repository
public interface MembreRepository extends JpaRepository<Membre, Long> {

    // Filtré par tontine_id via le filtre Hibernate (D-33) : deux tontines
    // peuvent réutiliser le même numeroMembre sans collision.
    boolean existsByNumeroMembre(String numeroMembre);
}
