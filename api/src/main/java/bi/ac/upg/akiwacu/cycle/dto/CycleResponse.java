package bi.ac.upg.akiwacu.cycle.dto;

import bi.ac.upg.akiwacu.cycle.Periodicite;
import bi.ac.upg.akiwacu.cycle.StatutCycle;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CycleResponse(Long id, String libelle, LocalDate dateDebut, LocalDate dateFin,
                            BigDecimal montantCotisation, Periodicite periodicite,
                            StatutCycle statut, LocalDate dateCloture) {
}
