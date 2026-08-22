package bi.ac.upg.akiwacu.membre.dto;

import bi.ac.upg.akiwacu.membre.StatutMembre;

import java.time.LocalDate;

public record MembreResponse(
        Long id,
        String numeroMembre,
        String nom,
        String prenom,
        String telephone,
        LocalDate dateAdhesion,
        StatutMembre statut,
        Long utilisateurId) {
}
