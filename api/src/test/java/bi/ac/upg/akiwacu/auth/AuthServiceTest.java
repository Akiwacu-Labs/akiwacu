package bi.ac.upg.akiwacu.auth;

import bi.ac.upg.akiwacu.auth.dto.LoginRequest;
import bi.ac.upg.akiwacu.common.exception.IdentifiantsInvalidesException;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Role;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — vérification des identifiants et émission du jeton")
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(utilisateurRepository, passwordEncoder, jwtService);
    }

    private Utilisateur unUtilisateurActif() {
        Tontine tontine = Tontine.builder().build();
        tontine.setId(3L);

        Utilisateur utilisateur = Utilisateur.builder()
                .tontine(tontine)
                .email("tresoriere@akiwacu.bi")
                .motDePasse("hash-bcrypt")
                .nom("Ndayishimiye")
                .prenom("Claudine")
                .actif(true)
                .roles(Set.of(Role.TRESORIER))
                .build();
        utilisateur.setId(9L);
        return utilisateur;
    }

    @Test
    @DisplayName("cas nominal — identifiants corrects, renvoie un jeton")
    void shouldReturnTokenWhenCredentialsAreValid() {
        // Arrange
        Utilisateur utilisateur = unUtilisateurActif();
        var requete = new LoginRequest("tresoriere@akiwacu.bi", "motdepasse-clair");
        when(utilisateurRepository.findByEmail("tresoriere@akiwacu.bi")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("motdepasse-clair", "hash-bcrypt")).thenReturn(true);
        when(jwtService.genererToken(utilisateur)).thenReturn("jeton.simule.test");
        when(jwtService.expirationDe("jeton.simule.test")).thenReturn(Instant.now().plusSeconds(3600));

        // Act
        var reponse = authService.login(requete);

        // Assert
        assertThat(reponse.jeton()).isEqualTo("jeton.simule.test");
        assertThat(reponse.tontineId()).isEqualTo(3L);
        assertThat(reponse.roles()).containsExactly(Role.TRESORIER);
    }

    @Test
    @DisplayName("cas d'erreur — email inconnu, refuse sans révéler la cause")
    void shouldRejectUnknownEmail() {
        // Arrange
        var requete = new LoginRequest("inconnu@akiwacu.bi", "peu-importe");
        when(utilisateurRepository.findByEmail("inconnu@akiwacu.bi")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(requete))
                .isInstanceOf(IdentifiantsInvalidesException.class);
    }

    @Test
    @DisplayName("cas d'erreur — mot de passe incorrect")
    void shouldRejectWrongPassword() {
        // Arrange
        Utilisateur utilisateur = unUtilisateurActif();
        var requete = new LoginRequest("tresoriere@akiwacu.bi", "mauvais-mot-de-passe");
        when(utilisateurRepository.findByEmail("tresoriere@akiwacu.bi")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("mauvais-mot-de-passe", "hash-bcrypt")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(requete))
                .isInstanceOf(IdentifiantsInvalidesException.class);
    }

    @Test
    @DisplayName("cas d'exception — compte désactivé, refuse même avec le bon mot de passe")
    void shouldRejectInactiveAccount() {
        // Un compte désactivé (ex-membre parti, employé quitté) ne doit jamais
        // pouvoir se reconnecter, quel que soit le mot de passe fourni.
        Utilisateur utilisateur = unUtilisateurActif();
        utilisateur.setActif(false);
        var requete = new LoginRequest("tresoriere@akiwacu.bi", "motdepasse-clair");
        when(utilisateurRepository.findByEmail("tresoriere@akiwacu.bi")).thenReturn(Optional.of(utilisateur));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(requete))
                .isInstanceOf(IdentifiantsInvalidesException.class);
    }
}
