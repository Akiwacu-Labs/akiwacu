package bi.ac.upg.akiwacu.adhesion.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Données de création d'une adhésion dans le cycle courant du membre. */
public record AdhesionRequest(
        @NotNull(message = "le membre est obligatoire") Long membreId,
        @NotNull(message = "le cycle est obligatoire") Long cycleId,
        @NotNull(message = "la date d'adhésion est obligatoire") LocalDate dateAdhesion) {
}
