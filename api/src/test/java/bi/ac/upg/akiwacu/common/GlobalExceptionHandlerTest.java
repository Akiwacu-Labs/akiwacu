package bi.ac.upg.akiwacu.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler — traduction des exceptions en réponses HTTP")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("filet de sécurité — une violation de contrainte d'unicité en base renvoie 409, pas 500")
    void shouldReturn409OnDataIntegrityViolationWithoutLeakingConstraintDetails() {
        // Arrange : un service qui n'a pas prévalidé une contrainte UNIQUE (ex. email
        // déjà utilisé) laisse Hibernate lever cette exception au flush/commit — la
        // cause JDBC porte le nom de contrainte et parfois le SQL généré.
        var requete = new MockHttpServletRequest("POST", "/api/tontines");
        var ex = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [uk_utilisateur_email]");

        // Act
        var reponse = handler.gererViolationContrainteBaseDeDonnees(ex, requete);

        // Assert : 409 propre pour le client, message générique — jamais le détail
        // SQL/nom de contrainte de la cause d'origine.
        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(reponse.getBody().message())
                .doesNotContain("uk_utilisateur_email", "SQL")
                .isEqualTo("Cette opération viole une contrainte d'unicité existante");
    }
}
