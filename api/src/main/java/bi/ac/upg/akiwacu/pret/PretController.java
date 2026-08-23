package bi.ac.upg.akiwacu.pret;

import bi.ac.upg.akiwacu.pret.dto.PretDisbursementRequest;
import bi.ac.upg.akiwacu.pret.dto.PretResponse;
import bi.ac.upg.akiwacu.pret.dto.PretScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints de déblocage et de consultation des prêts. */
@RestController
@RequestMapping("/api/prets")
public class PretController {

    private final PretService pretService;

    public PretController(PretService pretService) {
        this.pretService = pretService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRESORIER')")
    @Operation(summary = "Débloquer une demande approuvée")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Prêt créé et débloqué"),
        @ApiResponse(responseCode = "400", description = "Champs invalides"),
        @ApiResponse(responseCode = "403", description = "Rôle ou tontine insuffisant"),
        @ApiResponse(responseCode = "404", description = "Demande introuvable"),
        @ApiResponse(responseCode = "409", description = "Cycle, statut ou échéance incompatible")
    })
    public ResponseEntity<PretResponse> debloquer(@Valid @RequestBody PretDisbursementRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pretService.debloquerPret(requete));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEMBRE', 'COMMISSAIRE', 'TRESORIER', 'GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Lister les prêts de la tontine courante")
    public List<PretResponse> lister() {
        return pretService.listerPrets();
    }

    @GetMapping("/{pretId}")
    @PreAuthorize("hasAnyRole('MEMBRE', 'COMMISSAIRE', 'TRESORIER', 'GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Consulter le détail et l'échéance d'un prêt")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prêt trouvé"),
        @ApiResponse(responseCode = "403", description = "Prêt hors tontine"),
        @ApiResponse(responseCode = "404", description = "Prêt introuvable")
    })
    public PretResponse trouver(@PathVariable Long pretId) {
        return pretService.trouverPret(pretId);
    }

    @GetMapping("/{pretId}/echeancier")
    @PreAuthorize("hasAnyRole('MEMBRE', 'COMMISSAIRE', 'TRESORIER', 'GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Consulter l'échéancier d'un prêt")
    public PretScheduleResponse echeancier(@PathVariable Long pretId) {
        return pretService.echeancier(pretId);
    }
}
