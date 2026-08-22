package bi.ac.upg.akiwacu.utilisateur.dto;

import bi.ac.upg.akiwacu.utilisateur.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Création d'un utilisateur. tontineId n'apparaît jamais ici : il vient
 * de TenantContext, jamais du corps de la requête (R1).
 */
public record UtilisateurRequest(

        @NotBlank(message = "l'email est obligatoire")
        @Email(message = "l'email doit être valide")
        String email,

        @NotBlank(message = "le mot de passe est obligatoire")
        @Size(min = 8, message = "le mot de passe doit faire au moins 8 caractères")
        String motDePasse,

        @NotBlank(message = "le nom est obligatoire")
        String nom,

        @NotBlank(message = "le prénom est obligatoire")
        String prenom,

        String telephone,

        @NotEmpty(message = "au moins un rôle est obligatoire")
        Set<Role> roles) {
}
