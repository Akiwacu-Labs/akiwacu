package bi.ac.upg.akiwacu.caisse;

import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseRequest;
import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseResponse;
import bi.ac.upg.akiwacu.caisse.mapper.TransactionCaisseMapper;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.cycle.CycleRepository;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionCaisseService {

    private final TransactionCaisseRepository repository;
    private final TontineRepository tontineRepository;
    private final CycleRepository cycleRepository;
    private final CycleGuardService cycleGuardService;
    private final ValidateurCourantService validateurCourantService;
    private final TransactionCaisseMapper mapper;

    public TransactionCaisseService(TransactionCaisseRepository repository,
                                     TontineRepository tontineRepository,
                                     CycleRepository cycleRepository,
                                     CycleGuardService cycleGuardService,
                                     ValidateurCourantService validateurCourantService,
                                     TransactionCaisseMapper mapper) {
        this.repository = repository;
        this.tontineRepository = tontineRepository;
        this.cycleRepository = cycleRepository;
        this.cycleGuardService = cycleGuardService;
        this.validateurCourantService = validateurCourantService;
        this.mapper = mapper;
    }

    @Transactional
    public TransactionCaisseResponse enregistrer(TransactionCaisseRequest request) {
        Long tontineId = TenantContext.getTontineId();
        Tontine tontine = tontineRepository.findById(tontineId)
                .orElseThrow(() -> new RessourceIntrouvableException("Tontine introuvable"));
        Cycle cycle = null;
        if (request.cycleId() != null) {
            cycle = cycleRepository.findById(request.cycleId())
                    .orElseThrow(() -> new RessourceIntrouvableException("Cycle introuvable"));
            if (!cycle.getTontine().getId().equals(tontineId)) {
                throw new RessourceIntrouvableException("Cycle introuvable");
            }
            cycleGuardService.assertCycleActif(tontineId);
        }

        TransactionCaisse transaction = TransactionCaisse.builder()
                .tontine(tontine)
                .cycle(cycle)
                .sens(request.sens())
                .montant(request.montant())
                .motif(request.motif())
                .dateTransaction(request.dateTransaction())
                .validePar(validateurCourantService.obtenir())
                .referenceOperation(request.referenceOperation())
                .build();
        return mapper.versReponse(repository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionCaisseResponse> lister() {
        Long tontineId = TenantContext.getTontineId();
        return repository.findAll().stream()
                .filter(transaction -> transaction.getTontine().getId().equals(tontineId))
                .map(mapper::versReponse)
                .toList();
    }

            @Transactional(readOnly = true)
            public List<TransactionCaisseResponse> listerParCycle(Long cycleId) {
            Cycle cycle = cycleRepository.findById(cycleId)
                .filter(value -> value.getTontine().getId().equals(TenantContext.getTontineId()))
                .orElseThrow(() -> new RessourceIntrouvableException("Cycle introuvable"));
            return repository.findByCycleId(cycle.getId()).stream()
                .filter(transaction -> transaction.getTontine().getId().equals(
                    TenantContext.getTontineId()))
                .map(mapper::versReponse)
                .toList();
            }

            @Transactional(readOnly = true)
            public BigDecimal solde() {
            Long tontineId = TenantContext.getTontineId();
            return repository.findAll().stream()
                .filter(transaction -> transaction.getTontine().getId().equals(tontineId))
                .map(transaction -> transaction.getSens() == SensTransaction.ENTREE
                    ? transaction.getMontant()
                    : transaction.getMontant().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

    @Transactional(readOnly = true)
    public TransactionCaisseResponse recuperer(Long id) {
        TransactionCaisse transaction = trouver(id);
        verifierTontine(transaction);
        return mapper.versReponse(transaction);
    }

    @Transactional
    public TransactionCaisseResponse modifier(Long id, TransactionCaisseRequest request) {
        TransactionCaisse transaction = trouver(id);
        verifierTontine(transaction);
        if (transaction.getCycle() != null) {
            cycleGuardService.assertCycleActif(TenantContext.getTontineId());
        }
        transaction.setSens(request.sens());
        transaction.setMontant(request.montant());
        transaction.setMotif(request.motif());
        transaction.setDateTransaction(request.dateTransaction());
        transaction.setReferenceOperation(request.referenceOperation());
        return mapper.versReponse(repository.save(transaction));
    }

    @Transactional
    public void supprimer(Long id) {
        TransactionCaisse transaction = trouver(id);
        verifierTontine(transaction);
        repository.delete(transaction);
    }

    private TransactionCaisse trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Transaction de caisse introuvable"));
    }

    private void verifierTontine(TransactionCaisse transaction) {
        if (!transaction.getTontine().getId().equals(TenantContext.getTontineId())) {
            throw new RessourceIntrouvableException("Transaction de caisse introuvable");
        }
    }
}
