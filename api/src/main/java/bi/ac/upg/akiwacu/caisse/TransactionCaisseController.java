package bi.ac.upg.akiwacu.caisse;

import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseRequest;
import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseResponse;
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
    public ResponseEntity<TransactionCaisseResponse> enregistrer(
            @Valid @RequestBody TransactionCaisseRequest request) {
        return ResponseEntity.status(201).body(service.enregistrer(request));
    }

    @GetMapping
    public List<TransactionCaisseResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public TransactionCaisseResponse recuperer(@PathVariable Long id) {
        return service.recuperer(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
