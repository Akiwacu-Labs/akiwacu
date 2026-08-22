package bi.ac.upg.akiwacu.tontine;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.tontine.dto.TontineRequest;
import bi.ac.upg.akiwacu.tontine.dto.TontineCreationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TontineService — gestion des tontines")
class TontineServiceTest {

    @Mock
    private TontineRepository tontineRepository;

    @Mock
    private bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TontineService tontineService;

    @AfterEach
    void nettoyerContexteTenant() {
        TenantContext.clear();
    }

    private TontineRequest uneRequete() {
        return new TontineRequest("Twiyungunganye", "Épargne solidaire", LocalDate.of(2026, 8, 21),
                StatutTontine.ACTIVE);
    }

    private TontineCreationRequest uneRequeteCreation() {
        return new TontineCreationRequest("Twiyungunganye", "Épargne solidaire",
                LocalDate.of(2026, 8, 21), StatutTontine.ACTIVE,
                new TontineCreationRequest.AdministrateurCreation(
                        "admin@twiyungunganye.bi", "motdepasse-solide", "Ndayisenga", "Alice", null));
    }

    @Test
    @DisplayName("cas nominal — crée une tontine avec les données fournies")
    void shouldCreateTontine() {
        // Arrange : le nom choisi n'est pas encore utilisé.
        when(tontineRepository.existsByNom("Twiyungunganye")).thenReturn(false);
        when(passwordEncoder.encode("motdepasse-solide")).thenReturn("hash-motdepasse");
        when(tontineRepository.save(any(Tontine.class))).thenAnswer(invocation -> {
            var tontine = invocation.getArgument(0, Tontine.class);
            tontine.setId(7L);
            return tontine;
        });

        // Act
        var reponse = tontineService.creer(uneRequeteCreation());

        // Assert : la réponse contient l'identifiant donné par la base et les champs créés.
        assertThat(reponse.id()).isEqualTo(7L);
        assertThat(reponse.nom()).isEqualTo("Twiyungunganye");
        ArgumentCaptor<Tontine> captor = ArgumentCaptor.forClass(Tontine.class);
        verify(tontineRepository).save(captor.capture());
        assertThat(captor.getValue().getStatut()).isEqualTo(StatutTontine.ACTIVE);
        verify(utilisateurRepository).save(any(bi.ac.upg.akiwacu.utilisateur.Utilisateur.class));
    }

    @Test
    @DisplayName("cas d'erreur métier — refuse la création quand le nom existe déjà")
    void shouldRejectDuplicateTontineName() {
        // Un nom unique évite de confondre deux associations à la création.
        when(tontineRepository.existsByNom("Twiyungunganye")).thenReturn(true);

        assertThatThrownBy(() -> tontineService.creer(uneRequeteCreation()))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("déjà le nom");
    }

    @Test
    @DisplayName("cas d'exception — refuse de consulter une tontine inconnue")
    void shouldThrowWhenTontineDoesNotExist() {
        // Ce test protège contre une réponse 200 vide qui masquerait une mauvaise URL côté client.
        TenantContext.setTontineId(999L);
        when(tontineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tontineService.trouverParId(999L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("R1 — refuse l'accès à une tontine d'un autre tenant")
    void shouldRejectTontineFromAnotherTenant() {
        // Même si l'identifiant existe, il ne doit pas être utilisable depuis un autre JWT.
        TenantContext.setTontineId(7L);

        assertThatThrownBy(() -> tontineService.trouverParId(8L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessageContaining("8");
    }
}
