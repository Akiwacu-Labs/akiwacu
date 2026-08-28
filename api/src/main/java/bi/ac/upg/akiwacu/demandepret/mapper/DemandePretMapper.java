package bi.ac.upg.akiwacu.demandepret.mapper;

import bi.ac.upg.akiwacu.demandepret.DemandePret;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretRequest;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * PROPRIÉTAIRE : Gloria.
 * Le cycle, le membre, le statut et la date sont posés par le service :
 * ils ne viennent jamais directement du corps de la requête.
 */
@Mapper(componentModel = "spring")
public interface DemandePretMapper {

    @Mapping(source = "membre.id", target = "membreId")
    @Mapping(source = "cycle.id", target = "cycleId")
    DemandePretResponse versReponse(DemandePret demande);

    @Mapping(target = "cycle", ignore = true)
    @Mapping(target = "membre", ignore = true)
    @Mapping(target = "dateDemande", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "votes", ignore = true)
    DemandePret versEntite(DemandePretRequest requete);
}
