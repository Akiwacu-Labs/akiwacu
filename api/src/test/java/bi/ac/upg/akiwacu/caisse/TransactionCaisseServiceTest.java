package bi.ac.upg.akiwacu.caisse;

import bi.ac.upg.akiwacu.caisse.dto.TransactionCaisseRequest;
import bi.ac.upg.akiwacu.caisse.mapper.TransactionCaisseMapper;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.cycle.CycleRepository;
import bi.ac.upg.akiwacu.cycle.StatutCycle;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
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
@DisplayName("TransactionCaisseService — R5")
class TransactionCaisseServiceTest {

    @Mock
    private TransactionCaisseRepository repository;
    @Mock
    private TontineRepository tontineRepository;
    @Mock
    private CycleRepository cycleRepository;
    @Mock
    private CycleGuardService cycleGuardService;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private TransactionCaisseService service;

    @BeforeEach
    void setUp() {
        service = new TransactionCaisseService(
                repository,
                tontineRepository,
                cycleRepository,
                cycleGuardService,
                new ValidateurCourantService(utilisateurRepository),
                new TransactionCaisseMapper());
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
    @DisplayName("R5 — enregistre le trésorier authentifié comme validateur")
    void shouldRecordValidatingTreasurer() {
        Tontine tontine = Tontine.builder().nom("Akiwacu").build();
        tontine.setId(1L);
        Utilisateur tresorier = Utilisateur.builder().email("tresorier@akiwacu.test").build();
        tresorier.setId(7L);
        when(tontineRepository.findById(1L)).thenReturn(Optional.of(tontine));
        when(utilisateurRepository.findByEmail("tresorier@akiwacu.test"))
                .thenReturn(Optional.of(tresorier));
        when(repository.save(any(TransactionCaisse.class))).thenAnswer(invocation -> {
            TransactionCaisse transaction = invocation.getArgument(0);
            transaction.setId(10L);
            return transaction;
        });

        var response = service.enregistrer(new TransactionCaisseRequest(
                null,
                SensTransaction.ENTREE,
                new BigDecimal("50000.00"),
                "Cotisation en espèces",
                LocalDate.of(2026, 8, 21),
                "COTISATION:42"));

        assertThat(response.valideParId()).isEqualTo(7L);
        verify(repository).save(any(TransactionCaisse.class));
    }

    @Test
    @DisplayName("R1 — masque un cycle d'une autre tontine avant la garde R2")
    void shouldNotAccessDataFromAnotherTontine() {
        Tontine tontineCourante = Tontine.builder().nom("Akiwacu").build();
        tontineCourante.setId(1L);
        Tontine autreTontine = Tontine.builder().nom("Autre tontine").build();
        autreTontine.setId(2L);
        Cycle cycleEtranger = Cycle.builder()
                .tontine(autreTontine)
                .statut(StatutCycle.OUVERT)
                .build();
        cycleEtranger.setId(3L);

        when(tontineRepository.findById(1L)).thenReturn(Optional.of(tontineCourante));
        when(cycleRepository.findById(3L)).thenReturn(Optional.of(cycleEtranger));

        assertThatThrownBy(() -> service.enregistrer(new TransactionCaisseRequest(
                3L,
                SensTransaction.ENTREE,
                new BigDecimal("50000.00"),
                "Cotisation en espèces",
                LocalDate.of(2026, 8, 21),
                "COTISATION:42")))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Cycle introuvable");

        verifyNoInteractions(cycleGuardService);
    }

    @Test
    @DisplayName("liste les transactions du cycle courant")
    void shouldListTransactionsForCurrentTenantCycle() {
        Cycle cycle = cycle(3L, 1L);
        TransactionCaisse transaction = transaction(10L, 1L, cycle, SensTransaction.ENTREE, "50000");
        when(cycleRepository.findById(3L)).thenReturn(Optional.of(cycle));
        when(repository.findByCycleId(3L)).thenReturn(List.of(transaction));

        var responses = service.listerParCycle(3L);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("refuse la liste d'un cycle d'une autre tontine")
    void shouldRejectListingTransactionsForForeignCycle() {
        when(cycleRepository.findById(3L)).thenReturn(Optional.of(cycle(3L, 2L)));

        assertThatThrownBy(() -> service.listerParCycle(3L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Cycle introuvable");

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("signale un cycle absent lors de la liste")
    void shouldReportMissingCycleWhenListingTransactions() {
        when(cycleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listerParCycle(99L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Cycle introuvable");
    }

    @Test
    @DisplayName("calcule le solde des entrées et sorties de la tontine")
    void shouldCalculateCashBalanceForCurrentTenant() {
        when(repository.findAll()).thenReturn(List.of(
                transaction(1L, 1L, null, SensTransaction.ENTREE, "100000"),
                transaction(2L, 1L, null, SensTransaction.SORTIE, "25000")));

        assertThat(service.solde()).isEqualByComparingTo("75000");
    }

    @Test
    @DisplayName("exclut les transactions d'une autre tontine du solde")
    void shouldIgnoreForeignTenantTransactionsInCashBalance() {
        when(repository.findAll()).thenReturn(List.of(
                transaction(1L, 1L, null, SensTransaction.ENTREE, "100000"),
                transaction(2L, 2L, null, SensTransaction.ENTREE, "900000")));

        assertThat(service.solde()).isEqualByComparingTo("100000");
    }

    @Test
    @DisplayName("renvoie zéro lorsqu'aucune transaction n'appartient à la tontine")
    void shouldReturnZeroCashBalanceWithoutTenantTransactions() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.solde()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("modifie une transaction sur cycle actif")
    void shouldUpdateTransactionOnActiveCycle() {
        Cycle cycle = cycle(3L, 1L);
        TransactionCaisse transaction = transaction(10L, 1L, cycle, SensTransaction.ENTREE, "10000");
        when(repository.findById(10L)).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);

        var response = service.modifier(10L, request(SensTransaction.SORTIE, "25000"));

        assertThat(response.sens()).isEqualTo(SensTransaction.SORTIE);
        assertThat(response.montant()).isEqualByComparingTo("25000");
        verify(cycleGuardService).assertCycleActif(1L);
    }

    @Test
    @DisplayName("refuse la modification quand le cycle est inactif")
    void shouldRejectTransactionUpdateOnInactiveCycle() {
        TransactionCaisse transaction = transaction(10L, 1L, cycle(3L, 1L), SensTransaction.ENTREE, "10000");
        when(repository.findById(10L)).thenReturn(Optional.of(transaction));
        when(cycleGuardService.assertCycleActif(1L))
                .thenThrow(new bi.ac.upg.akiwacu.common.exception.RegleMetierException("Cycle inactif"));

        assertThatThrownBy(() -> service.modifier(10L, request(SensTransaction.SORTIE, "25000")))
                .isInstanceOf(bi.ac.upg.akiwacu.common.exception.RegleMetierException.class);
    }

    @Test
    @DisplayName("signale une transaction absente lors de la modification")
    void shouldReportMissingTransactionWhenUpdating() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifier(99L, request(SensTransaction.SORTIE, "25000")))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Transaction de caisse introuvable");
    }

    private Cycle cycle(Long id, Long tontineId) {
        Tontine tontine = Tontine.builder().nom("Tontine " + tontineId).build();
        tontine.setId(tontineId);
        Cycle cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.OUVERT).build();
        cycle.setId(id);
        return cycle;
    }

    private TransactionCaisse transaction(Long id, Long tontineId, Cycle cycle,
                                           SensTransaction sens, String montant) {
        Tontine tontine = Tontine.builder().nom("Tontine " + tontineId).build();
        tontine.setId(tontineId);
        Utilisateur validateur = Utilisateur.builder().email("tresorier@akiwacu.test").build();
        validateur.setId(7L);
        TransactionCaisse transaction = TransactionCaisse.builder()
                .tontine(tontine).cycle(cycle).sens(sens).montant(new BigDecimal(montant))
                .motif("Test").dateTransaction(LocalDate.of(2026, 8, 24))
                .validePar(validateur).build();
        transaction.setId(id);
        return transaction;
    }

    private TransactionCaisseRequest request(SensTransaction sens, String montant) {
        return new TransactionCaisseRequest(3L, sens, new BigDecimal(montant), "Mise à jour",
                LocalDate.of(2026, 8, 25), "MANUEL:1");
    }
}
