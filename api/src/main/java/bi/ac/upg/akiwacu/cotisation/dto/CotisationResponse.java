package bi.ac.upg.akiwacu.cotisation.dto;

import bi.ac.upg.akiwacu.cotisation.ModePaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotisationResponse(Long id, Long membreId, Long cycleId, BigDecimal montant,
                                 LocalDate dateCotisation, ModePaiement modePaiement,
                                 Long valideParId, boolean verrouille, Long recuId) {
}
