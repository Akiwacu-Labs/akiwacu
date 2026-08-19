package bi.ac.upg.akiwacu.auth;

import bi.ac.upg.akiwacu.auth.dto.LoginResponse;
import bi.ac.upg.akiwacu.common.exception.IdentifiantsInvalidesException;
import bi.ac.upg.akiwacu.utilisateur.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// addFilters = false : la chaîne de sécurité (JWT, STATELESS...) est testée
// séparément dans JwtServiceTest ; ici on vérifie uniquement le contrôleur.
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController — POST /api/auth/login")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // JwtAuthenticationFilter est un @Component Filter : @WebMvcTest l'instancie
    // même avec addFilters = false, donc son constructeur exige un JwtService.
    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("cas nominal — identifiants valides, renvoie 200 et le jeton")
    void shouldReturn200WithTokenOnValidCredentials() throws Exception {
        // Arrange
        var reponse = new LoginResponse("jeton.simule", Instant.now().plusSeconds(3600),
                1L, "Nkurunziza", "Alice", 2L, Set.of(Role.MEMBRE));
        when(authService.login(any())).thenReturn(reponse);
        String corps = objectMapper.writeValueAsString(
                new bi.ac.upg.akiwacu.auth.dto.LoginRequest("membre@akiwacu.bi", "motdepasse"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(corps))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jeton").value("jeton.simule"));
    }

    @Test
    @DisplayName("cas d'erreur métier — identifiants incorrects, renvoie 401")
    void shouldReturn401OnInvalidCredentials() throws Exception {
        // Arrange
        when(authService.login(any())).thenThrow(new IdentifiantsInvalidesException("Email ou mot de passe incorrect"));
        String corps = objectMapper.writeValueAsString(
                new bi.ac.upg.akiwacu.auth.dto.LoginRequest("membre@akiwacu.bi", "mauvais"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(corps))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("cas de validation — email manquant, renvoie 400 sans appeler le service")
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        // Arrange : email vide, viole @NotBlank sur LoginRequest.
        String corps = objectMapper.writeValueAsString(
                new bi.ac.upg.akiwacu.auth.dto.LoginRequest("", "motdepasse"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(corps))
                .andExpect(status().isBadRequest());
    }
}
