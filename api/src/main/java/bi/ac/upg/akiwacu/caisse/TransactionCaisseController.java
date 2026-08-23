package bi.ac.upg.akiwacu.caisse;

import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseRequest;
import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions-caisse")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'TRESORIER')")
public class TransactionCaisseController {

    private final TransactionCaisseService service;

    public TransactionCaisseController(TransactionCaisseService service) {
        this.service = service;
    }

    @PostMapping
        @Operation(summary = "Enregistrer une transaction de caisse",
            description = "Enregistre une entrée ou une sortie dans la caisse de la tontine courante. Applique R1, R2 et R5.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction enregistrée"),
            @ApiResponse(responseCode = "400", description = "Champs invalides"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
            @ApiResponse(responseCode = "404", description = "Tontine ou cycle introuvable"),
            @ApiResponse(responseCode = "409", description = "Cycle inactif")
        })
    public ResponseEntity<TransactionCaisseResponse> enregistrer(
            @Valid @RequestBody TransactionCaisseRequest request) {
        return ResponseEntity.status(201).body(service.enregistrer(request));
    }

    @GetMapping
        @Operation(summary = "Lister les transactions de caisse",
            description = "Renvoie les transactions de la tontine courante.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste renvoyée"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant")
        })
    public List<TransactionCaisseResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
        @Operation(summary = "Consulter une transaction de caisse")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction trouvée"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
            @ApiResponse(responseCode = "404", description = "Transaction introuvable")
        })
    public TransactionCaisseResponse recuperer(@PathVariable Long id) {
        return service.recuperer(id);
    }

    @DeleteMapping("/{id}")
        @Operation(summary = "Supprimer une transaction de caisse")
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaction supprimée"),
            @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
            @ApiResponse(responseCode = "404", description = "Transaction introuvable")
        })
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
