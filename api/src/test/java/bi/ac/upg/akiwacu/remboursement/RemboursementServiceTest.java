package bi.ac.upg.akiwacu.remboursement;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private Pret unPret(BigDecimal montant) {
        Tontine tontine = Tontine.builder().nom("Akiwacu").build();
        tontine.setId(1L);
        Cycle cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.OUVERT).build();
        cycle.setId(3L);
        Membre membre = Membre.builder().tontine(tontine).build();
        return Pret.builder()
                .cycle(cycle)
                .membre(membre)
                .montantAccorde(montant)
                .tauxInteret(BigDecimal.ZERO)
                .dureeMois(1)
                .remboursements(new java.util.ArrayList<>())
                .build();
    }
}
