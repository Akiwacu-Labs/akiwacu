package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.cotisation.dto.CotisationBatchRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationModificationRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cotisations")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'TRESORIER')")
public class CotisationController {

    private final CotisationService cotisationService;

    public CotisationController(CotisationService cotisationService) {
        this.cotisationService = cotisationService;
    }

    @PostMapping
    @Operation(summary = "Enregistrer une cotisation", description = "R2, R5 et génération du reçu.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cotisation et reçu créés"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Cycle inactif ou règle métier violée")})
    public ResponseEntity<CotisationResponse> creer(@Valid @RequestBody CotisationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotisationService.creer(request));
    }

    @GetMapping
    @Operation(summary = "Lister les cotisations de la tontine courante")
    public List<CotisationResponse> lister() { return cotisationService.lister(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une cotisation")
    public CotisationResponse recuperer(@PathVariable Long id) { return cotisationService.recuperer(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une cotisation non verrouillée")
    public CotisationResponse modifier(@PathVariable Long id,
                                       @Valid @RequestBody CotisationModificationRequest request) {
        return cotisationService.modifier(id, request);
    }

    @PostMapping("/batch")
    @Operation(summary = "Enregistrer un lot de cotisations", description = "Transaction atomique avec un reçu par ligne.")
    public ResponseEntity<List<CotisationResponse>> creerLot(@Valid @RequestBody CotisationBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotisationService.creerLot(request));
    }
}
