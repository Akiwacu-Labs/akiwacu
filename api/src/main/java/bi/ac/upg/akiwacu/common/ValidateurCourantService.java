package bi.ac.upg.akiwacu.common;

import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Résout le trésorier courant depuis le principal authentifié, jamais depuis le body. */
@Service
public class ValidateurCourantService {

    private final UtilisateurRepository utilisateurRepository;

    public ValidateurCourantService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur obtenir() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new RessourceIntrouvableException("Validateur authentifié introuvable");
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RessourceIntrouvableException("Validateur introuvable"));
    }
}
