package bi.ac.upg.akiwacu.adhesion;

import bi.ac.upg.akiwacu.adhesion.dto.AdhesionModificationRequest;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionRequest;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionResponse;
import bi.ac.upg.akiwacu.adhesion.mapper.AdhesionMapper;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleRepository;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdhesionService {

    private final AdhesionRepository adhesionRepository;
    private final MembreRepository membreRepository;
    private final CycleRepository cycleRepository;
    private final AdhesionMapper adhesionMapper;

    public AdhesionService(AdhesionRepository adhesionRepository, MembreRepository membreRepository,
                           CycleRepository cycleRepository, AdhesionMapper adhesionMapper) {
        this.adhesionRepository = adhesionRepository;
        this.membreRepository = membreRepository;
        this.cycleRepository = cycleRepository;
        this.adhesionMapper = adhesionMapper;
    }

    @Transactional
    public AdhesionResponse creer(AdhesionRequest requete) {
        Long tontineId = TenantContext.getTontineId();
        Membre membre = membreRepository.findById(requete.membreId())
                .orElseThrow(() -> new RessourceIntrouvableException("Membre introuvable"));
        Cycle cycle = cycleRepository.findById(requete.cycleId())
                .orElseThrow(() -> new RessourceIntrouvableException("Cycle introuvable"));
        verifierMemeTontine(membre, cycle, tontineId);
        if (adhesionRepository.existsByMembreIdAndCycleIdAndMembreTontineId(membre.getId(), cycle.getId(), tontineId)) {
            throw new RegleMetierException("Ce membre est déjà inscrit à ce cycle");
        }
        Adhesion adhesion = Adhesion.builder().membre(membre).cycle(cycle)
                .dateAdhesion(requete.dateAdhesion()).statut(StatutAdhesion.ACTIVE).build();
        return adhesionMapper.versReponse(adhesionRepository.save(adhesion));
    }

    @Transactional(readOnly = true)
    public List<AdhesionResponse> lister() {
        return adhesionRepository.findAllByMembreTontineId(TenantContext.getTontineId()).stream()
                .map(adhesionMapper::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public AdhesionResponse recuperer(Long id) {
        return adhesionMapper.versReponse(trouver(id));
    }

    @Transactional
    public AdhesionResponse modifier(Long id, AdhesionModificationRequest requete) {
        Adhesion adhesion = trouver(id);
        adhesion.setDateAdhesion(requete.dateAdhesion());
        adhesion.setStatut(requete.statut());
        return adhesionMapper.versReponse(adhesion);
    }

    private Adhesion trouver(Long id) {
        return adhesionRepository.findByIdAndMembreTontineId(id, TenantContext.getTontineId())
                .orElseThrow(() -> new RessourceIntrouvableException("Adhésion introuvable"));
    }

    private void verifierMemeTontine(Membre membre, Cycle cycle, Long tontineId) {
        if (!tontineId.equals(membre.getTontine().getId()) || !tontineId.equals(cycle.getTontine().getId())) {
            throw new RessourceIntrouvableException("Ressource hors de la tontine courante");
        }
    }
}
