package bi.ac.upg.akiwacu.pret;

import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PretService — règle R6")
class PretServiceTest {

    @Mock
    private CotisationRepository cotisationRepository;

    @InjectMocks
    private PretService pretService;

    @Test
    @DisplayName("R6 — refuse un prêt supérieur à trois fois les cotisations du cycle actif")
    void shouldRejectLoanExceedingThreeTimesSavings() {
        // Le membre a cotisé 100 000 BIF sur le cycle actif : le plafond est 300 000 BIF.
        // La demande de 300 001 BIF doit être refusée avant toute création de demande.
        when(cotisationRepository.sommeParMembreEtCycle(1L, 10L))
                .thenReturn(new BigDecimal("100000"));

        assertThatThrownBy(() -> pretService.verifierLimiteMontant(
                1L, 10L, new BigDecimal("300001")))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R6")
                .hasMessageContaining("300001")
                .hasMessageContaining("100000");
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

        assertThatThrownBy(() -> pretService.verifierLimiteMontant(
                1L, 10L, BigDecimal.ONE))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R6");
    }
}
