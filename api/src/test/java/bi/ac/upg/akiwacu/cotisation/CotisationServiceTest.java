package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationBatchRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationModificationRequest;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationRequest;
import bi.ac.upg.akiwacu.cotisation.mapper.CotisationMapper;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.cycle.StatutCycle;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.recu.RecuService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CotisationServiceTest {

    @Mock CotisationRepository repository;
    @Mock MembreRepository membreRepository;
    @Mock CycleGuardService cycleGuard;
    @Mock ValidateurCourantService validateur;
    @Mock RecuService recuService;

    @AfterEach void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("Refuse la modification d'une cotisation déjà reçue")
    void refuseModificationOfReceiptedCotisation() {
        TenantContext.setTontineId(1L);
        var cycle = Cycle.builder().statut(bi.ac.upg.akiwacu.cycle.StatutCycle.OUVERT).build();
        cycle.setId(2L);
        var membre = Membre.builder().build();
        membre.setId(3L);
        var cotisation = Cotisation.builder().cycle(cycle).membre(membre)
                .montant(BigDecimal.TEN).dateCotisation(LocalDate.now()).modePaiement(ModePaiement.ESPECES)
                .verrouille(true).build();
        cotisation.setId(8L);
        when(cycleGuard.assertCycleActif(1L)).thenReturn(cycle);
        when(repository.findByIdAndMembreTontineId(8L, 1L)).thenReturn(Optional.of(cotisation));
        var service = new CotisationService(repository, membreRepository, cycleGuard, validateur, recuService,
                new CotisationMapper());

        assertThatThrownBy(() -> service.modifier(8L, new CotisationModificationRequest(
                BigDecimal.ONE, LocalDate.now(), ModePaiement.VIREMENT)))
                .isInstanceOf(RegleMetierException.class).hasMessageContaining("R8");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Refuse la modification d'une cotisation appartenant à un cycle inactif")
    void refusesModificationFromInactiveCycle() {
        TenantContext.setTontineId(1L);
        var cycleActif = Cycle.builder().statut(StatutCycle.OUVERT).build();
        cycleActif.setId(2L);
        var ancienCycle = Cycle.builder().statut(StatutCycle.GELE).build();
        ancienCycle.setId(3L);
        var membre = Membre.builder().build();
        membre.setId(4L);
        var cotisation = Cotisation.builder().cycle(ancienCycle).membre(membre)
                .montant(BigDecimal.TEN).dateCotisation(LocalDate.now()).modePaiement(ModePaiement.ESPECES)
                .build();
        cotisation.setId(8L);
        when(cycleGuard.assertCycleActif(1L)).thenReturn(cycleActif);
        when(repository.findByIdAndMembreTontineId(8L, 1L)).thenReturn(Optional.of(cotisation));
        var service = new CotisationService(repository, membreRepository, cycleGuard, validateur, recuService,
                new CotisationMapper());

        assertThatThrownBy(() -> service.modifier(8L, new CotisationModificationRequest(
                BigDecimal.ONE, LocalDate.now(), ModePaiement.VIREMENT)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R2");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Signale une cotisation absente ou hors de la tontine")
    void reportsMissingContribution() {
        TenantContext.setTontineId(1L);
        when(repository.findByIdAndMembreTontineId(99L, 1L)).thenReturn(Optional.empty());
        var service = new CotisationService(repository, membreRepository, cycleGuard, validateur, recuService,
                new CotisationMapper());

        assertThatThrownBy(() -> service.recuperer(99L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("Interrompt un lot dès qu'une ligne échoue pour préserver son atomicité")
    void stopsBatchAfterInvalidLine() {
        TenantContext.setTontineId(1L);
        var tontine = bi.ac.upg.akiwacu.tontine.Tontine.builder().build();
        tontine.setId(1L);
        var cycle = Cycle.builder().tontine(tontine).statut(StatutCycle.OUVERT).build();
        cycle.setId(2L);
        var membre = Membre.builder().tontine(tontine).build();
        membre.setId(3L);
        var validateurUtilisateur = bi.ac.upg.akiwacu.utilisateur.Utilisateur.builder().build();
        validateurUtilisateur.setId(4L);
        var first = Cotisation.builder().cycle(cycle).membre(membre)
                .montant(BigDecimal.TEN).dateCotisation(LocalDate.now()).modePaiement(ModePaiement.ESPECES)
                .validePar(validateurUtilisateur).build();
        first.setId(10L);
        when(cycleGuard.assertCycleActif(1L)).thenReturn(cycle);
        when(membreRepository.findById(3L)).thenReturn(Optional.of(membre), Optional.empty());
        when(validateur.obtenir()).thenReturn(validateurUtilisateur);
        when(repository.save(any(Cotisation.class))).thenReturn(first);
        var service = new CotisationService(repository, membreRepository, cycleGuard, validateur, recuService,
                new CotisationMapper());
        var ligne = new CotisationRequest(3L, 2L, BigDecimal.TEN, LocalDate.now(), ModePaiement.ESPECES);

        assertThatThrownBy(() -> service.creerLot(new CotisationBatchRequest(java.util.List.of(ligne, ligne))))
                .isInstanceOf(RessourceIntrouvableException.class);
        verify(repository, times(1)).save(any(Cotisation.class));
        verify(recuService, times(1)).genererPourCotisation(10L);
    }
}
