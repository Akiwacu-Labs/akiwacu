package bi.ac.upg.akiwacu.pret.dto;

import bi.ac.upg.akiwacu.pret.StatutPret;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PretResponse(
        Long id,
        Long demandePretId,
        Long membreId,
        Long cycleId,
        BigDecimal montantAccorde,
        Integer dureeMois,
        LocalDate dateDeblocage,
        LocalDate dateEcheance,
        StatutPret statut,
        Long recuId) {
}
