package bi.ac.upg.akiwacu.tontine.dto;

import bi.ac.upg.akiwacu.tontine.StatutTontine;

import java.time.LocalDate;

/** Représentation publique d'une tontine dans l'API REST. */
public record TontineResponse(
        Long id,
        String nom,
        String description,
        LocalDate dateCreation,
        StatutTontine statut
) {
}
