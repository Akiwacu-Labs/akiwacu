package bi.ac.upg.akiwacu.cycle.dto;

import bi.ac.upg.akiwacu.cycle.Periodicite;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CycleRequest(
        @NotBlank(message = "le libellé est obligatoire") String libelle,
        @NotNull(message = "la date de début est obligatoire") LocalDate dateDebut,
        @NotNull(message = "la date de fin est obligatoire") LocalDate dateFin,
        @NotNull(message = "le montant de cotisation est obligatoire")
        @DecimalMin(value = "0.01", message = "le montant doit être positif") BigDecimal montantCotisation,
        @NotNull(message = "la périodicité est obligatoire") Periodicite periodicite) {
}
