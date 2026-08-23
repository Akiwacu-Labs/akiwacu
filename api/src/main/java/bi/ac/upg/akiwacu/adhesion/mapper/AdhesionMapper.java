package bi.ac.upg.akiwacu.adhesion.mapper;

import bi.ac.upg.akiwacu.adhesion.Adhesion;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionResponse;
import org.springframework.stereotype.Component;

/** Conversion explicite de l'entité vers la réponse REST. */
@Component
public class AdhesionMapper {

    public AdhesionResponse versReponse(Adhesion adhesion) {
        return new AdhesionResponse(adhesion.getId(), adhesion.getMembre().getId(), adhesion.getCycle().getId(),
                adhesion.getDateAdhesion(), adhesion.getStatut());
    }
}
