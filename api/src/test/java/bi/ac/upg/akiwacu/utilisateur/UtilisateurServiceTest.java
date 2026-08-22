package bi.ac.upg.akiwacu.utilisateur;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.tontine.TontineRepository;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurModificationRequest;
import bi.ac.upg.akiwacu.utilisateur.dto.UtilisateurRequest;
import bi.ac.upg.akiwacu.utilisateur.mapper.UtilisateurMapperImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurService — CRUD et R1")
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private TontineRepository tontineRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Mapper réel (non mocké) : ce test vérifie aussi que le mapping est correct,
    // pas seulement que le service appelle un mock qui répond n'importe quoi.
    private final UtilisateurMapperImpl utilisateurMapper = new UtilisateurMapperImpl();

    private UtilisateurService utilisateurService;

    @BeforeEach
    void setUp() {
        utilisateurService = new UtilisateurService(
                utilisateurRepository, tontineRepository, utilisateurMapper, passwordEncoder);
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

    private Utilisateur unUtilisateur() {
        Utilisateur utilisateur = Utilisateur.builder()
                .tontine(uneTontine())
                .email("gestionnaire@akiwacu.bi")
                .motDePasse("hash")
                .nom("Nkurunziza")
                .prenom("Alice")
                .actif(true)
                // HashSet mutable : comme le construirait réellement le mapper
                // (versEntite) ou le défaut du builder — Set.of() est immuable et
                // casse mettreAJour(), qui modifie la collection en place.
                .roles(new HashSet<>(Set.of(Role.GESTIONNAIRE)))
                .build();
        utilisateur.setId(9L);
        return utilisateur;
    }

    @Test
    @DisplayName("cas nominal — crée l'utilisateur avec le mot de passe haché et la tontine du contexte")
    void shouldCreateUtilisateurWithHashedPassword() {
        // Arrange
        var requete = new UtilisateurRequest("nouveau@akiwacu.bi", "motdepasse-clair",
                "Bukuru", "Jean", "111", Set.of(Role.TRESORIER));
        when(utilisateurRepository.findByEmail("nouveau@akiwacu.bi")).thenReturn(Optional.empty());
        when(tontineRepository.findById(1L)).thenReturn(Optional.of(uneTontine()));
        when(passwordEncoder.encode("motdepasse-clair")).thenReturn("hash-bcrypt");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var reponse = utilisateurService.creer(requete);

        // Assert
        assertThat(reponse.email()).isEqualTo("nouveau@akiwacu.bi");
        assertThat(reponse.roles()).containsExactly(Role.TRESORIER);
    }

    @Test
    @DisplayName("cas d'erreur métier — refuse un email déjà utilisé")
    void shouldRejectCreationWithDuplicateEmail() {
        // Arrange
        var requete = new UtilisateurRequest("existe@akiwacu.bi", "motdepasse-clair",
                "Bukuru", "Jean", "111", Set.of(Role.TRESORIER));
        when(utilisateurRepository.findByEmail("existe@akiwacu.bi")).thenReturn(Optional.of(unUtilisateur()));

        // Act & Assert
        assertThatThrownBy(() -> utilisateurService.creer(requete))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    @DisplayName("cas nominal — retourne l'utilisateur demandé")
    void shouldReturnUtilisateurById() {
        // Arrange
        when(utilisateurRepository.findById(9L)).thenReturn(Optional.of(unUtilisateur()));

        // Act
        var reponse = utilisateurService.recuperer(9L);

        // Assert
        assertThat(reponse.id()).isEqualTo(9L);
    }

    @Test
    @DisplayName("cas d'exception — lève une exception si l'utilisateur n'existe pas (ou hors tontine, R1)")
    void shouldThrowWhenUtilisateurNotFoundById() {
        // Le filtre Hibernate (D-33) rend un id d'une autre tontine indiscernable
        // d'un id inexistant : findById renvoie déjà vide dans les deux cas.
        when(utilisateurRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.recuperer(404L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal — modifie nom, prénom, téléphone, rôles et statut")
    void shouldUpdateUtilisateurFields() {
        // Arrange
        Utilisateur existant = unUtilisateur();
        var requete = new UtilisateurModificationRequest("Nkurunziza", "Alice-Marie", "222",
                Set.of(Role.ADMIN), false);
        when(utilisateurRepository.findById(9L)).thenReturn(Optional.of(existant));

        // Act
        var reponse = utilisateurService.modifier(9L, requete);

        // Assert
        assertThat(reponse.prenom()).isEqualTo("Alice-Marie");
        assertThat(reponse.actif()).isFalse();
        assertThat(reponse.roles()).containsExactly(Role.ADMIN);
    }

    @Test
    @DisplayName("cas d'exception — lève une exception en modifiant un utilisateur introuvable")
    void shouldThrowWhenUpdatingUnknownUtilisateur() {
        var requete = new UtilisateurModificationRequest("X", "Y", null, Set.of(Role.MEMBRE), true);
        when(utilisateurRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.modifier(404L, requete))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal — désactive sans supprimer")
    void shouldDeactivateInsteadOfDelete() {
        // Arrange
        Utilisateur existant = unUtilisateur();
        when(utilisateurRepository.findById(9L)).thenReturn(Optional.of(existant));

        // Act
        utilisateurService.desactiver(9L);

        // Assert : jamais d'appel à une méthode de suppression du repository.
        assertThat(existant.isActif()).isFalse();
    }

    @Test
    @DisplayName("cas d'exception — lève une exception en désactivant un utilisateur introuvable")
    void shouldThrowWhenDeactivatingUnknownUtilisateur() {
        when(utilisateurRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.desactiver(404L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }
}
