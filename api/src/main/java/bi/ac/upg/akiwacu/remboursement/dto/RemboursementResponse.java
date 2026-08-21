package bi.ac.upg.akiwacu.remboursement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RemboursementResponse(
        Long id,
        Long pretId,
        BigDecimal montant,
        LocalDate dateRemboursement,
        Long valideParId,
        boolean verrouille) {
}
