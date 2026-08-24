package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.ValidateurCourantService;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.cotisation.dto.CotisationModificationRequest;
import bi.ac.upg.akiwacu.cotisation.mapper.CotisationMapper;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleGuardService;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.recu.RecuService;
import org.junit.jupiter.api.AfterEach;
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
        when(repository.findByIdAndMembreTontineId(8L, 1L)).thenReturn(Optional.of(cotisation));
        var service = new CotisationService(repository, membreRepository, cycleGuard, validateur, recuService,
                new CotisationMapper());

        assertThatThrownBy(() -> service.modifier(8L, new CotisationModificationRequest(
                BigDecimal.ONE, LocalDate.now(), ModePaiement.VIREMENT)))
                .isInstanceOf(RegleMetierException.class).hasMessageContaining("R8");
        verify(repository, never()).save(any());
    }
}
