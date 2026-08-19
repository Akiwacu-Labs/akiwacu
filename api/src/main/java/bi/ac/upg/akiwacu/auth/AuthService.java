package bi.ac.upg.akiwacu.auth;

import bi.ac.upg.akiwacu.auth.dto.LoginRequest;
import bi.ac.upg.akiwacu.auth.dto.LoginResponse;
import bi.ac.upg.akiwacu.common.exception.IdentifiantsInvalidesException;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PROPRIÉTAIRE : Andy.
 * Authentification manuelle (pas d'AuthenticationManager Spring Security) :
 * l'API reste directe à lire et à expliquer à l'oral — comparer le mot de
 * passe, vérifier le compte actif, émettre le jeton.
 */
@Service
public class AuthService {

    // Message volontairement identique pour les deux cas : ne pas révéler à un
    // attaquant si c'est l'email ou le mot de passe qui est incorrect.
    private static final String MESSAGE_IDENTIFIANTS_INVALIDES = "Email ou mot de passe incorrect";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest requete) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.email())
                .orElseThrow(() -> new IdentifiantsInvalidesException(MESSAGE_IDENTIFIANTS_INVALIDES));

        if (!utilisateur.isActif()) {
            throw new IdentifiantsInvalidesException(MESSAGE_IDENTIFIANTS_INVALIDES);
        }

        if (!passwordEncoder.matches(requete.motDePasse(), utilisateur.getMotDePasse())) {
            throw new IdentifiantsInvalidesException(MESSAGE_IDENTIFIANTS_INVALIDES);
        }

        String jeton = jwtService.genererToken(utilisateur);

        return new LoginResponse(
                jeton,
                jwtService.expirationDe(jeton),
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getTontine().getId(),
                utilisateur.getRoles());
    }
}
