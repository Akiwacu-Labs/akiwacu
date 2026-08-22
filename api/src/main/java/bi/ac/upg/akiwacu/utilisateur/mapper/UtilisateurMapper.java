package bi.ac.upg.akiwacu.utilisateur.mapper;

import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurModificationRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * PROPRIÉTAIRE : Andy.
 * tontine et motDePasse ne sont jamais mappés automatiquement : le premier
 * vient de TenantContext (R1), le second doit être haché avant d'entrer
 * dans l'entité. UtilisateurService les pose explicitement après mapping.
 */
@Mapper(componentModel = "spring")
public interface UtilisateurMapper {

    UtilisateurResponse versReponse(Utilisateur utilisateur);

    @Mapping(target = "tontine", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "actif", ignore = true)
    Utilisateur versEntite(UtilisateurRequest requete);

    @Mapping(target = "tontine", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "email", ignore = true)
    void mettreAJour(@MappingTarget Utilisateur utilisateur, UtilisateurModificationRequest requete);
}
