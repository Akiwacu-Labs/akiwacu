package bi.ac.upg.akiwacu.demandepret;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretRequest;
import bi.ac.upg.akiwacu.demandepret.dto.DemandePretResponse;
import bi.ac.upg.akiwacu.demandepret.mapper.DemandePretMapper;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.pret.PretService;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DemandePretService — soumission d'une demande")
class DemandePretServiceTest {

    @Mock
    private DemandePretRepository demandePretRepository;
    @Mock
    private MembreRepository membreRepository;
    @Mock
    private CycleGuardService cycleGuardService;
    @Mock
    private PretService pretService;
    @Mock
    private DemandePretMapper demandePretMapper;
    @Mock
    private ValidateurCourantService validateurCourantService;

    @InjectMocks
    private DemandePretService demandePretService;

    @BeforeEach
    void initialiserTenant() {
        TenantContext.setTontineId(1L);
    }

    @AfterEach
    void nettoyerTenant() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Crée une demande sur le cycle actif")
    void shouldCreateLoanRequestOnActiveCycle() {
        var requete = new DemandePretRequest(7L, new BigDecimal("250000"), 3, "Achat de semences");
        var cycle = Cycle.builder().build();
        cycle.setId(10L);
        var tontine = Tontine.builder().build();
        tontine.setId(1L);
        var membre = Membre.builder().tontine(tontine).build();
        membre.setId(7L);
        var utilisateur = utilisateur(42L, tontine);
        membre.setUtilisateur(utilisateur);
        var entite = DemandePret.builder().build();
        var reponse = new DemandePretResponse(15L, 7L, 10L,
                new BigDecimal("250000"), 3, "Achat de semences", null,
                StatutDemandePret.SOUMISE);

        when(cycleGuardService.assertCycleActif(1L)).thenReturn(cycle);
        when(membreRepository.findById(7L)).thenReturn(Optional.of(membre));
        when(validateurCourantService.obtenir()).thenReturn(utilisateur);
        when(demandePretMapper.versEntite(requete)).thenReturn(entite);
        when(demandePretRepository.save(entite)).thenReturn(entite);
        when(demandePretMapper.versReponse(entite)).thenReturn(reponse);

        var resultat = demandePretService.demanderPret(requete);

        assertThat(resultat).isEqualTo(reponse);
        assertThat(entite.getCycle()).isSameAs(cycle);
        assertThat(entite.getMembre()).isSameAs(membre);
        assertThat(entite.getStatut()).isEqualTo(StatutDemandePret.SOUMISE);
        verify(pretService).verifierLimiteMontant(7L, 10L, new BigDecimal("250000"));
        verify(demandePretRepository).save(entite);
    }

    @Test
    @DisplayName("Refuse la demande quand R6 est violée")
    void shouldRejectRequestWhenR6IsViolated() {
        var requete = new DemandePretRequest(7L, new BigDecimal("300001"), 3, "Achat de semences");
        var cycle = Cycle.builder().build();
        cycle.setId(10L);
        var tontine = Tontine.builder().build();
        tontine.setId(1L);
        var membre = Membre.builder().tontine(tontine).build();
        membre.setId(7L);
        var utilisateur = utilisateur(42L, tontine);
        membre.setUtilisateur(utilisateur);
        var violation = new RegleMetierException("R6 : plafond dépassé");

        when(cycleGuardService.assertCycleActif(1L)).thenReturn(cycle);
        when(membreRepository.findById(7L)).thenReturn(Optional.of(membre));
        when(validateurCourantService.obtenir()).thenReturn(utilisateur);
        org.mockito.Mockito.doThrow(violation).when(pretService)
                .verifierLimiteMontant(7L, 10L, new BigDecimal("300001"));

        assertThatThrownBy(() -> demandePretService.demanderPret(requete))
                .isSameAs(violation);

        verify(demandePretRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refuse un membre qui soumet pour le compte d'un autre membre")
    void shouldRejectRequestForAnotherMember() {
        var requete = new DemandePretRequest(7L, new BigDecimal("250000"), 3,
                LocalDate.of(2026, 12, 31), "Achat de semences");
        var cycle = Cycle.builder().dateFin(LocalDate.of(2026, 12, 31)).build();
        cycle.setId(10L);
        var tontine = Tontine.builder().build();
        tontine.setId(1L);
        var membreCible = Membre.builder().tontine(tontine).build();
        membreCible.setId(7L);
        membreCible.setUtilisateur(utilisateur(42L, tontine));
        var utilisateurCourant = utilisateur(99L, tontine);

        when(cycleGuardService.assertCycleActif(1L)).thenReturn(cycle);
        when(membreRepository.findById(7L)).thenReturn(Optional.of(membreCible));
        when(validateurCourantService.obtenir()).thenReturn(utilisateurCourant);

        assertThatThrownBy(() -> demandePretService.demanderPret(requete))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(pretService, never()).verifierLimiteMontant(any(), any(), any());
        verify(demandePretRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lève une exception si le membre est introuvable")
    void shouldThrowWhenMemberNotFound() {
        var requete = new DemandePretRequest(99L, new BigDecimal("100000"), 3, "Achat de semences");
        when(cycleGuardService.assertCycleActif(1L)).thenReturn(Cycle.builder().build());
        when(membreRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> demandePretService.demanderPret(requete))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessageContaining("Membre");

        verify(pretService, never()).verifierLimiteMontant(any(), any(), any());
        verify(demandePretRepository, never()).save(any());
    }

    private Utilisateur utilisateur(Long id, Tontine tontine) {
        var utilisateur = Utilisateur.builder()
                .tontine(tontine)
                .email(id + "@akiwacu.bi")
                .build();
        utilisateur.setId(id);
        return utilisateur;
    }
}
