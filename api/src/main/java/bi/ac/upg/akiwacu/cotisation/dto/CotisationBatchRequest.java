package bi.ac.upg.akiwacu.cotisation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CotisationBatchRequest(
        @NotEmpty(message = "le lot ne peut pas être vide")
        List<@Valid CotisationRequest> cotisations) {
}
