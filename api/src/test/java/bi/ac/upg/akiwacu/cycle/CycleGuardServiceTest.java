package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CycleGuardService — garde partagée R2")
class CycleGuardServiceTest {

    @Mock
    private CycleRepository cycleRepository;

    @InjectMocks
    private CycleGuardService cycleGuardService;

    @Test
    @DisplayName("R2 — refuse une opération financière lorsque aucun cycle n'est actif")
    void shouldRejectOperationOnInactiveCycle() {
        // Arrange : la tontine ne possède aucun cycle ouvert.
        // Act + Assert : la garde lève une exception qui deviendra HTTP 409.
        // Cela empêche chaque domaine appelant de réimplémenter R2 différemment.
        var tontineId = 1L;
        when(cycleRepository.findByTontineIdAndStatut(tontineId, StatutCycle.OUVERT))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cycleGuardService.assertCycleActif(tontineId))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R2")
                .hasMessageContaining("aucun cycle actif");

        verify(cycleRepository).findByTontineIdAndStatut(tontineId, StatutCycle.OUVERT);
    }

    @Test
    @DisplayName("R2 — retourne le cycle ouvert")
    void shouldReturnOpenCycle() {
        // Arrange : un cycle ouvert existe pour la tontine.
        // Act : la garde recherche le cycle actif.
        // Assert : elle retourne exactement celui fourni par le repository.
        var tontineId = 1L;
        var cycle = Cycle.builder().statut(StatutCycle.OUVERT).build();
        when(cycleRepository.findByTontineIdAndStatut(tontineId, StatutCycle.OUVERT))
                .thenReturn(Optional.of(cycle));

        assertThat(cycleGuardService.assertCycleActif(tontineId)).isSameAs(cycle);
    }
}
