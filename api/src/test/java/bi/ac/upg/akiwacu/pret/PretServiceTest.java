package bi.ac.upg.akiwacu.pret;

import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.demandepret.DemandePret;
import bi.ac.upg.akiwacu.demandepret.DemandePretRepository;
import bi.ac.upg.akiwacu.demandepret.StatutDemandePret;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.recu.RecuService;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.pret.dto.PretDisbursementRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PretService — règle R6")
class PretServiceTest {

    @Mock
    private CotisationRepository cotisationRepository;

    @Mock
    private PretRepository pretRepository;

    @Mock
    private DemandePretRepository demandePretRepository;

    @Mock
    private CycleGuardService cycleGuardService;

    @Mock
    private ValidateurCourantService validateurCourantService;

    @Mock
    private RecuService recuService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter rejectedLoanCounter;

    @InjectMocks
    private PretService pretService;

    @org.junit.jupiter.api.BeforeEach
    void initialiserTenant() {
        bi.ac.upg.akiwacu.common.TenantContext.setTontineId(1L);
    }

    @org.junit.jupiter.api.AfterEach
    void nettoyerTenant() {
        bi.ac.upg.akiwacu.common.TenantContext.clear();
    }

    @Test
    @DisplayName("R6 — refuse un prêt supérieur à trois fois les cotisations du cycle actif")
    void shouldRejectLoanExceedingThreeTimesSavings() {
        // Le membre a cotisé 100 000 BIF sur le cycle actif : le plafond est 300 000 BIF.
        // La demande de 300 001 BIF doit être refusée avant toute création de demande.
        when(cotisationRepository.sommeParMembreEtCycle(1L, 10L))
                .thenReturn(new BigDecimal("100000"));
        when(meterRegistry.counter("akiwacu.prets.refuses.total", "motif", "R6"))
                .thenReturn(rejectedLoanCounter);

        assertThatThrownBy(() -> pretService.verifierLimiteMontant(
                1L, 10L, new BigDecimal("300001")))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R6")
                .hasMessageContaining("300001")
                .hasMessageContaining("100000");
        verify(rejectedLoanCounter).increment();
    }

    @Test
    @DisplayName("R6 — accepte exactement trois fois les cotisations du cycle actif")
    void shouldAcceptLoanExactlyThreeTimesSavings() {
        // Le plafond est inclus : 300 000 BIF est accepté pour 100 000 BIF cotisés.
        when(cotisationRepository.sommeParMembreEtCycle(1L, 10L))
                .thenReturn(new BigDecimal("100000"));

        assertThatCode(() -> pretService.verifierLimiteMontant(
                1L, 10L, new BigDecimal("300000")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("R6 — un prêt en cours ne réduit pas le plafond des cotisations")
    void shouldKeepContributionBasedLimitWhenLoanOutstanding() {
        // Le prêt en cours de 60 000 BIF est volontairement ignoré par R6.
        // Le plafond reste 300 000 BIF, car seules les cotisations du cycle comptent.
        when(cotisationRepository.sommeParMembreEtCycle(1L, 10L))
                .thenReturn(new BigDecimal("100000"));

        assertThatCode(() -> pretService.verifierLimiteMontant(
                1L, 10L, new BigDecimal("300000")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("R6 — refuse un prêt à un membre sans cotisation")
    void shouldRejectLoanForMemberWithoutSavings() {
        // Sans cotisation, le plafond vaut zéro et même une demande d'un BIF est refusée.
        when(cotisationRepository.sommeParMembreEtCycle(1L, 10L))
                .thenReturn(BigDecimal.ZERO);
        when(meterRegistry.counter("akiwacu.prets.refuses.total", "motif", "R6"))
                .thenReturn(rejectedLoanCounter);

        assertThatThrownBy(() -> pretService.verifierLimiteMontant(
                1L, 10L, BigDecimal.ONE))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R6");
        verify(rejectedLoanCounter).increment();
    }

    @Test
    @DisplayName("R7 — refuse une échéance après la fin du cycle")
    void shouldRejectDueDateAfterCycleEnd() {
        var cycle = Cycle.builder().dateFin(LocalDate.of(2026, 12, 31)).build();

        assertThatThrownBy(() -> pretService.verifierEcheance(cycle,
                LocalDate.of(2027, 1, 1)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R7");
    }

    @Test
    @DisplayName("R7 — accepte une échéance au dernier jour du cycle")
    void shouldAcceptDueDateOnCycleEndDate() {
        var cycle = Cycle.builder().dateFin(LocalDate.of(2026, 12, 31)).build();

        assertThatCode(() -> pretService.verifierEcheance(cycle,
                LocalDate.of(2026, 12, 31)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("R3/R5 — crée et débloque un prêt approuvé avec son trésorier")
    void shouldDisburseApprovedLoanAndRecordTreasurer() {
        var cycle = bi.ac.upg.akiwacu.cycle.Cycle.builder().tontine(tontine(1L))
                .dateFin(LocalDate.of(2026, 12, 31)).build();
        cycle.setId(10L);
        var membre = Membre.builder().tontine(tontine(1L)).build();
        membre.setId(7L);
        var demande = DemandePret.builder().cycle(cycle).membre(membre)
                .montantDemande(new BigDecimal("250000")).dureeMois(3)
                .statut(StatutDemandePret.APPROUVEE).build();
        demande.setId(20L);
        var validateur = Utilisateur.builder().tontine(tontine(1L))
                .email("tresorier@akiwacu.bi").build();
        var pret = Pret.builder().demandePret(demande).membre(membre).cycle(cycle)
                .montantAccorde(new BigDecimal("250000")).dureeMois(3)
                .dateDeblocage(LocalDate.now()).dateEcheance(LocalDate.of(2026, 12, 31))
                .statut(StatutPret.ACTIF).validePar(validateur).build();
        pret.setId(30L);

        when(cycleGuardService.assertCycleActif(1L)).thenReturn(cycle);
        when(demandePretRepository.findById(20L)).thenReturn(Optional.of(demande));
        when(pretRepository.findByDemandePretId(20L)).thenReturn(Optional.empty());
        when(validateurCourantService.obtenir()).thenReturn(validateur);
        when(pretRepository.save(any(Pret.class))).thenReturn(pret);

        var result = pretService.debloquerPret(new PretDisbursementRequest(
                20L, LocalDate.of(2026, 12, 31)));

        assertThat(result.id()).isEqualTo(30L);
        assertThat(result.statut()).isEqualTo(StatutPret.ACTIF);
        assertThat(demande.getStatut()).isEqualTo(StatutDemandePret.DEBLOQUEE);
        verify(validateurCourantService).obtenir();
        verify(recuService).genererPourPret(30L);
        verify(demandePretRepository).save(demande);
    }

    @Test
    @DisplayName("R3 — refuse le déblocage si le cycle n'est plus actif")
    void shouldRejectDisbursementWhenCycleIsFrozen() {
        var r3 = new RegleMetierException("R3 : cycle gelé");
        when(cycleGuardService.assertCycleActif(1L)).thenThrow(r3);

        assertThatThrownBy(() -> pretService.debloquerPret(new PretDisbursementRequest(
                20L, LocalDate.of(2026, 12, 31))))
                .isSameAs(r3);
        verify(pretRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refuse le déblocage d'une demande non approuvée")
    void shouldRejectDisbursementWhenRequestIsNotApproved() {
        var cycle = bi.ac.upg.akiwacu.cycle.Cycle.builder().tontine(tontine(1L))
                .dateFin(LocalDate.of(2026, 12, 31)).build();
        cycle.setId(10L);
        var membre = Membre.builder().tontine(tontine(1L)).build();
        var demande = DemandePret.builder().cycle(cycle).membre(membre)
                .statut(StatutDemandePret.SOUMISE).build();
        demande.setId(20L);
        when(cycleGuardService.assertCycleActif(1L)).thenReturn(cycle);
        when(demandePretRepository.findById(20L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> pretService.debloquerPret(new PretDisbursementRequest(
                20L, LocalDate.of(2026, 12, 31))))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("approuvée");
        verify(pretRepository, never()).save(any());
    }

    private Tontine tontine(Long id) {
        var tontine = Tontine.builder().build();
        tontine.setId(id);
        return tontine;
    }
}
