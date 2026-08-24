package bi.ac.upg.akiwacu.cotisation.mapper;

import bi.ac.upg.akiwacu.cotisation.Cotisation;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationResponse;
import org.springframework.stereotype.Component;

@Component
public class CotisationMapper {

    public CotisationResponse versReponse(Cotisation cotisation) {
        return new CotisationResponse(cotisation.getId(), cotisation.getMembre().getId(),
                cotisation.getCycle().getId(), cotisation.getMontant(), cotisation.getDateCotisation(),
                cotisation.getModePaiement(), cotisation.getValidePar().getId(),
                cotisation.isVerrouille(), cotisation.getRecu() == null ? null : cotisation.getRecu().getId());
    }
}
