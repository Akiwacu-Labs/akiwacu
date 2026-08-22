package bi.ac.upg.akiwacu.vote;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.vote.dto.VoteRequest;
import bi.ac.upg.akiwacu.vote.dto.VoteResponse;
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

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(VoteControllerTest.SecuriteMethodeTestConfig.class)
@DisplayName("VoteController — POST /api/demandes-pret/{id}/votes")
class VoteControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecuriteMethodeTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VoteService voteService;

    @MockBean
    private JwtService jwtService;

    private String corpsValide() throws Exception {
        return objectMapper.writeValueAsString(new VoteRequest(SensVote.POUR, "Projet viable"));
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    @DisplayName("cas nominal — COMMISSAIRE vote, renvoie 201")
    void shouldReturn201WhenCommissionerVotes() throws Exception {
        var reponse = new VoteResponse(31L, 20L, 8L, SensVote.POUR,
                "Projet viable", Instant.parse("2026-08-22T20:00:00Z"));
        when(voteService.voter(any(), any())).thenReturn(reponse);

        mockMvc.perform(post("/api/demandes-pret/20/votes")
                        .contentType("application/json")
                        .content(corpsValide()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sens").value("POUR"));
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas d'erreur — MEMBRE ne peut pas voter, renvoie 403")
    void shouldReturn403WhenRoleIsInsufficient() throws Exception {
        mockMvc.perform(post("/api/demandes-pret/20/votes")
                        .contentType("application/json")
                        .content(corpsValide()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    @DisplayName("cas d'exception — vote déjà enregistré, renvoie 409")
    void shouldReturn409WhenCommissionerAlreadyVoted() throws Exception {
        when(voteService.voter(any(), any()))
                .thenThrow(new RegleMetierException("R4 : un commissaire ne peut voter qu'une seule fois"));

        mockMvc.perform(post("/api/demandes-pret/20/votes")
                        .contentType("application/json")
                        .content(corpsValide()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    @DisplayName("cas de validation — sens manquant, renvoie 400")
    void shouldReturn400WhenVoteSenseIsMissing() throws Exception {
        mockMvc.perform(post("/api/demandes-pret/20/votes")
                        .contentType("application/json")
                        .content("{\"commentaire\":\"Projet viable\"}"))
                .andExpect(status().isBadRequest());
    }
}
