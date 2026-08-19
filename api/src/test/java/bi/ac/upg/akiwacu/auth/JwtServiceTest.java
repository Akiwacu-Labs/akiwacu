package bi.ac.upg.akiwacu.auth;

import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Role;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService — émission et lecture des jetons")
class JwtServiceTest {

    // Secret de test uniquement — jamais celui utilisé en dev/prod (application.yml).
    private static final String SECRET_TEST = "un-secret-de-test-largement-superieur-a-256-bits-pour-hs256";

    private final JwtService jwtService = new JwtService(SECRET_TEST, 120);

    private Utilisateur unUtilisateur() {
        Tontine tontine = Tontine.builder().build();
        tontine.setId(5L);

        Utilisateur utilisateur = Utilisateur.builder()
                .tontine(tontine)
                .email("gestionnaire@akiwacu.bi")
                .motDePasse("hash-non-lu-ici")
                .nom("Nkurunziza")
                .prenom("Alice")
                .roles(Set.of(Role.GESTIONNAIRE))
                .build();
        utilisateur.setId(11L);
        return utilisateur;
    }

    @Test
    @DisplayName("R1 — le jeton porte le tontineId de l'utilisateur, pas un tontineId arbitraire")
    void shouldEmbedTontineIdInToken() {
        // Arrange
        Utilisateur utilisateur = unUtilisateur();

        // Act
        String token = jwtService.genererToken(utilisateur);

        // Assert : c'est ce claim que JwtAuthenticationFilter pose dans TenantContext.
        assertThat(jwtService.extraireTontineId(token)).isEqualTo(5L);
    }

    @Test
    @DisplayName("émet un jeton dont l'email et les rôles se relisent identiques")
    void shouldRoundTripEmailAndRoles() {
        // Arrange
        Utilisateur utilisateur = unUtilisateur();

        // Act
        String token = jwtService.genererToken(utilisateur);

        // Assert
        assertThat(jwtService.extraireEmail(token)).isEqualTo("gestionnaire@akiwacu.bi");
        assertThat(jwtService.extraireRoles(token)).containsExactly(Role.GESTIONNAIRE);
    }

    @Test
    @DisplayName("pose une expiration dans le futur, cohérente avec expiration-minutes")
    void shouldSetExpirationInTheFuture() {
        // Arrange
        Utilisateur utilisateur = unUtilisateur();

        // Act
        String token = jwtService.genererToken(utilisateur);

        // Assert
        assertThat(jwtService.expirationDe(token)).isAfter(Instant.now());
    }

    @Test
    @DisplayName("refuse un jeton signé avec une autre clé")
    void shouldRejectTokenSignedWithAnotherKey() {
        // Un jeton signé par une autre instance (donc une autre clé) ne doit
        // jamais être accepté : sinon n'importe quel service pourrait forger
        // des jetons Akiwacu.
        JwtService autreService = new JwtService("une-cle-totalement-differente-elle-aussi-tres-longue", 120);
        String token = autreService.genererToken(unUtilisateur());

        assertThatThrownBy(() -> jwtService.extraireEmail(token))
                .isInstanceOf(SignatureException.class);
    }
}
