package bi.ac.upg.akiwacu.utilisateur;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// addFilters = false : sans SecurityConfig (exclu de ce slice), la CSRF par
// défaut de Spring Security bloquerait nos POST avant même d'atteindre le
// contrôleur.
//
// @EnableMethodSecurity vit sur SecurityConfig, lui aussi exclu de ce slice
// (@WebMvcTest ne charge pas les @Configuration ordinaires) : sans le réimporter
// ici, @PreAuthorize ne fait RIEN — vérifié en pratique, un premier essai sans
// ce bloc laissait un MEMBRE créer un utilisateur (201 au lieu de 403).
@WebMvcTest(UtilisateurController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UtilisateurControllerTest.SecuriteMethodeTestConfig.class)
@DisplayName("UtilisateurController — POST /api/utilisateurs")
class UtilisateurControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecuriteMethodeTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtilisateurService utilisateurService;

    // JwtAuthenticationFilter est un @Component Filter : @WebMvcTest l'instancie
    // quand même (voir AuthControllerTest), donc son JwtService doit être mocké.
    @MockBean
    private JwtService jwtService;

    private String corpsValide() throws Exception {
        return objectMapper.writeValueAsString(
                new UtilisateurRequest("nouveau@akiwacu.bi", "motdepasse-valide",
                        "Bukuru", "Jean", "111", Set.of(Role.TRESORIER)));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas nominal — GESTIONNAIRE crée un utilisateur, renvoie 201")
    void shouldReturn201WhenGestionnaireCreates() throws Exception {
        var reponse = new UtilisateurResponse(1L, "nouveau@akiwacu.bi", "Bukuru", "Jean", "111",
                true, Set.of(Role.TRESORIER));
        when(utilisateurService.creer(any())).thenReturn(reponse);

        mockMvc.perform(post("/api/utilisateurs").contentType("application/json").content(corpsValide()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nouveau@akiwacu.bi"));
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas d'erreur — un MEMBRE n'a pas le droit de créer un utilisateur, renvoie 403")
    void shouldReturn403WhenRoleInsufficient() throws Exception {
        mockMvc.perform(post("/api/utilisateurs").contentType("application/json").content(corpsValide()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas d'exception — email déjà utilisé, renvoie 409")
    void shouldReturn409OnDuplicateEmail() throws Exception {
        when(utilisateurService.creer(any())).thenThrow(new RegleMetierException("Cet email est déjà utilisé"));

        mockMvc.perform(post("/api/utilisateurs").contentType("application/json").content(corpsValide()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas de validation — email manquant, renvoie 400 sans appeler le service")
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        String corps = objectMapper.writeValueAsString(
                new UtilisateurRequest("", "motdepasse-valide", "Bukuru", "Jean", "111", Set.of(Role.TRESORIER)));

        mockMvc.perform(post("/api/utilisateurs").contentType("application/json").content(corps))
                .andExpect(status().isBadRequest());
    }
}
