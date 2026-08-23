package bi.ac.upg.akiwacu.adhesion;

import bi.ac.upg.akiwacu.adhesion.dto.AdhesionRequest;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionModificationRequest;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionResponse;
import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdhesionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdhesionControllerTest.MethodSecurityTestConfiguration.class)
class AdhesionControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration { }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AdhesionService adhesionService;
    @MockBean private JwtService jwtService;

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void shouldCreateAdhesion() throws Exception {
        var request = new AdhesionRequest(3L, 5L, LocalDate.of(2026, 8, 22));
        when(adhesionService.creer(any())).thenReturn(new AdhesionResponse(9L, 3L, 5L,
                request.dateAdhesion(), StatutAdhesion.ACTIVE));

        mockMvc.perform(post("/api/adhesions").contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void shouldRejectInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/adhesions").contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AdhesionRequest(3L, 5L, LocalDate.now()))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/adhesions").contentType("application/json")
                        .content("{\"membreId\":null,\"cycleId\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void shouldReturn404WhenServiceCannotFindAdhesion() throws Exception {
        when(adhesionService.recuperer(404L))
                .thenThrow(new RessourceIntrouvableException("Adhésion introuvable"));

        mockMvc.perform(get("/api/adhesions/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void shouldListAdhesions() throws Exception {
        when(adhesionService.lister()).thenReturn(List.of(
                new AdhesionResponse(9L, 3L, 5L, LocalDate.of(2026, 8, 22), StatutAdhesion.ACTIVE)));

        mockMvc.perform(get("/api/adhesions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void shouldGetAdhesion() throws Exception {
        when(adhesionService.recuperer(9L)).thenReturn(
                new AdhesionResponse(9L, 3L, 5L, LocalDate.of(2026, 8, 22), StatutAdhesion.ACTIVE));

        mockMvc.perform(get("/api/adhesions/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void shouldModifyAdhesion() throws Exception {
        var request = new AdhesionModificationRequest(LocalDate.of(2026, 8, 23), StatutAdhesion.CLOTUREE);
        when(adhesionService.modifier(org.mockito.ArgumentMatchers.eq(9L),
                org.mockito.ArgumentMatchers.eq(request))).thenReturn(
                new AdhesionResponse(9L, 3L, 5L, request.dateAdhesion(), request.statut()));

        mockMvc.perform(put("/api/adhesions/9").contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CLOTUREE"));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void shouldReturn404WhenModifyingUnknownAdhesion() throws Exception {
        when(adhesionService.modifier(any(Long.class), any(AdhesionModificationRequest.class)))
                .thenThrow(new RessourceIntrouvableException("Adhésion introuvable"));

        mockMvc.perform(put("/api/adhesions/404").contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new AdhesionModificationRequest(LocalDate.now(), StatutAdhesion.CLOTUREE))))
                .andExpect(status().isNotFound());
    }
}
