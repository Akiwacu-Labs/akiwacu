package bi.ac.upg.akiwacu.config;

import bi.ac.upg.akiwacu.auth.AuthController;
import bi.ac.upg.akiwacu.auth.AuthService;
import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.auth.dto.LoginRequest;
import bi.ac.upg.akiwacu.common.exception.IdentifiantsInvalidesException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrairement aux tests de contrôleur (@AutoConfigureMockMvc(addFilters = false)),
 * ici la vraie chaîne de sécurité tourne : c'est SecurityConfig elle-même qu'on
 * vérifie, pas un contrôleur. On ne dépend d'aucun contrôleur du domaine tontine
 * (TontineController n'existe pas forcément dans ce slice) — seul AuthController
 * est chargé, pour garder ce test valable quel que soit l'ordre de fusion des PR.
 */
@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("SecurityConfig — routes publiques et protégées")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("D-35 — POST /api/tontines est explicitement public, jamais bloqué par la sécurité")
    void shouldLetAnonymousPostToTontinesThroughSecurity() throws Exception {
        // Ce slice ne charge pas TontineController (domaine de Juste) : un 404 prouve
        // que la requête a franchi la sécurité et échoué faute de contrôleur mappé.
        // Un 401/403 signalerait au contraire que le matcher permitAll a disparu.

        // Act & Assert
        mockMvc.perform(post("/api/tontines"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("R1/D-35 — GET /api/tontines (même chemin, autre méthode) reste authentifié")
    void shouldRejectAnonymousGetOnSamePathAsPublicPost() throws Exception {
        // Le matcher est scopé à HttpMethod.POST : un élargissement accidentel au
        // chemin entier laisserait passer ce GET, ce que ce test interdit.

        // Act & Assert
        mockMvc.perform(get("/api/tontines"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise"));
    }

    @Test
    @DisplayName("cas nominal — /api/auth/login reste public, la requête atteint bien le contrôleur")
    void shouldLetAnonymousLoginReachController() throws Exception {
        // Arrange : le service métier refuse (mauvais mot de passe) — le message
        // attendu vient de GlobalExceptionHandler, pas de l'entrypoint de sécurité,
        // ce qui prouve que la requête a bien atteint AuthController.
        when(authService.login(any()))
                .thenThrow(new IdentifiantsInvalidesException("Email ou mot de passe incorrect"));
        String corps = objectMapper.writeValueAsString(new LoginRequest("membre@akiwacu.bi", "mauvais"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(corps))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect"));
    }
}
