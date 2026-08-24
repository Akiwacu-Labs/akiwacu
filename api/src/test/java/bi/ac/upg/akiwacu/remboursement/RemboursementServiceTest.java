package bi.ac.upg.akiwacu.remboursement;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.OperationVerrouilleeException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.cycle.StatutCycle;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.pret.Pret;
import bi.ac.upg.akiwacu.pret.PretRepository;
import bi.ac.upg.akiwacu.remboursement.dto.RemboursementRequest;
import bi.ac.upg.akiwacu.remboursement.mapper.RemboursementMapper;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemboursementService — règles métier")
class RemboursementServiceTest {

    @Mock
    private RemboursementRepository remboursementRepository;
    @Mock
    private PretRepository pretRepository;
    @Mock
    private CycleGuardService cycleGuardService;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private RemboursementService service;

    @BeforeEach
    void setUp() {
        service = new RemboursementService(
                remboursementRepository,
                pretRepository,
                cycleGuardService,
                new ValidateurCourantService(utilisateurRepository),
                new RemboursementMapper());
        TenantContext.setTontineId(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "tresorier@akiwacu.test", "secret", java.util.List.of()));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("refuse un remboursement supérieur au solde du prêt")
    void shouldRejectRepaymentExceedingRemainingBalance() {
        Pret pret = unPret(new BigDecimal("100000"));
        when(pretRepository.findById(5L)).thenReturn(Optional.of(pret));
        when(cycleGuardService.assertCycleActif(1L)).thenReturn(pret.getCycle());

        assertThatThrownBy(() -> service.enregistrer(new RemboursementRequest(
                5L, new BigDecimal("100001"), LocalDate.of(2026, 8, 21))))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("dépasse");
    }

    @Test
    @DisplayName("lists repayments for a loan in the current tontine")
    void shouldListRepaymentsForCurrentTenantLoan() {
        Pret pret = unPret(new BigDecimal("100000"));
        Remboursement remboursement = remboursement(8L, pret, "10000", false);
        when(pretRepository.findById(5L)).thenReturn(Optional.of(pret));
        when(remboursementRepository.findByPretId(5L)).thenReturn(List.of(remboursement));

        var responses = service.listerParPret(5L);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(8L);
    }

    @Test
    @DisplayName("rejects repayments listing for a foreign loan")
    void shouldRejectListingRepaymentsForForeignLoan() {
        Pret pretEtranger = unPret(new BigDecimal("100000"));
        pretEtranger.getCycle().getTontine().setId(2L);
        when(pretRepository.findById(5L)).thenReturn(Optional.of(pretEtranger));

        assertThatThrownBy(() -> service.listerParPret(5L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Prêt introuvable");

        verifyNoInteractions(remboursementRepository);
    }

    @Test
    @DisplayName("reports a missing loan when listing repayments")
    void shouldReportMissingLoanWhenListingRepayments() {
        when(pretRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listerParPret(99L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Prêt introuvable");
    }

    @Test
    @DisplayName("updates an unlocked repayment on an active cycle")
    void shouldUpdateUnlockedRepaymentOnActiveCycle() {
        Pret pret = unPret(new BigDecimal("100000"));
        Remboursement remboursement = remboursement(8L, pret, "10000", false);
        when(remboursementRepository.findById(8L)).thenReturn(Optional.of(remboursement));
        when(remboursementRepository.save(remboursement)).thenReturn(remboursement);

        var response = service.modifier(8L, demande(5L, "25000"));

        assertThat(response.montant()).isEqualByComparingTo("25000");
        verify(cycleGuardService).assertCycleActif(1L);
    }

    @Test
    @DisplayName("rejects an update to a locked repayment")
    void shouldRejectUpdateOfLockedRepayment() {
        Pret pret = unPret(new BigDecimal("100000"));
        Remboursement remboursement = remboursement(8L, pret, "10000", true);
        when(remboursementRepository.findById(8L)).thenReturn(Optional.of(remboursement));

        assertThatThrownBy(() -> service.modifier(8L, demande(5L, "25000")))
                .isInstanceOf(OperationVerrouilleeException.class)
                .hasMessageContaining("R8");
    }

    @Test
    @DisplayName("reports a missing repayment when updating")
    void shouldReportMissingRepaymentWhenUpdating() {
        when(remboursementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifier(99L, demande(5L, "25000")))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Remboursement introuvable");
    }

    private Pret unPret(BigDecimal montant) {
        Tontine tontine = Tontine.builder().nom("Akiwacu").build();
        tontine.setId(1L);
        Cycle cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.OUVERT).build();
        cycle.setId(3L);
        Membre membre = Membre.builder().tontine(tontine).build();
        Pret pret = Pret.builder()
                .cycle(cycle)
                .membre(membre)
                .montantAccorde(montant)
                .tauxInteret(BigDecimal.ZERO)
                .dureeMois(1)
                .remboursements(new java.util.ArrayList<>())
                .build();
        pret.setId(5L);
        return pret;
    }

    private Remboursement remboursement(Long id, Pret pret, String montant, boolean verrouille) {
        Utilisateur validateur = Utilisateur.builder().email("tresorier@akiwacu.test").build();
        validateur.setId(7L);
        Remboursement remboursement = Remboursement.builder()
                .pret(pret)
                .montant(new BigDecimal(montant))
                .dateRemboursement(LocalDate.of(2026, 8, 24))
                .validePar(validateur)
                .verrouille(verrouille)
                .build();
        remboursement.setId(id);
        return remboursement;
    }

    private RemboursementRequest demande(Long pretId, String montant) {
        return new RemboursementRequest(pretId, new BigDecimal(montant), LocalDate.of(2026, 8, 25));
    }
}
