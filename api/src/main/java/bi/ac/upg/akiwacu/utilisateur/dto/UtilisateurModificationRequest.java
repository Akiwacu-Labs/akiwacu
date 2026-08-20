package bi.ac.upg.akiwacu.utilisateur.dto;

import bi.ac.upg.akiwacu.utilisateur.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Modification d'un utilisateur existant. Ni email ni mot de passe ici :
 * ce sont des changements plus sensibles, hors périmètre de cet endpoint.
 */
public record UtilisateurModificationRequest(

        @NotBlank(message = "le nom est obligatoire")
        String nom,

        @NotBlank(message = "le prénom est obligatoire")
        String prenom,

        String telephone,

        @NotEmpty(message = "au moins un rôle est obligatoire")
        Set<Role> roles,

        boolean actif) {
}
