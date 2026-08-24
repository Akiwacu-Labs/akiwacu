package bi.ac.upg.akiwacu.cotisation.dto;

import bi.ac.upg.akiwacu.cotisation.ModePaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotisationModificationRequest(
        @NotNull(message = "le montant est obligatoire")
        @DecimalMin(value = "0.01", message = "le montant doit être positif") BigDecimal montant,
        @NotNull(message = "la date de cotisation est obligatoire") LocalDate dateCotisation,
        @NotNull(message = "le mode de paiement est obligatoire") ModePaiement modePaiement) {
}
