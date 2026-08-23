package bi.ac.upg.akiwacu.dashboard;

import bi.ac.upg.akiwacu.dashboard.dto.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'TRESORIER')")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
        @Operation(summary = "Consulter le tableau de bord",
            description = "Renvoie les indicateurs de la tontine courante.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicateurs renvoyés"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
            @ApiResponse(responseCode = "404", description = "Tontine introuvable")
        })
    public DashboardResponse agregats() {
        return service.agregats();
    }
}
