package bi.ac.upg.akiwacu.membre.mapper;

import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.dto.MembreModificationRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * PROPRIÉTAIRE : Andy.
 * tontine et utilisateur ne sont jamais mappés automatiquement : le premier
 * vient de TenantContext (R1), le second est résolu (et vérifié dans la
 * même tontine) par MembreService avant d'être posé sur l'entité.
 */
@Mapper(componentModel = "spring")
public interface MembreMapper {

    @Mapping(source = "utilisateur.id", target = "utilisateurId")
    MembreResponse versReponse(Membre membre);

    @Mapping(target = "tontine", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "statut", ignore = true)
    Membre versEntite(MembreRequest requete);

    @Mapping(target = "tontine", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    void mettreAJour(@MappingTarget Membre membre, MembreModificationRequest requete);
}
