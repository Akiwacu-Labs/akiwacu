package bi.ac.upg.akiwacu.pret;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.pret.dto.PretDisbursementRequest;
import bi.ac.upg.akiwacu.pret.dto.PretResponse;
import bi.ac.upg.akiwacu.pret.dto.PretScheduleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PretController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PretControllerTest.SecuriteMethodeTestConfig.class)
@DisplayName("PretController — déblocage")
class PretControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecuriteMethodeTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PretService pretService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "TRESORIER")
    void shouldReturn201WhenTreasurerDisbursesApprovedLoan() throws Exception {
        when(pretService.debloquerPret(any())).thenReturn(new PretResponse(
                30L, 20L, 7L, 10L, new BigDecimal("250000"), 3,
                LocalDate.of(2026, 8, 23), LocalDate.of(2026, 12, 31),
                StatutPret.ACTIF, 40L));

        mockMvc.perform(post("/api/prets")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PretDisbursementRequest(20L, LocalDate.of(2026, 12, 31)))))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void shouldReturn403WhenMemberDisbursesLoan() throws Exception {
        mockMvc.perform(post("/api/prets")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PretDisbursementRequest(20L, LocalDate.of(2026, 12, 31)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    void shouldReturn409WhenDisbursementRuleIsViolated() throws Exception {
        when(pretService.debloquerPret(any()))
                .thenThrow(new RegleMetierException("R3 : cycle gelé"));

        mockMvc.perform(post("/api/prets")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PretDisbursementRequest(20L, LocalDate.of(2026, 12, 31)))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void shouldReturn200WhenListingLoans() throws Exception {
        when(pretService.listerPrets()).thenReturn(List.of(exampleResponse()));

        mockMvc.perform(get("/api/prets"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void shouldReturn200WhenReadingLoanDetail() throws Exception {
        when(pretService.trouverPret(30L)).thenReturn(exampleResponse());

        mockMvc.perform(get("/api/prets/30"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void shouldReturn200WhenReadingLoanSchedule() throws Exception {
        when(pretService.echeancier(30L)).thenReturn(new PretScheduleResponse(
                30L, 3, new BigDecimal("250000"), new BigDecimal("250000"),
                LocalDate.of(2026, 8, 23), LocalDate.of(2026, 12, 31)));

        mockMvc.perform(get("/api/prets/30/echeancier"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void shouldReturn404WhenLoanDetailIsMissing() throws Exception {
        when(pretService.trouverPret(404L))
                .thenThrow(new RessourceIntrouvableException("Prêt introuvable"));

        mockMvc.perform(get("/api/prets/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void shouldReturn403WhenLoanBelongsToAnotherTenant() throws Exception {
        when(pretService.trouverPret(99L))
                .thenThrow(new org.springframework.security.access.AccessDeniedException(
                        "Ressource hors de la tontine courante"));

        mockMvc.perform(get("/api/prets/99"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VISITEUR")
    void shouldReturn403WhenRoleCannotReadLoans() throws Exception {
        mockMvc.perform(get("/api/prets"))
                .andExpect(status().isForbidden());
    }

    private PretResponse exampleResponse() {
        return new PretResponse(30L, 20L, 7L, 10L, new BigDecimal("250000"), 3,
                LocalDate.of(2026, 8, 23), LocalDate.of(2026, 12, 31),
                StatutPret.ACTIF, 40L);
    }
}
