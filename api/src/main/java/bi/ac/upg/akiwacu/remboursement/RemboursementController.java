package bi.ac.upg.akiwacu.remboursement;

import bi.ac.upg.akiwacu.remboursement.dto.RemboursementRequest;
import bi.ac.upg.akiwacu.remboursement.dto.RemboursementResponse;
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
@RequestMapping("/api/remboursements")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'TRESORIER')")
public class RemboursementController {

    private final RemboursementService service;

    public RemboursementController(RemboursementService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RemboursementResponse> enregistrer(@Valid @RequestBody RemboursementRequest request) {
        return ResponseEntity.status(201).body(service.enregistrer(request));
    }

    @GetMapping
    public List<RemboursementResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public RemboursementResponse recuperer(@PathVariable Long id) {
        return service.recuperer(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
