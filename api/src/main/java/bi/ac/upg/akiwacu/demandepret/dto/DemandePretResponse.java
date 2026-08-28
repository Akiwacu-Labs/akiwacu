package bi.ac.upg.akiwacu.demandepret.dto;

import bi.ac.upg.akiwacu.demandepret.StatutDemandePret;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DemandePretResponse(
        Long id,
        Long membreId,
        Long cycleId,
        BigDecimal montantDemande,
        Integer dureeMois,
        String motif,
        LocalDate dateDemande,
        StatutDemandePret statut) {
}
