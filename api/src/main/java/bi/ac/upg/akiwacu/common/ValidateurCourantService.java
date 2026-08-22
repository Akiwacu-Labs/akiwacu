package bi.ac.upg.akiwacu.common;

import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Résout le trésorier authentifié pour l'audit des opérations financières. */
@Service
public class ValidateurCourantService {

    private final UtilisateurRepository utilisateurRepository;

    public ValidateurCourantService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Retourne l'utilisateur porté par le SecurityContext courant.
     * Une identité absente, anonyme ou non authentifiée ne peut pas valider
     * une opération financière (R5).
     */
    public Utilisateur obtenir() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new RessourceIntrouvableException("Validateur authentifié introuvable");
        }

        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RessourceIntrouvableException("Validateur authentifié introuvable"));
    }
}
