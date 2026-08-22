package bi.ac.upg.akiwacu.demandepret.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Données nécessaires pour soumettre une demande dans la tontine courante. */
public record DemandePretRequest(

        @NotNull(message = "le membre est obligatoire")
        Long membreId,

        @NotNull(message = "le montant demandé est obligatoire")
        @DecimalMin(value = "0.01", message = "le montant demandé doit être positif")
        BigDecimal montantDemande,

        @NotNull(message = "la durée est obligatoire")
        @Positive(message = "la durée doit être positive")
        Integer dureeMois,

        @NotBlank(message = "le motif est obligatoire")
        String motif) {
}
