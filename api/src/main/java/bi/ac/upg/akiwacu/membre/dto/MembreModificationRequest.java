package bi.ac.upg.akiwacu.membre.dto;

import bi.ac.upg.akiwacu.membre.StatutMembre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembreModificationRequest(

        String numeroMembre,

        @NotBlank(message = "le nom est obligatoire")
        String nom,

        @NotBlank(message = "le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "le téléphone est obligatoire")
        String telephone,

        @NotNull(message = "le statut est obligatoire")
        StatutMembre statut) {
}
