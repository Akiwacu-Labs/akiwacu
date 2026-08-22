package bi.ac.upg.akiwacu.membre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Création d'un membre. tontineId n'apparaît jamais ici : il vient de
 * TenantContext, jamais du corps de la requête (R1). numeroMembre est
 * optionnel — nullable dans le modèle de données (MODELE-DE-DONNEES.md).
 * utilisateurId est optionnel : un membre peut n'avoir aucun compte de
 * connexion (saisie manuelle par le gestionnaire).
 */
public record MembreRequest(

        String numeroMembre,

        @NotBlank(message = "le nom est obligatoire")
        String nom,

        @NotBlank(message = "le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "le téléphone est obligatoire")
        String telephone,

        @NotNull(message = "la date d'adhésion est obligatoire")
        LocalDate dateAdhesion,

        Long utilisateurId) {
}
