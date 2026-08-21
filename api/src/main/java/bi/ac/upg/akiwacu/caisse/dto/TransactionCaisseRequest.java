package bi.ac.upg.akiwacu.caisse.dto;

import bi.ac.upg.akiwacu.caisse.SensTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCaisseRequest(
        Long cycleId,
        @NotNull SensTransaction sens,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        @NotBlank String motif,
        @NotNull LocalDate dateTransaction,
        String referenceOperation) {
}
