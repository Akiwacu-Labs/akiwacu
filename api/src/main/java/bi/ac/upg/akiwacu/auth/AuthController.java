package bi.ac.upg.akiwacu.auth;

import bi.ac.upg.akiwacu.auth.dto.LoginRequest;
import bi.ac.upg.akiwacu.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PROPRIÉTAIRE : Andy.
 * REST uniquement — la logique d'authentification vit dans AuthService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authentifier un utilisateur",
               description = "Vérifie l'email et le mot de passe, renvoie un JWT portant tontineId et roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentification réussie"),
        @ApiResponse(responseCode = "400", description = "Email ou mot de passe manquant/invalide"),
        @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest requete) {
        return ResponseEntity.ok(authService.login(requete));
    }
}
