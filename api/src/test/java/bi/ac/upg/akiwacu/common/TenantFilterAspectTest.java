package bi.ac.upg.akiwacu.common;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantFilterAspect — active tontineFilter au début de chaque transaction")
class TenantFilterAspectTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Session session;

    @Mock
    private org.hibernate.Filter filter;

    @AfterEach
    void nettoyer() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("R1 — active tontineFilter avec le tontineId du contexte courant, jamais un autre")
    void shouldNotAccessDataFromAnotherTontine() {
        // Arrange : l'utilisateur courant appartient à la tontine 7.
        TenantContext.setTontineId(7L);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter("tontineFilter")).thenReturn(filter);
        when(filter.setParameter("tontineId", 7L)).thenReturn(filter);

        // Act
        new TenantFilterAspect(entityManager).activerFiltreTontine();

        // Assert : le filtre Hibernate est activé avec EXACTEMENT le tontineId
        // du contexte courant — jamais un autre, jamais aucun. C'est ce
        // paramètre qui, une fois la requête SQL exécutée, exclut les lignes
        // de toute autre tontine. Preuve au niveau SQL (base réelle, deux
        // tontines, deux membres) faite manuellement en session de travail :
        // voir le message du commit — non reproductible en CI sans Postgres
        // (Testcontainers, prévu pour Klein, comblera cet écart).
        verify(session).enableFilter("tontineFilter");
        verify(filter).setParameter("tontineId", 7L);
    }

    @Test
    @DisplayName("n'active rien hors contexte authentifié (ex. AuthService.login())")
    void shouldSkipWhenNoTenantContextIsSet() {
        // Arrange : aucun TenantContext.setTontineId() — cas du login, avant
        // de savoir dans quelle tontine chercher l'utilisateur.

        // Act
        new TenantFilterAspect(entityManager).activerFiltreTontine();

        // Assert : ne touche pas à l'EntityManager, donc ne lève jamais
        // l'IllegalStateException de TenantContext.getTontineId() à chaque login.
        verifyNoInteractions(entityManager);
    }
}
