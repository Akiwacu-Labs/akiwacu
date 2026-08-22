package bi.ac.upg.akiwacu.tontine.dto;

import bi.ac.upg.akiwacu.tontine.StatutTontine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Données du libre-service d'inscription : la tontine et son premier ADMIN
 * sont créés ensemble, avant qu'un TenantContext/JWT n'existe.
 */
public record TontineCreationRequest(
        @NotBlank(message = "Le nom de la tontine est obligatoire")
        @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
        String nom,
        String description,
        @NotNull(message = "La date de création est obligatoire")
        LocalDate dateCreation,
        @NotNull(message = "Le statut de la tontine est obligatoire")
        StatutTontine statut,
        @Valid
        @NotNull(message = "Les informations du premier administrateur sont obligatoires")
        AdministrateurCreation administrateur
) {

    public record AdministrateurCreation(
            @NotBlank(message = "L'email de l'administrateur est obligatoire")
            @Email(message = "L'email de l'administrateur doit être valide")
            String email,
            @NotBlank(message = "Le mot de passe de l'administrateur est obligatoire")
            @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
            String motDePasse,
            @NotBlank(message = "Le nom de l'administrateur est obligatoire")
            @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
            String nom,
            @NotBlank(message = "Le prénom de l'administrateur est obligatoire")
            @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
            String prenom,
            @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
            String telephone
    ) {
    }
}
