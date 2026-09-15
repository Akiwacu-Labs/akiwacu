package bi.ac.upg.akiwacu.dashboard;

import bi.ac.upg.akiwacu.caisse.TransactionCaisseRepository;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.cycle.CycleRepository;
import bi.ac.upg.akiwacu.demandepret.DemandePretRepository;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.pret.PretRepository;
import bi.ac.upg.akiwacu.remboursement.RemboursementRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService — agrégats par tontine")
class DashboardServiceTest {

    @Mock
    private MembreRepository membreRepository;
    @Mock
    private CotisationRepository cotisationRepository;
    @Mock
    private CycleRepository cycleRepository;
    @Mock
    private DemandePretRepository demandePretRepository;
    @Mock
    private PretRepository pretRepository;
    @Mock
    private RemboursementRepository remboursementRepository;
    @Mock
    private TransactionCaisseRepository transactionRepository;

    private DashboardService service;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new DashboardService(
                membreRepository,
                cotisationRepository,
                cycleRepository,
                demandePretRepository,
                pretRepository,
                remboursementRepository,
                transactionRepository,
                meterRegistry);
        TenantContext.setTontineId(1L);
        when(membreRepository.findAll()).thenReturn(java.util.List.of());
        when(cotisationRepository.findAll()).thenReturn(java.util.List.of());
        when(cycleRepository.findAll()).thenReturn(java.util.List.of());
        when(demandePretRepository.findAll()).thenReturn(java.util.List.of());
        when(pretRepository.findAll()).thenReturn(java.util.List.of());
        when(remboursementRepository.findAll()).thenReturn(java.util.List.of());
        when(transactionRepository.findAll()).thenReturn(java.util.List.of());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("retourne des agrégats nuls pour une tontine sans données")
    void shouldReturnEmptyAggregates() {
        var response = service.agregats();

        assertThat(response.membresActifs()).isZero();
        assertThat(response.cotisationsTotal()).isZero();
        assertThat(response.pretsEnCours()).isZero();
        assertThat(response.remboursementsTotal()).isZero();
        assertThat(response.soldeCaisse()).isZero();
        assertThat(meterRegistry.find("akiwacu.prets.refuses.total").meter()).isNull();
    }
}
