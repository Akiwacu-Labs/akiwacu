package bi.ac.upg.akiwacu.remboursement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RemboursementRequest(
        @NotNull Long pretId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
        @NotNull LocalDate dateRemboursement) {
}
