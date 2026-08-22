package bi.ac.upg.akiwacu.adhesion.dto;

import bi.ac.upg.akiwacu.adhesion.StatutAdhesion;

import java.time.LocalDate;

public record AdhesionResponse(
        Long id,
        Long membreId,
        Long cycleId,
        LocalDate dateAdhesion,
        StatutAdhesion statut) {
}
