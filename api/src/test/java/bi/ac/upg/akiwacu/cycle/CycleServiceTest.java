package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.dto.CycleRequest;
import bi.ac.upg.akiwacu.cycle.mapper.CycleMapper;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CycleServiceTest {

    @Mock CycleRepository cycleRepository;
    @Mock TontineRepository tontineRepository;
    @Mock ObjectProvider<CycleActiveLoanPort> activeLoanPort;

    @AfterEach void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("Refuse la création d'un second cycle ouvert dans la même tontine")
    void refuseSecondOpenCycle() {
        TenantContext.setTontineId(1L);
        when(cycleRepository.existsByTontineIdAndStatut(1L, StatutCycle.OUVERT)).thenReturn(true);
        var service = new CycleService(cycleRepository, tontineRepository, new CycleMapper(), activeLoanPort);

        assertThatThrownBy(() -> service.creer(request()))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("Un seul cycle");
        verifyNoInteractions(tontineRepository);
    }

    @Test
    @DisplayName("Refuse la clôture si l'adaptateur des prêts actifs est indisponible")
    void refuseClosingWhenLoanContractIsMissing() {
        TenantContext.setTontineId(1L);
        var tontine = Tontine.builder().build();
        tontine.setId(1L);
        var cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.GELE).build();
        cycle.setId(4L);
        when(cycleRepository.findByIdAndTontineId(4L, 1L)).thenReturn(Optional.of(cycle));
        when(activeLoanPort.getIfAvailable()).thenReturn(null);
        var service = new CycleService(cycleRepository, tontineRepository, new CycleMapper(), activeLoanPort);

        assertThatThrownBy(() -> service.changerStatut(4L, StatutCycle.CLOTURE))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("adaptateur");
    }

    @Test
    @DisplayName("Refuse le passage direct d'un cycle ouvert à la clôture")
    void refuseDirectTransitionFromOpenToClosed() {
        TenantContext.setTontineId(1L);
        var tontine = Tontine.builder().build();
        tontine.setId(1L);
        var cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.OUVERT).build();
        cycle.setId(4L);
        when(cycleRepository.findByIdAndTontineId(4L, 1L)).thenReturn(Optional.of(cycle));
        var service = new CycleService(cycleRepository, tontineRepository, new CycleMapper(), activeLoanPort);

        assertThatThrownBy(() -> service.changerStatut(4L, StatutCycle.CLOTURE))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("d'abord être gelé");
        verifyNoInteractions(activeLoanPort);
    }

    @Test
    @DisplayName("Clôture un cycle gelé lorsqu'aucun prêt actif ne subsiste")
    void closesFrozenCycleWhenNoActiveLoanExists() {
        TenantContext.setTontineId(1L);
        var tontine = Tontine.builder().build();
        tontine.setId(1L);
        var cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.GELE).build();
        cycle.setId(4L);
        var loanPort = mock(CycleActiveLoanPort.class);
        when(cycleRepository.findByIdAndTontineId(4L, 1L)).thenReturn(Optional.of(cycle));
        when(activeLoanPort.getIfAvailable()).thenReturn(loanPort);
        when(loanPort.existePretActif(4L, 1L)).thenReturn(false);
        var service = new CycleService(cycleRepository, tontineRepository, new CycleMapper(), activeLoanPort);

        var response = service.changerStatut(4L, StatutCycle.CLOTURE);

        org.assertj.core.api.Assertions.assertThat(response.statut()).isEqualTo(StatutCycle.CLOTURE);
        org.assertj.core.api.Assertions.assertThat(cycle.getDateCloture()).isNotNull();
    }

    @Test
    @DisplayName("Refuse la clôture lorsqu'un prêt actif existe dans le cycle")
    void refusesClosingWhenActiveLoanExists() {
        TenantContext.setTontineId(1L);
        var tontine = Tontine.builder().build();
        tontine.setId(1L);
        var cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.GELE).build();
        cycle.setId(4L);
        var loanPort = mock(CycleActiveLoanPort.class);
        when(cycleRepository.findByIdAndTontineId(4L, 1L)).thenReturn(Optional.of(cycle));
        when(activeLoanPort.getIfAvailable()).thenReturn(loanPort);
        when(loanPort.existePretActif(4L, 1L)).thenReturn(true);
        var service = new CycleService(cycleRepository, tontineRepository, new CycleMapper(), activeLoanPort);

        assertThatThrownBy(() -> service.changerStatut(4L, StatutCycle.CLOTURE))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R3");
    }

    @Test
    @DisplayName("Refuse des dates de cycle incohérentes")
    void refusesInvalidCycleDates() {
        TenantContext.setTontineId(1L);
        var service = new CycleService(cycleRepository, tontineRepository, new CycleMapper(), activeLoanPort);

        assertThatThrownBy(() -> service.creer(new CycleRequest(
                "Cycle invalide", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1),
                BigDecimal.TEN, Periodicite.MENSUELLE)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("date de fin");
    }

    @Test
    @DisplayName("Signale un cycle introuvable lors de sa consultation")
    void reportsMissingCycle() {
        TenantContext.setTontineId(1L);
        when(cycleRepository.findByIdAndTontineId(99L, 1L)).thenReturn(Optional.empty());
        var service = new CycleService(cycleRepository, tontineRepository, new CycleMapper(), activeLoanPort);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.recuperer(99L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    private CycleRequest request() {
        return new CycleRequest("Cycle 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                BigDecimal.valueOf(100), Periodicite.MENSUELLE);
    }
}
