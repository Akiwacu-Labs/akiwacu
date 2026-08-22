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
}
