package bi.ac.upg.akiwacu.cycle.dto;

import bi.ac.upg.akiwacu.cycle.StatutCycle;
import jakarta.validation.constraints.NotNull;

public record CycleStatutRequest(
        @NotNull(message = "le statut cible est obligatoire") StatutCycle statut) {
}
