package bi.ac.upg.akiwacu.membre;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.membre.dto.MembreModificationRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreResponse;
import bi.ac.upg.akiwacu.membre.mapper.MembreMapper;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * PROPRIÉTAIRE : Andy.
 * tontineId vient exclusivement de TenantContext (R1). Le filtre Hibernate
 * sur Membre ET Utilisateur (DECISIONS.md D-33) fait que lier un
 * utilisateurId d'une autre tontine échoue déjà à la lecture — la
 * vérification explicite documente l'intention, elle ne fait pas le
 * travail seule.
 */
@Service
public class MembreService {

    private final MembreRepository membreRepository;
    private final TontineRepository tontineRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MembreMapper membreMapper;

    public MembreService(MembreRepository membreRepository,
                          TontineRepository tontineRepository,
                          UtilisateurRepository utilisateurRepository,
                          MembreMapper membreMapper) {
        this.membreRepository = membreRepository;
        this.tontineRepository = tontineRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.membreMapper = membreMapper;
    }

    @Transactional
    public MembreResponse creer(MembreRequest requete) {
        verifierNumeroMembreDisponible(requete.numeroMembre());

        Tontine tontine = tontineRepository.findById(TenantContext.getTontineId())
                .orElseThrow(() -> new RessourceIntrouvableException("Tontine introuvable"));

        Membre membre = membreMapper.versEntite(requete);
        membre.setTontine(tontine);
        membre.setStatut(StatutMembre.ACTIF);
        membre.setUtilisateur(resoudreUtilisateur(requete.utilisateurId()));

        return membreMapper.versReponse(membreRepository.save(membre));
    }

    @Transactional(readOnly = true)
    public List<MembreResponse> lister() {
        return membreRepository.findAll().stream()
                .map(membreMapper::versReponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MembreResponse recuperer(Long id) {
        return membreMapper.versReponse(trouverOuLever(id));
    }

    @Transactional
    public MembreResponse modifier(Long id, MembreModificationRequest requete) {
        Membre membre = trouverOuLever(id);
        if (!Objects.equals(membre.getNumeroMembre(), requete.numeroMembre())) {
            verifierNumeroMembreDisponible(requete.numeroMembre());
        }
        membreMapper.mettreAJour(membre, requete);
        return membreMapper.versReponse(membre);
    }

    private void verifierNumeroMembreDisponible(String numeroMembre) {
        if (StringUtils.hasText(numeroMembre) && membreRepository.existsByNumeroMembre(numeroMembre)) {
            throw new RegleMetierException("Ce numéro de membre est déjà utilisé dans cette tontine");
        }
    }

    private Utilisateur resoudreUtilisateur(Long utilisateurId) {
        if (utilisateurId == null) {
            return null;
        }
        // Filtré par tontine_id (D-33) : un id d'une autre tontine est déjà
        // introuvable ici, avant même la levée de cette exception.
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }

    private Membre trouverOuLever(Long id) {
        return membreRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Membre introuvable"));
    }
}
