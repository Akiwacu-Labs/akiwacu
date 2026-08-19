package bi.ac.upg.akiwacu.utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PROPRIÉTAIRE : Andy.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // email est unique (contrainte de colonne, voir V2) : au plus un résultat.
    Optional<Utilisateur> findByEmail(String email);
}
