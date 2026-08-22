package bi.ac.upg.akiwacu.dashboard;

import bi.ac.upg.akiwacu.common.ControllerMethodSecurityTestConfig;
import bi.ac.upg.akiwacu.common.GlobalExceptionHandler;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.dashboard.dto.DashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ControllerMethodSecurityTestConfig.class})
@DisplayName("DashboardController — API REST")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService service;

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("cas nominal — renvoie les agrégats")
    void shouldReturnAggregates() throws Exception {
        when(service.agregats()).thenReturn(new DashboardResponse(
                3L, new BigDecimal("50000.00"), new BigDecimal("100000.00"),
                new BigDecimal("20000.00"), new BigDecimal("30000.00")));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("erreur métier — renvoie 409")
    void shouldReturn409OnBusinessError() throws Exception {
        when(service.agregats()).thenThrow(new RegleMetierException("R2 — cycle inactif"));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    @DisplayName("ressource absente — renvoie 404")
    void shouldReturn404WhenDataIsMissing() throws Exception {
        when(service.agregats()).thenThrow(new RessourceIntrouvableException("Tontine introuvable"));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("rôle insuffisant — renvoie 403")
    void shouldRejectMember() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isForbidden());
    }
}
