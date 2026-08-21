package bi.ac.upg.akiwacu.common;

import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleRepository;
import org.springframework.stereotype.Service;

/** Garde commune R2 : les opérations financières exigent un cycle ouvert. */
@Service
public class CycleGuardService {

    private final CycleRepository cycleRepository;

    public CycleGuardService(CycleRepository cycleRepository) {
        this.cycleRepository = cycleRepository;
    }

    public Cycle assertCycleActif(Long cycleId) {
        Cycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new RessourceIntrouvableException("Cycle introuvable"));
        if (!cycle.peutAccueillirOperation()) {
            throw new RegleMetierException("R2 — le cycle n'est pas actif");
        }
        return cycle;
    }
}