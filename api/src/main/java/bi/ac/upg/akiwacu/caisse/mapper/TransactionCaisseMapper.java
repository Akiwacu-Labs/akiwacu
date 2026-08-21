package bi.ac.upg.akiwacu.caisse.mapper;

import bi.ac.upg.akiwacu.caisse.TransactionCaisse;
import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionCaisseMapper {

    public TransactionCaisseResponse versReponse(TransactionCaisse transaction) {
        return new TransactionCaisseResponse(
                transaction.getId(),
                transaction.getTontine().getId(),
                transaction.getCycle() == null ? null : transaction.getCycle().getId(),
                transaction.getSens(),
                transaction.getMontant(),
                transaction.getMotif(),
                transaction.getDateTransaction(),
                transaction.getValidePar().getId(),
                transaction.getReferenceOperation());
    }
}
