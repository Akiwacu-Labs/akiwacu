package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.Periodicite;
import bi.ac.upg.akiwacu.cycle.StatutCycle;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.StatutMembre;
import bi.ac.upg.akiwacu.tontine.StatutTontine;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Role;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("CotisationRepository — lecture tenant-scopée D-27")
class CotisationRepositoryDataJpaTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CotisationRepository cotisationRepository;

    @Autowired
    private EntityManager entityManager;

    private Tontine tontineA;
    private Cycle cycleA;
    private Cycle cycleB;
    private Membre membreA;
    private Membre membreB;
    private Utilisateur validateurA;

    @BeforeEach
    void preparerDonnees() {
        tontineA = entityManager.merge(Tontine.builder()
                .nom("Tontine A - test repository")
                .dateCreation(LocalDate.of(2026, 1, 1))
                .statut(StatutTontine.ACTIVE)
                .build());
        var tontineB = entityManager.merge(Tontine.builder()
                .nom("Tontine B - test repository")
                .dateCreation(LocalDate.of(2026, 1, 1))
                .statut(StatutTontine.ACTIVE)
                .build());

        cycleA = entityManager.merge(cycle(tontineA, "Cycle A"));
        cycleB = entityManager.merge(cycle(tontineB, "Cycle B"));
        membreA = entityManager.merge(membre(tontineA, "Membre A"));
        membreB = entityManager.merge(membre(tontineB, "Membre B"));
        validateurA = entityManager.merge(Utilisateur.builder()
                .tontine(tontineA)
                .email("validateur-a@repository.test")
                .motDePasse("secret")
                .nom("Validateur")
                .prenom("A")
                .roles(Set.of(Role.TRESORIER))
                .build());
        entityManager.flush();

        entityManager.persist(cotisation(cycleA, membreA, validateurA, "100000"));
        entityManager.persist(cotisation(cycleA, membreA, validateurA, "50000"));
        entityManager.persist(cotisation(cycleB, membreB, validateurA, "900000"));
        entityManager.flush();
    }

    @AfterEach
    void nettoyerContexteTenant() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("D-27/R1 — ignore les cotisations d'une autre tontine")
    void shouldSumOnlyContributionsFromCurrentTenant() {
        TenantContext.setTontineId(tontineA.getId());

        var total = cotisationRepository.sommeParMembreEtCycle(membreA.getId(), cycleA.getId());

        assertThat(total).isEqualByComparingTo("150000.00");
    }

    @Test
    @DisplayName("R1 — masque les IDs d'une autre tontine et les relations croisées")
    void shouldNotSelectOtherTenantIds() {
        TenantContext.setTontineId(tontineA.getId());

        var otherTenantTotal = cotisationRepository.sommeParMembreEtCycle(
                membreB.getId(), cycleB.getId());
        var mixedTenantTotal = cotisationRepository.sommeParMembreEtCycle(
                membreB.getId(), cycleA.getId());

        assertThat(otherTenantTotal).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(mixedTenantTotal).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("D-27 — retourne zéro sans cotisation")
    void shouldReturnZeroWhenMemberHasNoContributionOnCycle() {
        var membreSansCotisation = entityManager.merge(membre(tontineA, "Sans cotisation"));
        entityManager.flush();
        TenantContext.setTontineId(tontineA.getId());

        var total = cotisationRepository.sommeParMembreEtCycle(
                membreSansCotisation.getId(), cycleA.getId());

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Cycle cycle(Tontine tontine, String libelle) {
        return Cycle.builder()
                .tontine(tontine)
                .libelle(libelle)
                .dateDebut(LocalDate.of(2026, 1, 1))
                .dateFin(LocalDate.of(2026, 12, 31))
                .montantCotisation(new BigDecimal("100000"))
                .periodicite(Periodicite.MENSUELLE)
                .statut(StatutCycle.OUVERT)
                .build();
    }

    private Membre membre(Tontine tontine, String nom) {
        return Membre.builder()
                .tontine(tontine)
                .nom(nom)
                .prenom("Test")
                .telephone("+25770000000")
                .dateAdhesion(LocalDate.of(2026, 1, 1))
                .statut(StatutMembre.ACTIF)
                .build();
    }

    private Cotisation cotisation(Cycle cycle, Membre membre, Utilisateur validateur,
                                  String montant) {
        return Cotisation.builder()
                .cycle(cycle)
                .membre(membre)
                .montant(new BigDecimal(montant))
                .dateCotisation(LocalDate.of(2026, 2, 1))
                .modePaiement(ModePaiement.ESPECES)
                .validePar(validateur)
                .build();
    }
}
