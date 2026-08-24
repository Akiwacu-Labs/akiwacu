package bi.ac.upg.akiwacu.demandepret;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import org.springframework.security.access.AccessDeniedException;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretRequest;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemandePretController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DemandePretControllerTest.SecuriteMethodeTestConfig.class)
@DisplayName("DemandePretController — POST /api/demandes-pret")
class DemandePretControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecuriteMethodeTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DemandePretService demandePretService;

    @MockBean
    private JwtService jwtService;

    private String corpsValide() throws Exception {
        return objectMapper.writeValueAsString(
                new DemandePretRequest(7L, new BigDecimal("250000"), 3, "Achat de semences"));
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas nominal — MEMBRE soumet une demande, renvoie 201")
    void shouldReturn201WhenMemberSubmitsRequest() throws Exception {
        var reponse = new DemandePretResponse(15L, 7L, 10L,
                new BigDecimal("250000"), 3, "Achat de semences", null,
                StatutDemandePret.SOUMISE);
        when(demandePretService.demanderPret(any())).thenReturn(reponse);

        mockMvc.perform(post("/api/demandes-pret")
                        .contentType("application/json")
                        .content(corpsValide()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("SOUMISE"));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas d'erreur — GESTIONNAIRE ne soumet pas au nom d'un membre, renvoie 403")
    void shouldReturn403WhenRoleIsInsufficient() throws Exception {
        mockMvc.perform(post("/api/demandes-pret")
                        .contentType("application/json")
                        .content(corpsValide()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas d'exception — R6 violée, renvoie 409")
    void shouldReturn409WhenR6IsViolated() throws Exception {
        when(demandePretService.demanderPret(any()))
                .thenThrow(new RegleMetierException("R6 : plafond dépassé"));

        mockMvc.perform(post("/api/demandes-pret")
                        .contentType("application/json")
                        .content(corpsValide()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas d'accès — un membre ne peut pas demander pour un autre membre")
    void shouldReturn403WhenMemberSubmitsForAnotherMember() throws Exception {
        when(demandePretService.demanderPret(any()))
                .thenThrow(new AccessDeniedException("Compte membre différent"));

        mockMvc.perform(post("/api/demandes-pret")
                        .contentType("application/json")
                        .content(corpsValide()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas de validation — montant manquant, renvoie 400")
    void shouldReturn400WhenAmountIsMissing() throws Exception {
        String corps = """
                {"membreId":7,"dureeMois":3,"motif":"Achat de semences"}
                """;

        mockMvc.perform(post("/api/demandes-pret")
                        .contentType("application/json")
                        .content(corps))
                .andExpect(status().isBadRequest());
    }
}
