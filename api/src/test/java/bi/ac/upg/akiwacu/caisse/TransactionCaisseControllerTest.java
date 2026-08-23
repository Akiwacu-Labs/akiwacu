package bi.ac.upg.akiwacu.caisse;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseResponse;
import bi.ac.upg.akiwacu.common.GlobalExceptionHandler;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionCaisseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, TransactionCaisseControllerTest.SecuriteMethodeTestConfig.class})
@DisplayName("TransactionCaisseController — API REST")
class TransactionCaisseControllerTest {

        @TestConfiguration
        @EnableMethodSecurity
        static class SecuriteMethodeTestConfig {
        }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionCaisseService service;

        @MockBean
        private JwtService jwtService;

    @Test
    @WithMockUser(roles = "TRESORIER")
    @DisplayName("cas nominal — enregistre une transaction et renvoie 201")
    void shouldCreateTransaction() throws Exception {
        when(service.enregistrer(any())).thenReturn(new TransactionCaisseResponse(
                10L, 1L, 3L, SensTransaction.ENTREE, new BigDecimal("50000.00"),
                "Cotisation", LocalDate.of(2026, 8, 21), 7L, "COTISATION:42"));

        mockMvc.perform(post("/api/transactions-caisse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cycleId":3,"sens":"ENTREE","montant":50000,
                                 "motif":"Cotisation","dateTransaction":"2026-08-21"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    @DisplayName("erreur métier — renvoie 409")
    void shouldReturn409OnBusinessError() throws Exception {
        when(service.enregistrer(any()))
                .thenThrow(new RegleMetierException("R2 — cycle inactif"));

        mockMvc.perform(post("/api/transactions-caisse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cycleId":3,"sens":"ENTREE","montant":50000,
                                 "motif":"Cotisation","dateTransaction":"2026-08-21"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    @DisplayName("ressource absente — renvoie 404")
    void shouldReturn404WhenTransactionIsMissing() throws Exception {
        when(service.enregistrer(any()))
                .thenThrow(new RessourceIntrouvableException("Cycle introuvable"));

        mockMvc.perform(post("/api/transactions-caisse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cycleId":3,"sens":"ENTREE","montant":50000,
                                 "motif":"Cotisation","dateTransaction":"2026-08-21"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    @DisplayName("rôle insuffisant — renvoie 403")
    void shouldRejectMember() throws Exception {
        mockMvc.perform(post("/api/transactions-caisse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cycleId":3,"sens":"ENTREE","montant":50000,
                                 "motif":"Cotisation","dateTransaction":"2026-08-21"}
                                """))
                .andExpect(status().isForbidden());
    }
}
