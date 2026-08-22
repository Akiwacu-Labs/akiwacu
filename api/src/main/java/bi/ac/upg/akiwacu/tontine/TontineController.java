package bi.ac.upg.akiwacu.tontine;

import bi.ac.upg.akiwacu.tontine.dto.TontineRequest;
import bi.ac.upg.akiwacu.tontine.dto.TontineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Endpoints REST du domaine tontine ; la logique métier reste dans TontineService. */
@RestController
@RequestMapping("/api/tontines")
@RequiredArgsConstructor
public class TontineController {

    private final TontineService tontineService;

    @GetMapping
    @Operation(summary = "Lister les tontines")
    public List<TontineResponse> lister() {
        return tontineService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une tontine")
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Tontine introuvable"))
    public TontineResponse trouverParId(@PathVariable Long id) {
        return tontineService.trouverParId(id);
    }

    @PostMapping
    @Operation(summary = "Créer une tontine")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tontine créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Nom de tontine déjà utilisé")
    })
    public ResponseEntity<TontineResponse> creer(@Valid @RequestBody TontineRequest requete) {
        var reponse = tontineService.creer(requete);
        URI emplacement = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(reponse.id()).toUri();
        return ResponseEntity.created(emplacement).body(reponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier une tontine")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Tontine introuvable"),
            @ApiResponse(responseCode = "409", description = "Nom de tontine déjà utilisé")
    })
    public TontineResponse modifier(@PathVariable Long id, @Valid @RequestBody TontineRequest requete) {
        return tontineService.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une tontine")
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Tontine introuvable"))
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        tontineService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
