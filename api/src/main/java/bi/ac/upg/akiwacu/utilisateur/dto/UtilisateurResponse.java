package bi.ac.upg.akiwacu.utilisateur.dto;

import bi.ac.upg.akiwacu.utilisateur.Role;

import java.util.Set;

public record UtilisateurResponse(
        Long id,
        String email,
        String nom,
        String prenom,
        String telephone,
        boolean actif,
        Set<Role> roles) {
}
