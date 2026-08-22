package bi.ac.upg.akiwacu.membre;

import bi.ac.upg.akiwacu.membre.dto.MembreModificationRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * PROPRIÉTAIRE : Andy.
 * REST uniquement — la logique vit dans MembreService. Pas de suppression :
 * un membre change de statut (SUSPENDU, SORTI), il ne disparaît jamais —
 * il reste référencé par ses cotisations, prêts et reçus.
 */
@RestController
@RequestMapping("/api/membres")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
public class MembreController {

    private final MembreService membreService;

    public MembreController(MembreService membreService) {
        this.membreService = membreService;
    }

    @PostMapping
    @Operation(summary = "Créer un membre",
               description = "Crée un membre dans la tontine courante (R1). Rôle ADMIN ou GESTIONNAIRE requis.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Membre créé"),
        @ApiResponse(responseCode = "400", description = "Champs invalides"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Utilisateur à lier introuvable"),
        @ApiResponse(responseCode = "409", description = "Numéro de membre déjà utilisé dans cette tontine")
    })
    public ResponseEntity<MembreResponse> creer(@Valid @RequestBody MembreRequest requete) {
        MembreResponse cree = membreService.creer(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping
    @Operation(summary = "Lister les membres",
               description = "Renvoie les membres de la tontine courante (R1).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste renvoyée"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant")
    })
    public List<MembreResponse> lister() {
        return membreService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un membre")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Membre trouvé"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Membre introuvable ou hors de la tontine courante (R1)")
    })
    public MembreResponse recuperer(@PathVariable Long id) {
        return membreService.recuperer(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un membre",
               description = "Numéro, nom, prénom, téléphone et statut.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Membre modifié"),
        @ApiResponse(responseCode = "400", description = "Champs invalides"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Membre introuvable ou hors de la tontine courante (R1)"),
        @ApiResponse(responseCode = "409", description = "Numéro de membre déjà utilisé dans cette tontine")
    })
    public MembreResponse modifier(@PathVariable Long id, @Valid @RequestBody MembreModificationRequest requete) {
        return membreService.modifier(id, requete);
    }
}
