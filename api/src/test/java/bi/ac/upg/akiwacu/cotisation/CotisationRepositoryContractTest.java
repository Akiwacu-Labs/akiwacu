package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.common.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("CotisationRepository — contrat de lecture D-27")
class CotisationRepositoryContractTest {

    private final CotisationRepository repository =
            mock(CotisationRepository.class, CALLS_REAL_METHODS);

    @AfterEach
    void nettoyerContexteTenant() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("D-27 — transmet la tontine courante au calcul des cotisations")
    void shouldUseCurrentTenantForContributionTotal() {
        TenantContext.setTontineId(7L);
        when(repository.sommeParMembreEtCycle(12L, 34L, 7L))
                .thenReturn(new BigDecimal("100000.00"));

        var total = repository.sommeParMembreEtCycle(12L, 34L);

        assertThat(total).isEqualByComparingTo("100000.00");
        verify(repository).sommeParMembreEtCycle(12L, 34L, 7L);
    }

    @Test
    @DisplayName("R1 — refuse une lecture hors requête authentifiée")
    void shouldRejectContributionTotalWithoutTenant() {
        assertThatThrownBy(() -> repository.sommeParMembreEtCycle(12L, 34L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hors d'une requête authentifiée");
    }
}
