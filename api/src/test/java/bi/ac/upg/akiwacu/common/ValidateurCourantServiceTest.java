package bi.ac.upg.akiwacu.common;

import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateurCourantService — résolution R5")
class ValidateurCourantServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private ValidateurCourantService service;

    @AfterEach
    void nettoyerContexte() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("R5 — résout l'utilisateur authentifié par son email")
    void shouldResolveAuthenticatedUser() {
        var utilisateur = Utilisateur.builder().email("tresorier@akiwacu.bi").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        utilisateur.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_TRESORIER"))));
        when(utilisateurRepository.findByEmail(utilisateur.getEmail())).thenReturn(Optional.of(utilisateur));

        assertThat(service.obtenir()).isSameAs(utilisateur);
        verify(utilisateurRepository).findByEmail(utilisateur.getEmail());
    }

    @Test
    @DisplayName("R5 — refuse une authentification absente ou non authentifiée")
    void shouldRejectMissingOrUnauthenticatedAuthentication() {
        assertThatThrownBy(service::obtenir)
                .isInstanceOf(RessourceIntrouvableException.class);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tresorier@akiwacu.bi", "secret"));

        assertThatThrownBy(service::obtenir)
                .isInstanceOf(RessourceIntrouvableException.class);
        verifyNoInteractions(utilisateurRepository);
    }

    @Test
    @DisplayName("R5 — refuse une identité anonyme")
    void shouldRejectAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThatThrownBy(service::obtenir)
                .isInstanceOf(RessourceIntrouvableException.class);
        verifyNoInteractions(utilisateurRepository);
    }

    @Test
    @DisplayName("R5 — refuse un utilisateur absent du dépôt")
    void shouldRejectUnknownAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "inconnu@akiwacu.bi", null,
                        List.of(new SimpleGrantedAuthority("ROLE_TRESORIER"))));
        when(utilisateurRepository.findByEmail("inconnu@akiwacu.bi")).thenReturn(Optional.empty());

        assertThatThrownBy(service::obtenir)
                .isInstanceOf(RessourceIntrouvableException.class);
    }
}
