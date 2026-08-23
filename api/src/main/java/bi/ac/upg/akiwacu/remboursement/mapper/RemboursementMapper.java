package bi.ac.upg.akiwacu.remboursement.mapper;

import bi.ac.upg.akiwacu.remboursement.Remboursement;
import bi.ac.upg.akiwacu.remboursement.dto.RemboursementResponse;
import org.springframework.stereotype.Component;

@Component
public class RemboursementMapper {

    public RemboursementResponse versReponse(Remboursement remboursement) {
        return new RemboursementResponse(
                remboursement.getId(),
                remboursement.getPret().getId(),
                remboursement.getMontant(),
                remboursement.getDateRemboursement(),
                remboursement.getValidePar().getId(),
                remboursement.isVerrouille() || remboursement.getRecu() != null);
    }
}
