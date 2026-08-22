package bi.ac.upg.akiwacu.utilisateur;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurModificationRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurResponse;
import bi.ac.upg.akiwacu.utilisateur.mapper.UtilisateurMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PROPRIÉTAIRE : Andy.
 * tontineId vient exclusivement de TenantContext (R1) — jamais du corps de
 * la requête. Le filtre Hibernate sur Utilisateur (DECISIONS.md D-33) fait
 * qu'un id d'une autre tontine se comporte déjà comme "introuvable" avant
 * même d'atteindre la vérification explicite ci-dessous : les deux se
 * complètent, aucune des deux ne suffit seule à documenter l'intention.
 */
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final TontineRepository tontineRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                               TontineRepository tontineRepository,
                               UtilisateurMapper utilisateurMapper,
                               PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.tontineRepository = tontineRepository;
        this.utilisateurMapper = utilisateurMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UtilisateurResponse creer(UtilisateurRequest requete) {
        if (utilisateurRepository.findByEmail(requete.email()).isPresent()) {
            throw new RegleMetierException("Cet email est déjà utilisé");
        }

        Tontine tontine = tontineRepository.findById(TenantContext.getTontineId())
                .orElseThrow(() -> new RessourceIntrouvableException("Tontine introuvable"));

        Utilisateur utilisateur = utilisateurMapper.versEntite(requete);
        utilisateur.setTontine(tontine);
        utilisateur.setMotDePasse(passwordEncoder.encode(requete.motDePasse()));
        utilisateur.setActif(true);

        return utilisateurMapper.versReponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> lister() {
        return utilisateurRepository.findAll().stream()
                .map(utilisateurMapper::versReponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse recuperer(Long id) {
        return utilisateurMapper.versReponse(trouverOuLever(id));
    }

    @Transactional
    public UtilisateurResponse modifier(Long id, UtilisateurModificationRequest requete) {
        Utilisateur utilisateur = trouverOuLever(id);
        utilisateurMapper.mettreAJour(utilisateur, requete);
        return utilisateurMapper.versReponse(utilisateur);
    }

    /**
     * Désactive plutôt que supprime : Utilisateur est référencé par Membre,
     * et comme validePar sur Cotisation/TransactionCaisse (R5) — une
     * suppression physique casserait ces références ou l'historique
     * d'audit. actif=false retire l'accès sans perdre la traçabilité.
     */
    @Transactional
    public void desactiver(Long id) {
        Utilisateur utilisateur = trouverOuLever(id);
        utilisateur.setActif(false);
    }

    private Utilisateur trouverOuLever(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }
}
