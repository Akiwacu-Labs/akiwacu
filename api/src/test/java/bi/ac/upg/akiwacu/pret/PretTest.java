package bi.ac.upg.akiwacu.pret;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pret — calcul du montant dû")
class PretTest {

    @Test
    @DisplayName("D-25 — calcule les intérêts mensuels simples sur toute la durée")
    void shouldCalculateMonthlySimpleInterestOverLoanDuration() {
        // 100 000 BIF à 10 % par mois pendant 3 mois : intérêts = 30 000 BIF.
        // Ce test protège la multiplication par dureeMois, absente dans l'ancienne formule.
        var pret = Pret.builder()
                .montantAccorde(new BigDecimal("100000"))
                .tauxInteret(new BigDecimal("10"))
                .dureeMois(3)
                .build();

        assertThat(pret.montantDu()).isEqualByComparingTo("130000.00");
    }

    @Test
    @DisplayName("D-25 — un taux nul ne modifie pas le montant accordé")
    void shouldReturnPrincipalWhenInterestRateIsZero() {
        // Un prêt sans intérêt conserve exactement son capital, quelle que soit sa durée.
        // Le test évite qu'une future modification introduise un coût fixe involontaire.
        var pret = Pret.builder()
                .montantAccorde(new BigDecimal("75000"))
                .tauxInteret(BigDecimal.ZERO)
                .dureeMois(6)
                .build();

        assertThat(pret.montantDu()).isEqualByComparingTo("75000.00");
    }

    @Test
    @DisplayName("D-25 — arrondit les intérêts une seule fois avec HALF_UP")
    void shouldRoundInterestHalfUpAfterWholeCalculation() {
        // 1 001 × 0,50 % = 5,005 : l'arrondi HALF_UP doit donner 5,01, puis 1 006,01.
        // Cela protège contre un arrondi prématuré ou un mauvais mode d'arrondi.
        var pret = Pret.builder()
                .montantAccorde(new BigDecimal("1001"))
                .tauxInteret(new BigDecimal("0.50"))
                .dureeMois(1)
                .build();

        assertThat(pret.montantDu()).isEqualByComparingTo("1006.01");
    }
}
