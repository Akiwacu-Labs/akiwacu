package bi.ac.upg.akiwacu.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TenantContext — base de R1, un tontineId par thread de requête")
class TenantContextTest {

    @AfterEach
    void nettoyer() {
        // Sans ce clear(), un tontineId posé par un test fuiterait sur les suivants —
        // même risque que Tomcat réutilisant un thread entre deux requêtes en prod.
        TenantContext.clear();
    }

    @Test
    @DisplayName("R1 — renvoie le tontineId posé pour ce thread")
    void shouldReturnTontineIdSetOnThisThread() {
        // Arrange
        TenantContext.setTontineId(42L);

        // Act
        Long tontineId = TenantContext.getTontineId();

        // Assert
        assertThat(tontineId).isEqualTo(42L);
    }

    @Test
    @DisplayName("R1 — refuse de renvoyer un tontineId à null : lève plutôt une exception")
    void shouldThrowWhenNoTontineIdIsSet() {
        // Ce test protège contre le pire bug possible pour R1 : un service qui
        // interrogerait la base avec un tontineId à null au lieu d'échouer bruyamment.
        assertThatThrownBy(TenantContext::getTontineId)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("R1 — clear() supprime le tontineId posé")
    void shouldRemoveTontineIdAfterClear() {
        // Arrange
        TenantContext.setTontineId(7L);

        // Act
        TenantContext.clear();

        // Assert
        assertThatThrownBy(TenantContext::getTontineId)
                .isInstanceOf(IllegalStateException.class);
    }
}
