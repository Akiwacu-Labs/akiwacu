package bi.ac.upg.akiwacu.caisse.dto;

import bi.ac.upg.akiwacu.caisse.SensTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCaisseResponse(
        Long id,
        Long tontineId,
        Long cycleId,
        SensTransaction sens,
        BigDecimal montant,
        String motif,
        LocalDate dateTransaction,
        Long valideParId,
        String referenceOperation) {
}
