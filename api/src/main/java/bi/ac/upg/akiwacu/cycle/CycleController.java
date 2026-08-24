package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.cycle.dto.CycleRequest;
import bi.ac.upg.akiwacu.cycle.dto.CycleResponse;
import bi.ac.upg.akiwacu.cycle.dto.CycleStatutRequest;
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
@RequestMapping("/api/cycles")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
public class CycleController {

    private final CycleService cycleService;

    public CycleController(CycleService cycleService) {
        this.cycleService = cycleService;
    }

    @PostMapping
    @Operation(summary = "Créer un cycle")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cycle créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Cycle ouvert déjà existant")})
    public ResponseEntity<CycleResponse> creer(@Valid @RequestBody CycleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cycleService.creer(request));
    }

    @GetMapping
    @Operation(summary = "Lister les cycles de la tontine courante")
    public List<CycleResponse> lister() { return cycleService.lister(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un cycle")
    public CycleResponse recuperer(@PathVariable Long id) { return cycleService.recuperer(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les paramètres d'un cycle")
    public CycleResponse modifier(@PathVariable Long id, @Valid @RequestBody CycleRequest request) {
        return cycleService.modifier(id, request);
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Changer le statut d'un cycle", description = "Applique les transitions R2/R3/R4.")
    public CycleResponse changerStatut(@PathVariable Long id, @Valid @RequestBody CycleStatutRequest request) {
        return cycleService.changerStatut(id, request.statut());
    }
}
