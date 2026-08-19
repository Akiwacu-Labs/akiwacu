package bi.ac.upg.akiwacu.auth.dto;

import bi.ac.upg.akiwacu.utilisateur.Role;

import java.time.Instant;
import java.util.Set;

public record LoginResponse(
        String jeton,
        Instant expireA,
        Long utilisateurId,
        String nom,
        String prenom,
        Long tontineId,
        Set<Role> roles) {
}
