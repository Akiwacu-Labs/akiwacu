package bi.ac.upg.akiwacu.cycle;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(CycleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CycleControllerTest.MethodSecurityTestConfig.class)
class CycleControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Autowired MockMvc mockMvc;
    @MockBean CycleService cycleService;
    @MockBean JwtService jwtService;

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void gestionnaireMayListCycles() throws Exception {
        mockMvc.perform(get("/api/cycles")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void memberCannotManageCycles() throws Exception {
        mockMvc.perform(get("/api/cycles")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void managerMayCreateCycle() throws Exception {
        mockMvc.perform(post("/api/cycles").contentType(APPLICATION_JSON)
                        .content("""
                                {"libelle":"Cycle 2026","dateDebut":"2026-01-01","dateFin":"2026-12-31",
                                 "montantCotisation":100000,"periodicite":"MENSUELLE"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void managerMayReadOneCycle() throws Exception {
        mockMvc.perform(get("/api/cycles/1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void managerMayUpdateCycle() throws Exception {
        mockMvc.perform(put("/api/cycles/1").contentType(APPLICATION_JSON)
                        .content("""
                                {"libelle":"Cycle modifié","dateDebut":"2026-01-01","dateFin":"2026-12-31",
                                 "montantCotisation":100000,"periodicite":"MENSUELLE"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void managerMayChangeCycleStatus() throws Exception {
        mockMvc.perform(patch("/api/cycles/1/statut").contentType(APPLICATION_JSON)
                        .content("{\"statut\":\"GELE\"}"))
                .andExpect(status().isOk());
    }
}
