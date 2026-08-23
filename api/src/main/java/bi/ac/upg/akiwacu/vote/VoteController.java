package bi.ac.upg.akiwacu.vote;

import bi.ac.upg.akiwacu.vote.dto.VoteRequest;
import bi.ac.upg.akiwacu.vote.dto.VoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST uniquement : l'identité et R4 vivent dans VoteService. */
@RestController
@RequestMapping("/api/demandes-pret/{demandePretId}/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    @PreAuthorize("hasRole('COMMISSAIRE')")
    @Operation(summary = "Voter sur une demande de prêt",
            description = "Enregistre le vote du commissaire authentifié et applique R4.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vote enregistré"),
        @ApiResponse(responseCode = "400", description = "Vote invalide"),
        @ApiResponse(responseCode = "403", description = "Commissaire hors tontine ou rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Demande introuvable"),
        @ApiResponse(responseCode = "409", description = "Commissaire déjà voté")
    })
    public ResponseEntity<VoteResponse> voter(@PathVariable Long demandePretId,
                                               @Valid @RequestBody VoteRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voteService.voter(demandePretId, requete));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEMBRE', 'COMMISSAIRE', 'TRESORIER', 'GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Lister les votes d'une demande de prêt")
    public List<VoteResponse> lister(@PathVariable Long demandePretId) {
        return voteService.lister(demandePretId);
    }
}
