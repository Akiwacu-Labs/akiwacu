package bi.ac.upg.akiwacu.remboursement;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.OperationVerrouilleeException;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.pret.Pret;
import bi.ac.upg.akiwacu.pret.PretRepository;
import bi.ac.upg.akiwacu.remboursement.dto.RemboursementRequest;
import bi.ac.upg.akiwacu.remboursement.dto.RemboursementResponse;
import bi.ac.upg.akiwacu.remboursement.mapper.RemboursementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RemboursementService {

    private final RemboursementRepository remboursementRepository;
    private final PretRepository pretRepository;
    private final CycleGuardService cycleGuardService;
    private final ValidateurCourantService validateurCourantService;
    private final RemboursementMapper mapper;

    public RemboursementService(RemboursementRepository remboursementRepository,
                                PretRepository pretRepository,
                                CycleGuardService cycleGuardService,
                                ValidateurCourantService validateurCourantService,
                                RemboursementMapper mapper) {
        this.remboursementRepository = remboursementRepository;
        this.pretRepository = pretRepository;
        this.cycleGuardService = cycleGuardService;
        this.validateurCourantService = validateurCourantService;
        this.mapper = mapper;
    }

    @Transactional
    public RemboursementResponse enregistrer(RemboursementRequest request) {
        Pret pret = trouverPret(request.pretId());
        verifierTontine(pret);
        cycleGuardService.assertCycleActif(TenantContext.getTontineId());
        if (request.montant().compareTo(pret.soldeRestant()) > 0) {
            throw new RegleMetierException("Le remboursement dépasse le solde restant du prêt");
        }

        Remboursement remboursement = Remboursement.builder()
                .pret(pret)
                .montant(request.montant())
                .dateRemboursement(request.dateRemboursement())
                .validePar(validateurCourantService.obtenir())
                .build();
        return mapper.versReponse(remboursementRepository.save(remboursement));
    }

    @Transactional(readOnly = true)
    public List<RemboursementResponse> lister() {
        return remboursementRepository.findAll().stream()
                .filter(remboursement -> appartientALaTontine(remboursement.getPret()))
                .map(mapper::versReponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RemboursementResponse recuperer(Long id) {
        Remboursement remboursement = trouver(id);
        verifierTontine(remboursement.getPret());
        return mapper.versReponse(remboursement);
    }

    @Transactional
    public void supprimer(Long id) {
        Remboursement remboursement = trouver(id);
        verifierTontine(remboursement.getPret());
        verifierNonVerrouille(remboursement);
        remboursementRepository.delete(remboursement);
    }

    private Pret trouverPret(Long id) {
        return pretRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Prêt introuvable"));
    }

    private Remboursement trouver(Long id) {
        return remboursementRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Remboursement introuvable"));
    }

    private void verifierTontine(Pret pret) {
        if (!appartientALaTontine(pret)) {
            throw new RessourceIntrouvableException("Prêt introuvable");
        }
    }

    private boolean appartientALaTontine(Pret pret) {
        return pret.getCycle().getTontine().getId().equals(TenantContext.getTontineId());
    }

    private void verifierNonVerrouille(Remboursement remboursement) {
        if (remboursement.isVerrouille() || remboursement.getRecu() != null) {
            throw new OperationVerrouilleeException("R8 — le remboursement possède déjà un reçu");
        }
    }
}
