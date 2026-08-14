package bi.ac.upg.akiwacu.cycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cycle — comportement métier de la machine à états")
class CycleTest {

    @ParameterizedTest
    @EnumSource(value = StatutCycle.class, names = "OUVERT")
    @DisplayName("R2 — accepte une opération financière uniquement lorsque le cycle est ouvert")
    void shouldAcceptFinancialOperationWhenCycleIsOpen(StatutCycle statut) {
        // Un cycle ouvert est le seul état où le trésorier peut enregistrer une opération.
        // Ce test évite qu'un changement futur autorise accidentellement un cycle gelé ou clôturé.
        var cycle = Cycle.builder().statut(statut).build();

        assertThat(cycle.peutAccueillirOperation()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = StatutCycle.class, names = "OUVERT", mode = Mode.EXCLUDE)
    @DisplayName("R2 — refuse une opération financière lorsque le cycle n'est pas ouvert")
    void shouldRejectFinancialOperationWhenCycleIsNotOpen(StatutCycle statut) {
        // Le gel suspend les opérations et la clôture est définitive.
        // Le test protège la règle R2 dans les deux états inactifs.
        var cycle = Cycle.builder().statut(statut).build();

        assertThat(cycle.peutAccueillirOperation()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = StatutCycle.class, names = "OUVERT")
    @DisplayName("R3 — autorise un nouveau prêt seulement lorsque le cycle est ouvert")
    void shouldAllowNewLoanWhenCycleIsOpen(StatutCycle statut) {
        // Un nouveau prêt ne peut être créé que pendant la période active du cycle.
        // Cela empêche une régression qui contournerait R3 au niveau de l'entité.
        var cycle = Cycle.builder().statut(statut).build();

        assertThat(cycle.autoriseNouveauPret()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = StatutCycle.class, names = "OUVERT", mode = Mode.EXCLUDE)
    @DisplayName("R3 — bloque un nouveau prêt lorsque le cycle est gelé ou clôturé")
    void shouldRejectNewLoanWhenCycleIsNotOpen(StatutCycle statut) {
        // Un cycle gelé ou clôturé ne peut plus accepter de nouveau prêt.
        // Ce test couvre les deux cas que la règle R3 doit refuser.
        var cycle = Cycle.builder().statut(statut).build();

        assertThat(cycle.autoriseNouveauPret()).isFalse();
    }
}
