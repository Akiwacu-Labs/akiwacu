package bi.ac.upg.akiwacu.vote;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.demandepret.DemandePret;
import bi.ac.upg.akiwacu.demandepret.DemandePretRepository;
import bi.ac.upg.akiwacu.demandepret.StatutDemandePret;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import bi.ac.upg.akiwacu.vote.dto.VoteRequest;
import bi.ac.upg.akiwacu.vote.dto.VoteResponse;
import bi.ac.upg.akiwacu.vote.mapper.VoteCommissaireMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * PROPRIÉTAIRE : Gloria.
 * R4 est protégée par le contrôle lisible ci-dessous et par la contrainte
 * unique publiée dans VoteCommissaire : les deux protections sont nécessaires.
 */
@Service
public class VoteService {

    private final VoteCommissaireRepository voteRepository;
    private final DemandePretRepository demandePretRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VoteCommissaireMapper voteMapper;

    public VoteService(VoteCommissaireRepository voteRepository,
                       DemandePretRepository demandePretRepository,
                       UtilisateurRepository utilisateurRepository,
                       VoteCommissaireMapper voteMapper) {
        this.voteRepository = voteRepository;
        this.demandePretRepository = demandePretRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.voteMapper = voteMapper;
    }

    @Transactional
    public VoteResponse voter(Long demandePretId, VoteRequest requete) {
        Long tontineId = TenantContext.getTontineId();
        DemandePret demande = demandePretRepository.findById(demandePretId)
                .orElseThrow(() -> new RessourceIntrouvableException("Demande de prêt introuvable"));
        verifierTontine(demande.getCycle().getTontine().getId(), tontineId);

        Utilisateur commissaire = utilisateurCourant(tontineId);
        if (voteRepository.existsByDemandePretIdAndCommissaireId(demandePretId, commissaire.getId())) {
            throw new RegleMetierException("R4 : un commissaire ne peut voter qu'une seule fois");
        }

        var vote = VoteCommissaire.builder()
                .demandePret(demande)
                .commissaire(commissaire)
                .sens(requete.sens())
                .commentaire(requete.commentaire())
                .dateVote(Instant.now())
                .build();
        VoteCommissaire voteEnregistre = voteRepository.save(vote);
        mettreAJourStatut(demande);

        return voteMapper.versReponse(voteEnregistre);
    }

    private void mettreAJourStatut(DemandePret demande) {
        long pour = voteRepository.countByDemandePretIdAndSens(demande.getId(), SensVote.POUR);
        long contre = voteRepository.countByDemandePretIdAndSens(demande.getId(), SensVote.CONTRE);

        if (pour >= 2) {
            demande.setStatut(StatutDemandePret.APPROUVEE);
            demandePretRepository.save(demande);
        } else if (contre >= 2) {
            demande.setStatut(StatutDemandePret.REJETEE);
            demandePretRepository.save(demande);
        }
    }

    private Utilisateur utilisateurCourant(Long tontineId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Commissaire non authentifié");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RessourceIntrouvableException("Commissaire introuvable"));
        verifierTontine(utilisateur.getTontine().getId(), tontineId);
        return utilisateur;
    }

    private void verifierTontine(Long ressourceTontineId, Long tontineCourante) {
        if (!ressourceTontineId.equals(tontineCourante)) {
            throw new AccessDeniedException("Ressource hors de la tontine courante");
        }
    }
}
