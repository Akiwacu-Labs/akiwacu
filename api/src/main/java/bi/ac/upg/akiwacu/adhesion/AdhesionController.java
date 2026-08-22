package bi.ac.upg.akiwacu.adhesion;

import bi.ac.upg.akiwacu.adhesion.dto.AdhesionModificationRequest;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionRequest;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adhesions")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
public class AdhesionController {

    private final AdhesionService adhesionService;

    public AdhesionController(AdhesionService adhesionService) {
        this.adhesionService = adhesionService;
    }

    @PostMapping
    @Operation(summary = "Créer une adhésion")
    public ResponseEntity<AdhesionResponse> creer(@Valid @RequestBody AdhesionRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adhesionService.creer(requete));
    }

    @GetMapping
    @Operation(summary = "Lister les adhésions de la tontine courante")
    public List<AdhesionResponse> lister() {
        return adhesionService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une adhésion")
    public AdhesionResponse recuperer(@PathVariable Long id) {
        return adhesionService.recuperer(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier le statut ou la date d'une adhésion")
    public AdhesionResponse modifier(@PathVariable Long id, @Valid @RequestBody AdhesionModificationRequest requete) {
        return adhesionService.modifier(id, requete);
    }
}
