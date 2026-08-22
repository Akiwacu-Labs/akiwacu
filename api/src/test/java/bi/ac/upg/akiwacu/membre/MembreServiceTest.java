package bi.ac.upg.akiwacu.membre;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.membre.dto.MembreModificationRequest;
import bi.ac.upg.akiwacu.membre.dto.MembreRequest;
import bi.ac.upg.akiwacu.membre.mapper.MembreMapperImpl;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MembreService — CRUD et R1")
class MembreServiceTest {

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private TontineRepository tontineRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private final MembreMapperImpl membreMapper = new MembreMapperImpl();

    private MembreService membreService;

    @BeforeEach
    void setUp() {
        membreService = new MembreService(membreRepository, tontineRepository, utilisateurRepository, membreMapper);
        TenantContext.setTontineId(1L);
    }

    @AfterEach
    void nettoyer() {
        TenantContext.clear();
    }

    private Tontine uneTontine() {
        Tontine tontine = Tontine.builder().build();
        tontine.setId(1L);
        return tontine;
    }

    private Membre unMembre() {
        Membre membre = Membre.builder()
                .tontine(uneTontine())
                .numeroMembre("M-014")
                .nom("Nkurunziza")
                .prenom("Alice")
                .telephone("79000000")
                .dateAdhesion(LocalDate.of(2026, 1, 10))
                .statut(StatutMembre.ACTIF)
                .build();
        membre.setId(5L);
        return membre;
    }

    @Test
    @DisplayName("cas nominal — crée un membre sans compte de connexion")
    void shouldCreateMembreWithoutUtilisateur() {
        // Arrange
        var requete = new MembreRequest("M-030", "Havyarimana", "Marie", "78000000",
                LocalDate.of(2026, 3, 1), null);
        when(membreRepository.existsByNumeroMembre("M-030")).thenReturn(false);
        when(tontineRepository.findById(1L)).thenReturn(Optional.of(uneTontine()));
        when(membreRepository.save(any(Membre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var reponse = membreService.creer(requete);

        // Assert
        assertThat(reponse.nom()).isEqualTo("Havyarimana");
        assertThat(reponse.statut()).isEqualTo(StatutMembre.ACTIF);
        assertThat(reponse.utilisateurId()).isNull();
        verify(utilisateurRepository, never()).findById(any());
    }

    @Test
    @DisplayName("cas nominal — lie un membre à un utilisateur existant de la même tontine")
    void shouldLinkMembreToExistingUtilisateur() {
        // Arrange
        var requete = new MembreRequest("M-031", "Bukuru", "Jean", "77000000",
                LocalDate.of(2026, 3, 1), 42L);
        Utilisateur utilisateur = Utilisateur.builder().build();
        utilisateur.setId(42L);
        when(membreRepository.existsByNumeroMembre("M-031")).thenReturn(false);
        when(tontineRepository.findById(1L)).thenReturn(Optional.of(uneTontine()));
        when(utilisateurRepository.findById(42L)).thenReturn(Optional.of(utilisateur));
        when(membreRepository.save(any(Membre.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var reponse = membreService.creer(requete);

        // Assert
        assertThat(reponse.utilisateurId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("cas d'erreur métier — refuse un numéro de membre déjà utilisé dans la tontine")
    void shouldRejectDuplicateNumeroMembre() {
        var requete = new MembreRequest("M-014", "Bukuru", "Jean", "77000000",
                LocalDate.of(2026, 3, 1), null);
        when(membreRepository.existsByNumeroMembre("M-014")).thenReturn(true);

        assertThatThrownBy(() -> membreService.creer(requete))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    @DisplayName("cas d'exception — utilisateurId fourni introuvable (ou hors tontine, R1)")
    void shouldThrowWhenUtilisateurToLinkNotFound() {
        // Le filtre Hibernate (D-33) rend un utilisateurId d'une autre tontine
        // indiscernable d'un id inexistant : findById renvoie vide dans les deux cas.
        var requete = new MembreRequest("M-032", "Niyonzima", "Eric", "76000000",
                LocalDate.of(2026, 3, 1), 999L);
        when(membreRepository.existsByNumeroMembre("M-032")).thenReturn(false);
        when(tontineRepository.findById(1L)).thenReturn(Optional.of(uneTontine()));
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membreService.creer(requete))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal — modifie un membre existant")
    void shouldUpdateMembre() {
        Membre existant = unMembre();
        var requete = new MembreModificationRequest("M-014", "Nkurunziza", "Alice-Marie",
                "79111111", StatutMembre.SUSPENDU);
        when(membreRepository.findById(5L)).thenReturn(Optional.of(existant));

        var reponse = membreService.modifier(5L, requete);

        assertThat(reponse.prenom()).isEqualTo("Alice-Marie");
        assertThat(reponse.statut()).isEqualTo(StatutMembre.SUSPENDU);
    }

    @Test
    @DisplayName("cas d'exception — lève une exception en modifiant un membre introuvable")
    void shouldThrowWhenUpdatingUnknownMembre() {
        var requete = new MembreModificationRequest("M-014", "X", "Y", "70000000", StatutMembre.ACTIF);
        when(membreRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membreService.modifier(404L, requete))
                .isInstanceOf(RessourceIntrouvableException.class);
    }
}
