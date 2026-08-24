package bi.ac.upg.akiwacu.remboursement;

import bi.ac.upg.akiwacu.remboursement.dto.RemboursementRequest;
import bi.ac.upg.akiwacu.remboursement.dto.RemboursementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/remboursements")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'TRESORIER')")
public class RemboursementController {

    private final RemboursementService service;

    public RemboursementController(RemboursementService service) {
        this.service = service;
    }

    @PostMapping
        @Operation(summary = "Enregistrer un remboursement",
            description = "Enregistre un remboursement sur un prêt de la tontine courante. Applique R1, R2 et R5.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Remboursement enregistré"),
            @ApiResponse(responseCode = "400", description = "Champs invalides"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
            @ApiResponse(responseCode = "404", description = "Prêt introuvable"),
            @ApiResponse(responseCode = "409", description = "Cycle inactif ou solde dépassé")
        })
    public ResponseEntity<RemboursementResponse> enregistrer(@Valid @RequestBody RemboursementRequest request) {
        return ResponseEntity.status(201).body(service.enregistrer(request));
    }

    @GetMapping
        @Operation(summary = "Lister les remboursements",
            description = "Renvoie les remboursements de la tontine courante.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste renvoyée"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant")
        })
    public List<RemboursementResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
        @Operation(summary = "Consulter un remboursement")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remboursement trouvé"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
            @ApiResponse(responseCode = "404", description = "Remboursement introuvable")
        })
    public RemboursementResponse recuperer(@PathVariable Long id) {
        return service.recuperer(id);
    }

    @GetMapping("/pret/{pretId}")
    @Operation(summary = "Lister les remboursements d'un prêt")
    public List<RemboursementResponse> listerParPret(@PathVariable Long pretId) {
        return service.listerParPret(pretId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un remboursement")
    public RemboursementResponse modifier(@PathVariable Long id,
                                          @Valid @RequestBody RemboursementRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
        @Operation(summary = "Supprimer un remboursement")
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Remboursement supprimé"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
            @ApiResponse(responseCode = "404", description = "Remboursement introuvable"),
            @ApiResponse(responseCode = "409", description = "Remboursement verrouillé par un reçu")
        })
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
