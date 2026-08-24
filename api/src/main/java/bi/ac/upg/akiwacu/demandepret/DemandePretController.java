package bi.ac.upg.akiwacu.demandepret;

import bi.ac.upg.akiwacu.demandepret.dto.DemandePretRequest;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST uniquement : la création et R2/R6 vivent dans DemandePretService. */
@RestController
@RequestMapping("/api/demandes-pret")
public class DemandePretController {

    private final DemandePretService demandePretService;

    public DemandePretController(DemandePretService demandePretService) {
        this.demandePretService = demandePretService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MEMBRE')")
    @Operation(summary = "Soumettre une demande de prêt",
            description = "Soumet une demande sur le cycle actif et applique R2 et R6.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Demande créée"),
        @ApiResponse(responseCode = "400", description = "Champs invalides"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Membre introuvable"),
        @ApiResponse(responseCode = "409", description = "Cycle inactif ou plafond R6 dépassé")
    })
    public ResponseEntity<DemandePretResponse> demanderPret(
            @Valid @RequestBody DemandePretRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(demandePretService.demanderPret(requete));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEMBRE', 'COMMISSAIRE', 'TRESORIER', 'GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Lister les demandes de prêt de la tontine courante")
    public List<DemandePretResponse> lister() {
        return demandePretService.listerDemandes();
    }

    @GetMapping("/{demandePretId}")
    @PreAuthorize("hasAnyRole('MEMBRE', 'COMMISSAIRE', 'TRESORIER', 'GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Consulter une demande de prêt")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Demande trouvée"),
        @ApiResponse(responseCode = "404", description = "Demande introuvable ou hors tontine")
    })
    public DemandePretResponse trouver(@PathVariable Long demandePretId) {
        return demandePretService.trouverDemande(demandePretId);
    }
}
