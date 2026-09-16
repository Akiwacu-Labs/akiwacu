package bi.ac.upg.akiwacu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'amorçage : il tourne sans base de données, donc la CI passe au vert
 * avant même que PostgreSQL soit disponible sur le runner.
 *
 * Il sera remplacé par de vrais tests d'entités — voir docs/MATRICE-REGLES-METIER.md.
 */
class AkiwacuApplicationTest {

    @Test
    void laClassePrincipaleEstChargeable() {
        assertThat(AkiwacuApplication.class).isNotNull();
    }
}
