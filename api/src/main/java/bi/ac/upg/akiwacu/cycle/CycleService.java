package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.dto.CycleRequest;
import bi.ac.upg.akiwacu.cycle.dto.CycleResponse;
import bi.ac.upg.akiwacu.cycle.mapper.CycleMapper;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CycleService {

    private final CycleRepository cycleRepository;
    private final TontineRepository tontineRepository;
    private final CycleMapper cycleMapper;
    private final ObjectProvider<CycleActiveLoanPort> activeLoanPort;

    public CycleService(CycleRepository cycleRepository, TontineRepository tontineRepository,
                        CycleMapper cycleMapper, ObjectProvider<CycleActiveLoanPort> activeLoanPort) {
        this.cycleRepository = cycleRepository;
        this.tontineRepository = tontineRepository;
        this.cycleMapper = cycleMapper;
        this.activeLoanPort = activeLoanPort;
    }

    @Transactional
    public CycleResponse creer(CycleRequest request) {
        Long tontineId = TenantContext.getTontineId();
        verifierDates(request);
        if (cycleRepository.existsByTontineIdAndStatut(tontineId, StatutCycle.OUVERT)) {
            throw new RegleMetierException("Un seul cycle peut être ouvert par tontine");
        }
        Cycle cycle = cycleMapper.versEntite(request);
        cycle.setTontine(tontineRepository.findById(tontineId)
                .orElseThrow(() -> new RessourceIntrouvableException("Tontine introuvable")));
        cycle.setStatut(StatutCycle.OUVERT);
        return cycleMapper.versReponse(cycleRepository.save(cycle));
    }

    @Transactional(readOnly = true)
    public List<CycleResponse> lister() {
        return cycleRepository.findAllByTontineId(TenantContext.getTontineId()).stream()
                .map(cycleMapper::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public CycleResponse recuperer(Long id) {
        return cycleMapper.versReponse(trouver(id));
    }

    @Transactional
    public CycleResponse modifier(Long id, CycleRequest request) {
        verifierDates(request);
        Cycle cycle = trouver(id);
        cycleMapper.mettreAJour(cycle, request);
        return cycleMapper.versReponse(cycle);
    }

    @Transactional
    public CycleResponse changerStatut(Long id, StatutCycle cible) {
        Cycle cycle = trouver(id);
        StatutCycle precedent = cycle.getStatut();
        if (precedent == cible) {
            throw new RegleMetierException("Le cycle possède déjà ce statut");
        }
        if (precedent == StatutCycle.CLOTURE) {
            throw new RegleMetierException("Un cycle clôturé ne peut pas être rouvert");
        }
        if (precedent == StatutCycle.OUVERT && cible != StatutCycle.GELE) {
            throw new RegleMetierException("Transition de cycle non autorisée : un cycle ouvert doit d'abord être gelé");
        }
        if (precedent == StatutCycle.GELE && cible != StatutCycle.CLOTURE
                && cible != StatutCycle.OUVERT) {
            throw new RegleMetierException("Transition de cycle non autorisée");
        }
        if (precedent == StatutCycle.GELE && cible == StatutCycle.OUVERT
                && cycleRepository.existsByTontineIdAndStatut(
                TenantContext.getTontineId(), StatutCycle.OUVERT)) {
            throw new RegleMetierException("Un seul cycle peut être ouvert par tontine");
        }
        if (cible == StatutCycle.CLOTURE) {
            CycleActiveLoanPort port = activeLoanPort.getIfAvailable();
            if (port == null) {
                throw new RegleMetierException(
                        "R3 : clôture indisponible tant que l'adaptateur des prêts actifs n'est pas branché");
            }
            if (port.existePretActif(cycle.getId(), TenantContext.getTontineId())) {
                throw new RegleMetierException("R3 : impossible de clôturer un cycle avec des prêts actifs");
            }
            cycle.setDateCloture(LocalDate.now());
        }
        cycle.setStatut(cible);
        return cycleMapper.versReponse(cycle);
    }

    private Cycle trouver(Long id) {
        return cycleRepository.findByIdAndTontineId(id, TenantContext.getTontineId())
                .orElseThrow(() -> new RessourceIntrouvableException("Cycle introuvable"));
    }

    private void verifierDates(CycleRequest request) {
        if (!request.dateFin().isAfter(request.dateDebut())) {
            throw new RegleMetierException("La date de fin doit être postérieure à la date de début");
        }
    }
}
