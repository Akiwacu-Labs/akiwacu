package bi.ac.upg.akiwacu.pret.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Contrat du déblocage d'une demande approuvée. */
public record PretDisbursementRequest(
        @NotNull(message = "la demande de prêt est obligatoire")
        Long demandePretId,

        @NotNull(message = "l'échéance est obligatoire")
        @FutureOrPresent(message = "l'échéance ne peut pas être dans le passé")
        LocalDate dateEcheance) {
}
