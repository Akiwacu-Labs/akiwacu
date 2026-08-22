package bi.ac.upg.akiwacu.membre;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.membre.dto.MembreRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreResponse;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Voir CLAUDE.md « Conventions de tests » : @PreAuthorize exige de réimporter
// @EnableMethodSecurity dans ce slice, sinon il ne fait rien silencieusement.
@WebMvcTest(MembreController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MembreControllerTest.SecuriteMethodeTestConfig.class)
@DisplayName("MembreController — POST /api/membres")
class MembreControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecuriteMethodeTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MembreService membreService;

    @MockBean
    private JwtService jwtService;

    private String corpsValide() throws Exception {
        return objectMapper.writeValueAsString(
                new MembreRequest("M-030", "Havyarimana", "Marie", "78000000",
                        LocalDate.of(2026, 3, 1), null));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas nominal — GESTIONNAIRE crée un membre, renvoie 201")
    void shouldReturn201WhenGestionnaireCreates() throws Exception {
        var reponse = new MembreResponse(1L, "M-030", "Havyarimana", "Marie", "78000000",
                LocalDate.of(2026, 3, 1), StatutMembre.ACTIF, null);
        when(membreService.creer(any())).thenReturn(reponse);

        mockMvc.perform(post("/api/membres").contentType("application/json").content(corpsValide()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Havyarimana"));
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas d'erreur — un MEMBRE n'a pas le droit de créer un membre, renvoie 403")
    void shouldReturn403WhenRoleInsufficient() throws Exception {
        mockMvc.perform(post("/api/membres").contentType("application/json").content(corpsValide()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas d'exception — numéro de membre déjà utilisé, renvoie 409")
    void shouldReturn409OnDuplicateNumeroMembre() throws Exception {
        when(membreService.creer(any()))
                .thenThrow(new RegleMetierException("Ce numéro de membre est déjà utilisé dans cette tontine"));

        mockMvc.perform(post("/api/membres").contentType("application/json").content(corpsValide()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas de validation — téléphone manquant, renvoie 400 sans appeler le service")
    void shouldReturn400WhenTelephoneIsBlank() throws Exception {
        String corps = objectMapper.writeValueAsString(
                new MembreRequest("M-030", "Havyarimana", "Marie", "",
                        LocalDate.of(2026, 3, 1), null));

        mockMvc.perform(post("/api/membres").contentType("application/json").content(corps))
                .andExpect(status().isBadRequest());
    }
}
