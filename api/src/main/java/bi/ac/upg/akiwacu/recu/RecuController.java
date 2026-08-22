package bi.ac.upg.akiwacu.recu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Téléchargement du reçu généré, affiché directement dans le navigateur. */
@RestController
@RequestMapping("/api/recus")
@Tag(name = "Reçus")
public class RecuController {

    private final RecuService recuService;

    public RecuController(RecuService recuService) {
        this.recuService = recuService;
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Télécharger un reçu PDF")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        byte[] contenu = recuService.telecharger(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"recu-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenu);
    }
}
