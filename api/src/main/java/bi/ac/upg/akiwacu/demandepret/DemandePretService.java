package bi.ac.upg.akiwacu.demandepret;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretRequest;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretResponse;
import bi.ac.upg.akiwacu.demandepret.mapper.DemandePretMapper;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.pret.PretService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;

/**
 * PROPRIÉTAIRE : Gloria.
 * Les règles vivent dans les services : R2 est déléguée à Benitha, R6 à
 * PretService. Le tenant vient toujours du TenantContext (R1).
 */
@Service
public class DemandePretService {

    private final DemandePretRepository demandePretRepository;
    private final MembreRepository membreRepository;
    private final CycleGuardService cycleGuardService;
    private final PretService pretService;
    private final DemandePretMapper demandePretMapper;

    public DemandePretService(DemandePretRepository demandePretRepository,
                              MembreRepository membreRepository,
                              CycleGuardService cycleGuardService,
                              PretService pretService,
                              DemandePretMapper demandePretMapper) {
        this.demandePretRepository = demandePretRepository;
        this.membreRepository = membreRepository;
        this.cycleGuardService = cycleGuardService;
        this.pretService = pretService;
        this.demandePretMapper = demandePretMapper;
    }

    @Transactional
    public DemandePretResponse demanderPret(DemandePretRequest requete) {
        Long tontineId = TenantContext.getTontineId();
        Cycle cycleActif = cycleGuardService.assertCycleActif(tontineId);
        Membre membre = membreRepository.findById(requete.membreId())
                .orElseThrow(() -> new RessourceIntrouvableException("Membre introuvable"));

        if (!membre.getTontine().getId().equals(tontineId)) {
            throw new RessourceIntrouvableException("Membre introuvable");
        }

        pretService.verifierLimiteMontant(
                membre.getId(), cycleActif.getId(), requete.montantDemande());
        pretService.verifierEcheance(cycleActif, requete.dateEcheance());

        DemandePret demande = demandePretMapper.versEntite(requete);
        demande.setCycle(cycleActif);
        demande.setMembre(membre);
        demande.setDateDemande(LocalDate.now());
        demande.setStatut(StatutDemandePret.SOUMISE);

        return demandePretMapper.versReponse(demandePretRepository.save(demande));
    }

    @Transactional(readOnly = true)
    public List<DemandePretResponse> listerDemandes() {
        Long tontineId = TenantContext.getTontineId();
        return demandePretRepository.findAll().stream()
                .filter(demande -> demande.getMembre().getTontine().getId().equals(tontineId))
                .map(demandePretMapper::versReponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DemandePretResponse trouverDemande(Long demandePretId) {
        Long tontineId = TenantContext.getTontineId();
        DemandePret demande = demandePretRepository.findById(demandePretId)
                .orElseThrow(() -> new RessourceIntrouvableException("Demande de prêt introuvable"));
        if (!demande.getMembre().getTontine().getId().equals(tontineId)) {
            throw new AccessDeniedException("Ressource hors de la tontine courante");
        }
        return demandePretMapper.versReponse(demande);
    }
}
