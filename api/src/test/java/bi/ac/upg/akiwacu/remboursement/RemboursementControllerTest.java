package bi.ac.upg.akiwacu.remboursement;

import bi.ac.upg.akiwacu.common.ControllerMethodSecurityTestConfig;
import bi.ac.upg.akiwacu.common.GlobalExceptionHandler;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.remboursement.dto.RemboursementResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RemboursementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ControllerMethodSecurityTestConfig.class})
@DisplayName("RemboursementController — API REST")
class RemboursementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RemboursementService service;

    @Test
    @WithMockUser(roles = "TRESORIER")
    @DisplayName("cas nominal — enregistre un remboursement et renvoie 201")
    void shouldCreateRepayment() throws Exception {
        when(service.enregistrer(any())).thenReturn(new RemboursementResponse(
                8L, 5L, new BigDecimal("10000.00"), LocalDate.of(2026, 8, 21), 7L, false));

        mockMvc.perform(post("/api/remboursements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pretId":5,"montant":10000,
                                 "dateRemboursement":"2026-08-21"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    @DisplayName("erreur métier — renvoie 409")
    void shouldReturn409OnBusinessError() throws Exception {
        when(service.enregistrer(any()))
                .thenThrow(new RegleMetierException("Le remboursement dépasse le solde restant du prêt"));

        mockMvc.perform(post("/api/remboursements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pretId":5,"montant":10000,
                                 "dateRemboursement":"2026-08-21"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    @DisplayName("ressource absente — renvoie 404")
    void shouldReturn404WhenLoanIsMissing() throws Exception {
        when(service.enregistrer(any()))
                .thenThrow(new RessourceIntrouvableException("Prêt introuvable"));

        mockMvc.perform(post("/api/remboursements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pretId":5,"montant":10000,
                                 "dateRemboursement":"2026-08-21"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("rôle insuffisant — renvoie 403")
    void shouldRejectMember() throws Exception {
        mockMvc.perform(post("/api/remboursements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pretId":5,"montant":10000,
                                 "dateRemboursement":"2026-08-21"}
                                """))
                .andExpect(status().isForbidden());
    }
}
