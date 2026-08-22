package bi.ac.upg.akiwacu.tontine;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.tontine.dto.TontineCreationRequest;
import bi.ac.upg.akiwacu.tontine.dto.TontineRequest;
import bi.ac.upg.akiwacu.tontine.dto.TontineResponse;
import bi.ac.upg.akiwacu.utilisateur.Role;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Service applicatif du domaine tontine. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TontineService {

    private final TontineRepository tontineRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public List<TontineResponse> lister() {
        // La tontine est la racine du tenant : son identifiant est celui du contexte JWT.
        return tontineRepository.findById(TenantContext.getTontineId()).stream()
                .map(this::versResponse).toList();
    }

    public TontineResponse trouverParId(Long id) {
        return versResponse(trouverEntiteAccessible(id));
    }

    @Transactional
    public TontineResponse creer(TontineCreationRequest requete) {
        verifierNomDisponible(requete.nom());
        var tontine = Tontine.builder()
                .nom(requete.nom())
                .description(requete.description())
                .dateCreation(requete.dateCreation())
                .statut(requete.statut())
                .build();
        var tontineCreee = tontineRepository.save(tontine);
        var administrateur = requete.administrateur();
        var utilisateur = Utilisateur.builder()
                .tontine(tontineCreee)
                .email(administrateur.email())
                .motDePasse(passwordEncoder.encode(administrateur.motDePasse()))
                .nom(administrateur.nom())
                .prenom(administrateur.prenom())
                .telephone(administrateur.telephone())
                .roles(java.util.Set.of(Role.ADMIN))
                .actif(true)
                .build();
        utilisateurRepository.save(utilisateur);
        return versResponse(tontineCreee);
    }

    @Transactional
    public TontineResponse modifier(Long id, TontineRequest requete) {
        var tontine = trouverEntiteAccessible(id);
        if (!tontine.getNom().equals(requete.nom())) {
            verifierNomDisponible(requete.nom());
        }
        tontine.setNom(requete.nom());
        tontine.setDescription(requete.description());
        tontine.setDateCreation(requete.dateCreation());
        tontine.setStatut(requete.statut());
        return versResponse(tontineRepository.save(tontine));
    }

    @Transactional
    public void supprimer(Long id) {
        tontineRepository.delete(trouverEntiteAccessible(id));
    }

    private Tontine trouverEntiteAccessible(Long id) {
        // Un id de chemin ne peut jamais choisir le tenant ; il doit correspondre
        // au tontineId signé dans le JWT de l'appelant.
        if (!TenantContext.getTontineId().equals(id)) {
            throw new RessourceIntrouvableException("Tontine introuvable : " + id);
        }
        return trouverEntite(id);
    }

    private Tontine trouverEntite(Long id) {
        return tontineRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Tontine introuvable : " + id));
    }

    private void verifierNomDisponible(String nom) {
        if (tontineRepository.existsByNom(nom)) {
            throw new RegleMetierException("Une tontine porte déjà le nom : " + nom);
        }
    }

    private TontineResponse versResponse(Tontine tontine) {
        return new TontineResponse(tontine.getId(), tontine.getNom(), tontine.getDescription(),
                tontine.getDateCreation(), tontine.getStatut());
    }
}
