package bi.ac.upg.akiwacu.vote.mapper;

import bi.ac.upg.akiwacu.vote.VoteCommissaire;
import bi.ac.upg.akiwacu.vote.dto.VoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** PROPRIÉTAIRE : Gloria. L'identité du commissaire n'est jamais mappée depuis la requête. */
@Mapper(componentModel = "spring")
public interface VoteCommissaireMapper {

    @Mapping(source = "demandePret.id", target = "demandePretId")
    @Mapping(source = "commissaire.id", target = "commissaireId")
    VoteResponse versReponse(VoteCommissaire vote);
}
