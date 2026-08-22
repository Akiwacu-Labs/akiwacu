package bi.ac.upg.akiwacu.tontine.dto;

import bi.ac.upg.akiwacu.tontine.StatutTontine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Données nécessaires à la création ou à la modification d'une tontine. */
public record TontineRequest(
        @NotBlank(message = "Le nom de la tontine est obligatoire")
        @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
        String nom,
        String description,
        @NotNull(message = "La date de création est obligatoire")
        LocalDate dateCreation,
        @NotNull(message = "Le statut de la tontine est obligatoire")
        StatutTontine statut
) {
}
