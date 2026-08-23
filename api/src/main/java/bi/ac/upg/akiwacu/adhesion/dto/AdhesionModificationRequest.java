package bi.ac.upg.akiwacu.adhesion.dto;

import bi.ac.upg.akiwacu.adhesion.StatutAdhesion;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdhesionModificationRequest(
        @NotNull(message = "la date d'adhésion est obligatoire") LocalDate dateAdhesion,
        @NotNull(message = "le statut est obligatoire") StatutAdhesion statut) {
}
