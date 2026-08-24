package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationBatchRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationModificationRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationResponse;
import bi.ac.upg.akiwacu.cotisation.mapper.CotisationMapper;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.recu.RecuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CotisationService {

    private final CotisationRepository cotisationRepository;
    private final MembreRepository membreRepository;
    private final CycleGuardService cycleGuardService;
    private final ValidateurCourantService validateurCourantService;
    private final RecuService recuService;
    private final CotisationMapper cotisationMapper;

    public CotisationService(CotisationRepository cotisationRepository, MembreRepository membreRepository,
                             CycleGuardService cycleGuardService,
                             ValidateurCourantService validateurCourantService,
                             RecuService recuService, CotisationMapper cotisationMapper) {
        this.cotisationRepository = cotisationRepository;
        this.membreRepository = membreRepository;
        this.cycleGuardService = cycleGuardService;
        this.validateurCourantService = validateurCourantService;
        this.recuService = recuService;
        this.cotisationMapper = cotisationMapper;
    }

    @Transactional
    public CotisationResponse creer(CotisationRequest request) {
        Cycle cycle = cycleGuardService.assertCycleActif(TenantContext.getTontineId());
        verifierCycleDemande(cycle, request.cycleId());
        Membre membre = trouverMembre(request.membreId());
        verifierMemeTontine(membre, cycle);
        Cotisation cotisation = Cotisation.builder().cycle(cycle).membre(membre)
                .montant(request.montant()).dateCotisation(request.dateCotisation())
                .modePaiement(request.modePaiement()).validePar(validateurCourantService.obtenir()).build();
        Cotisation saved = cotisationRepository.save(cotisation);
        recuService.genererPourCotisation(saved.getId());
        return cotisationMapper.versReponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CotisationResponse> lister() {
        return cotisationRepository.findAllByMembreTontineId(TenantContext.getTontineId()).stream()
                .map(cotisationMapper::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public CotisationResponse recuperer(Long id) {
        return cotisationMapper.versReponse(trouver(id));
    }

    @Transactional
    public CotisationResponse modifier(Long id, CotisationModificationRequest request) {
        Cycle cycleActif = cycleGuardService.assertCycleActif(TenantContext.getTontineId());
        Cotisation cotisation = trouver(id);
        if (!cycleActif.getId().equals(cotisation.getCycle().getId())) {
            throw new RegleMetierException("R2 : une cotisation d'un cycle inactif ne peut pas être modifiée");
        }
        if (cotisation.estVerrouillee()) {
            throw new RegleMetierException("R8 : une cotisation ayant un reçu ne peut pas être modifiée");
        }
        cotisation.setMontant(request.montant());
        cotisation.setDateCotisation(request.dateCotisation());
        cotisation.setModePaiement(request.modePaiement());
        return cotisationMapper.versReponse(cotisation);
    }

    @Transactional
    public List<CotisationResponse> creerLot(CotisationBatchRequest request) {
        return request.cotisations().stream().map(this::creer).toList();
    }

    private Cotisation trouver(Long id) {
        return cotisationRepository.findByIdAndMembreTontineId(id, TenantContext.getTontineId())
                .orElseThrow(() -> new RessourceIntrouvableException("Cotisation introuvable"));
    }

    private Membre trouverMembre(Long id) {
        return membreRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Membre introuvable"));
    }

    private void verifierCycleDemande(Cycle actif, Long cycleId) {
        if (!actif.getId().equals(cycleId)) {
            throw new RegleMetierException("R2 : la cotisation doit cibler le cycle actif");
        }
    }

    private void verifierMemeTontine(Membre membre, Cycle cycle) {
        Long tenant = TenantContext.getTontineId();
        if (!tenant.equals(membre.getTontine().getId()) || !tenant.equals(cycle.getTontine().getId())) {
            throw new RessourceIntrouvableException("Ressource hors de la tontine courante");
        }
    }
}
