package bi.ac.upg.akiwacu.pret;

import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.demandepret.DemandePret;
import bi.ac.upg.akiwacu.demandepret.DemandePretRepository;
import bi.ac.upg.akiwacu.demandepret.StatutDemandePret;
import bi.ac.upg.akiwacu.recu.RecuService;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.pret.dto.PretResponse;
import bi.ac.upg.akiwacu.pret.dto.PretDisbursementRequest;
import bi.ac.upg.akiwacu.pret.dto.PretScheduleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PROPRIÉTAIRE : Gloria.
 * Porte les règles métier liées au montant demandé d'un prêt.
 */
@Service
public class PretService {

    private static final BigDecimal MULTIPLICATEUR_R6 = BigDecimal.valueOf(3);

    private final CotisationRepository cotisationRepository;
    private final PretRepository pretRepository;
    private final DemandePretRepository demandePretRepository;
    private final CycleGuardService cycleGuardService;
    private final ValidateurCourantService validateurCourantService;
    private final RecuService recuService;

    public PretService(CotisationRepository cotisationRepository,
                       PretRepository pretRepository,
                       DemandePretRepository demandePretRepository,
                       CycleGuardService cycleGuardService,
                       ValidateurCourantService validateurCourantService,
                       RecuService recuService) {
        this.cotisationRepository = cotisationRepository;
        this.pretRepository = pretRepository;
        this.demandePretRepository = demandePretRepository;
        this.cycleGuardService = cycleGuardService;
        this.validateurCourantService = validateurCourantService;
        this.recuService = recuService;
    }

    /**
     * R6 — le capital demandé ne dépasse pas trois fois les cotisations
     * du membre sur le cycle actif. Les prêts en cours ne sont pas déduits.
     */
    @Transactional(readOnly = true)
    public void verifierLimiteMontant(Long membreId, Long cycleId, BigDecimal montantDemande) {
        BigDecimal epargne = cotisationRepository.sommeParMembreEtCycle(membreId, cycleId);
        BigDecimal plafond = epargne.multiply(MULTIPLICATEUR_R6);

        if (montantDemande.compareTo(plafond) > 0) {
            throw new RegleMetierException(
                    "R6 : le montant demandé (%s BIF) dépasse trois fois l'épargne du membre (%s BIF)"
                            .formatted(montantDemande, epargne));
        }
    }

    /** R7 — l'échéance demandée reste dans les bornes du cycle. */
    @Transactional(readOnly = true)
    public void verifierEcheance(Cycle cycle, LocalDate dateEcheance) {
        if (dateEcheance.isAfter(cycle.getDateFin())) {
            throw new RegleMetierException(
                    "R7 : l'échéance (%s) dépasse la fin du cycle (%s)"
                            .formatted(dateEcheance, cycle.getDateFin()));
        }
    }

    /**
     * R3/R5 — le trésorier débloque une demande approuvée et crée le prêt.
     * La date d'échéance est portée par le contrat de déblocage car le schéma
     * existant ne stocke pas encore d'échéance sur demandes_pret.
     */
    @Transactional
    public PretResponse debloquerPret(PretDisbursementRequest requete) {
        Long tontineId = TenantContext.getTontineId();
        Cycle cycleActif = cycleGuardService.assertCycleActif(tontineId);
        DemandePret demande = demandePretRepository.findById(requete.demandePretId())
                .orElseThrow(() -> new RessourceIntrouvableException("Demande de prêt introuvable"));

        verifierTenant(demande, tontineId);
        if (demande.getStatut() != StatutDemandePret.APPROUVEE) {
            throw new RegleMetierException("Le prêt ne peut être débloqué que pour une demande approuvée");
        }
        if (!demande.getCycle().getId().equals(cycleActif.getId())) {
            throw new RegleMetierException("R3 : la demande n'appartient pas au cycle actif");
        }
        verifierEcheance(cycleActif, requete.dateEcheance());
        if (pretRepository.findByDemandePretId(demande.getId()).isPresent()) {
            throw new RegleMetierException("Un prêt existe déjà pour cette demande");
        }

        Utilisateur validateur = validateurCourantService.obtenir();
        verifierTenant(validateur.getTontine().getId(), tontineId);
        Pret pret = Pret.builder()
                .demandePret(demande)
                .membre(demande.getMembre())
                .cycle(cycleActif)
                .montantAccorde(demande.getMontantDemande())
                .dureeMois(demande.getDureeMois())
                .dateDeblocage(LocalDate.now())
                .dateEcheance(requete.dateEcheance())
                .statut(StatutPret.ACTIF)
                .validePar(validateur)
                .build();
        Pret saved = pretRepository.save(pret);
        demande.setStatut(StatutDemandePret.DEBLOQUEE);
        demandePretRepository.save(demande);
        recuService.genererPourPret(saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PretResponse> listerPrets() {
        Long tontineId = TenantContext.getTontineId();
        return pretRepository.findByMembreTontineId(tontineId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PretResponse trouverPret(Long pretId) {
        Long tontineId = TenantContext.getTontineId();
        Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RessourceIntrouvableException("Prêt introuvable"));
        verifierTenant(pret.getMembre().getTontine().getId(), tontineId);
        return toResponse(pret);
    }

    @Transactional(readOnly = true)
    public PretScheduleResponse echeancier(Long pretId) {
        Long tontineId = TenantContext.getTontineId();
        Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RessourceIntrouvableException("Prêt introuvable"));
        verifierTenant(pret.getMembre().getTontine().getId(), tontineId);
        return new PretScheduleResponse(pret.getId(), pret.getDureeMois(), pret.montantDu(),
                pret.soldeRestant(), pret.getDateDeblocage(), pret.getDateEcheance());
    }

    private PretResponse toResponse(Pret pret) {
        return new PretResponse(pret.getId(), pret.getDemandePret().getId(),
                pret.getMembre().getId(), pret.getCycle().getId(), pret.getMontantAccorde(),
                pret.getDureeMois(), pret.getDateDeblocage(), pret.getDateEcheance(), pret.getStatut(),
                pret.getRecu() == null ? null : pret.getRecu().getId());
    }

    private void verifierTenant(DemandePret demande, Long tontineId) {
        verifierTenant(demande.getMembre().getTontine().getId(), tontineId);
    }

    private void verifierTenant(Long ressourceTontineId, Long tontineId) {
        if (!ressourceTontineId.equals(tontineId)) {
            // R1 : une ressource d'un autre tenant doit être indistinguable d'une absence.
            throw new RessourceIntrouvableException("Prêt introuvable");
        }
    }
}
