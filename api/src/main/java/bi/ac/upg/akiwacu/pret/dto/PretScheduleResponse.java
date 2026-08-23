package bi.ac.upg.akiwacu.pret.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Projection de l'échéancier sans inventer une règle d'amortissement. */
public record PretScheduleResponse(
        Long pretId,
        Integer dureeMois,
        BigDecimal montantDu,
        BigDecimal soldeRestant,
        LocalDate dateDeblocage,
        LocalDate dateEcheance) {
}
