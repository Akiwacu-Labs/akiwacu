package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.auth.JwtService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(CotisationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CotisationControllerTest.MethodSecurityTestConfig.class)
class CotisationControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Autowired MockMvc mockMvc;
    @MockBean CotisationService cotisationService;
    @MockBean JwtService jwtService;

    @Test
    @WithMockUser(roles = "TRESORIER")
    void treasurerMayListContributions() throws Exception {
        mockMvc.perform(get("/api/cotisations")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void memberCannotManageContributions() throws Exception {
        mockMvc.perform(get("/api/cotisations")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    void treasurerMayCreateContribution() throws Exception {
        mockMvc.perform(post("/api/cotisations").contentType(APPLICATION_JSON)
                        .content("""
                                {"membreId":1,"cycleId":1,"montant":100000,
                                 "dateCotisation":"2026-01-01","modePaiement":"ESPECES"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    void treasurerMayReadOneContribution() throws Exception {
        mockMvc.perform(get("/api/cotisations/1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    void treasurerMayUpdateContribution() throws Exception {
        mockMvc.perform(put("/api/cotisations/1").contentType(APPLICATION_JSON)
                        .content("""
                                {"montant":120000,"dateCotisation":"2026-01-02",
                                 "modePaiement":"VIREMENT"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    void treasurerMayCreateContributionBatch() throws Exception {
        mockMvc.perform(post("/api/cotisations/batch").contentType(APPLICATION_JSON)
                        .content("""
                                {"cotisations":[{"membreId":1,"cycleId":1,"montant":100000,
                                 "dateCotisation":"2026-01-01","modePaiement":"ESPECES"}]}
                                """))
                .andExpect(status().isCreated());
    }
}
