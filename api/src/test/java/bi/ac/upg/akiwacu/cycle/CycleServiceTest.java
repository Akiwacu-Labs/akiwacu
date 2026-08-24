package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.cycle.dto.CycleRequest;
import bi.ac.upg.akiwacu.cycle.mapper.CycleMapper;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import org.junit.jupiter.api.AfterEach;
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
                .hasMessageContaining("prêts actifs");
    }

    @Test
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

    private CycleRequest request() {
        return new CycleRequest("Cycle 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                BigDecimal.valueOf(100), Periodicite.MENSUELLE);
    }
}
