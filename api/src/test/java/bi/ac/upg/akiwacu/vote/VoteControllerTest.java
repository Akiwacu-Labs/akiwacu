package bi.ac.upg.akiwacu.vote;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.vote.dto.VoteRequest;
import bi.ac.upg.akiwacu.vote.dto.VoteResponse;
import bi.ac.upg.akiwacu.vote.dto.VoteDecisionResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas nominal — membre consulte un vote, renvoie 200")
    void shouldReturn200WhenVoteIsFound() throws Exception {
        when(voteService.trouver(20L, 31L))
                .thenReturn(new VoteResponse(31L, 20L, 8L, SensVote.POUR, null,
                        Instant.parse("2026-08-22T20:00:00Z")));

        mockMvc.perform(get("/api/demandes-pret/20/votes/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(31));
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas d'erreur — vote introuvable, renvoie 404")
    void shouldReturn404WhenVoteIsMissing() throws Exception {
        when(voteService.trouver(20L, 31L))
                .thenThrow(new bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException("Vote introuvable"));

        mockMvc.perform(get("/api/demandes-pret/20/votes/31"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PRESIDENT")
    @DisplayName("cas d'autorisation — PRESIDENT ne consulte pas un vote, renvoie 403")
    void shouldReturn403WhenRoleCannotReadVote() throws Exception {
        mockMvc.perform(get("/api/demandes-pret/20/votes/31"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas nominal — membre consulte le résumé de décision")
    void shouldReturnDecisionSummary() throws Exception {
        when(voteService.decision(20L)).thenReturn(
                new VoteDecisionResponse(20L, 1L, 0L, 2, false,
                        bi.ac.upg.akiwacu.demandepret.StatutDemandePret.SOUMISE));

        mockMvc.perform(get("/api/demandes-pret/20/votes/decision"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votesPour").value(1))
                .andExpect(jsonPath("$.quorumAtteint").value(false));
    }

    @Test
    @WithMockUser(roles = "PRESIDENT")
    @DisplayName("cas d'autorisation — PRESIDENT ne consulte pas la décision, renvoie 403")
    void shouldReturn403WhenRoleCannotReadDecisionSummary() throws Exception {
        mockMvc.perform(get("/api/demandes-pret/20/votes/decision"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("cas d'erreur — demande du résumé introuvable, renvoie 404")
    void shouldReturn404WhenDecisionSummaryRequestIsMissing() throws Exception {
        when(voteService.decision(20L)).thenThrow(
                new bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException(
                        "Demande de prêt introuvable"));

        mockMvc.perform(get("/api/demandes-pret/20/votes/decision"))
                .andExpect(status().isNotFound());
    }
}
