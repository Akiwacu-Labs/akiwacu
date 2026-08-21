package bi.ac.upg.akiwacu.tontine;

import bi.ac.upg.akiwacu.auth.JwtService;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.tontine.dto.TontineRequest;
import bi.ac.upg.akiwacu.tontine.dto.TontineResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TontineController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TontineController — endpoints /api/tontines")
class TontineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TontineService tontineService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("cas nominal — POST crée une tontine et renvoie 201")
    void shouldCreateTontineAndReturn201() throws Exception {
        // Arrange
        var requete = new TontineRequest("Twiyungunganye", "Épargne solidaire", LocalDate.of(2026, 8, 21),
                StatutTontine.ACTIVE);
        var reponse = new TontineResponse(12L, requete.nom(), requete.description(), requete.dateCreation(),
                requete.statut());
        when(tontineService.creer(any())).thenReturn(reponse);

        // Act & Assert : le contrôleur délègue puis expose la ressource créée et son URI.
        mockMvc.perform(post("/api/tontines").contentType("application/json")
                        .content(objectMapper.writeValueAsString(requete)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/tontines/12"))
                .andExpect(jsonPath("$.nom").value("Twiyungunganye"));
    }

    @Test
    @DisplayName("cas d'erreur — GET sur une tontine absente renvoie 404")
    void shouldReturn404WhenTontineDoesNotExist() throws Exception {
        // Le client reçoit un 404 explicite, pas une erreur serveur, pour un identifiant inexistant.
        when(tontineService.trouverParId(99L))
                .thenThrow(new RessourceIntrouvableException("Tontine introuvable : 99"));

        mockMvc.perform(get("/api/tontines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tontine introuvable : 99"));
    }

    @Test
    @DisplayName("cas de validation — POST sans nom renvoie 400")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        var requete = new TontineRequest("", "Description", LocalDate.of(2026, 8, 21), StatutTontine.ACTIVE);

        mockMvc.perform(post("/api/tontines").contentType("application/json")
                        .content(objectMapper.writeValueAsString(requete)))
                .andExpect(status().isBadRequest());
    }
}
