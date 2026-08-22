package bi.ac.upg.akiwacu.utilisateur;

import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurModificationRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

/**
 * PROPRIÉTAIRE : Andy.
 * REST uniquement — la logique vit dans UtilisateurService.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    @Operation(summary = "Créer un utilisateur",
               description = "Crée un utilisateur dans la tontine courante (R1). Rôle ADMIN ou GESTIONNAIRE requis.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
        @ApiResponse(responseCode = "400", description = "Champs invalides"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurRequest requete) {
        UtilisateurResponse cree = utilisateurService.creer(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping
    @Operation(summary = "Lister les utilisateurs",
               description = "Renvoie les utilisateurs de la tontine courante (R1).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste renvoyée"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant")
    })
    public List<UtilisateurResponse> lister() {
        return utilisateurService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un utilisateur")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable ou hors de la tontine courante (R1)")
    })
    public UtilisateurResponse recuperer(@PathVariable Long id) {
        return utilisateurService.recuperer(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur",
               description = "Nom, prénom, téléphone, rôles et statut actif — pas l'email ni le mot de passe.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur modifié"),
        @ApiResponse(responseCode = "400", description = "Champs invalides"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable ou hors de la tontine courante (R1)")
    })
    public UtilisateurResponse modifier(@PathVariable Long id,
                                         @Valid @RequestBody UtilisateurModificationRequest requete) {
        return utilisateurService.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver un utilisateur",
               description = "Désactive le compte (actif=false) — jamais de suppression physique, "
                       + "l'utilisateur reste référencé par l'historique d'audit (R5).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Utilisateur désactivé"),
        @ApiResponse(responseCode = "403", description = "Rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable ou hors de la tontine courante (R1)")
    })
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
