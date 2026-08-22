package bi.ac.upg.akiwacu.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PROPRIÉTAIRE : Gloria.
 * Vide volontairement — les méthodes de requête appartiennent au domaine
 * qui les consomme, pas à la PR socle.
 */
@Repository
public interface VoteCommissaireRepository extends JpaRepository<VoteCommissaire, Long> {

    boolean existsByDemandePretIdAndCommissaireId(Long demandePretId, Long commissaireId);

    long countByDemandePretIdAndSens(Long demandePretId, SensVote sens);

    List<VoteCommissaire> findByDemandePretId(Long demandePretId);
}
