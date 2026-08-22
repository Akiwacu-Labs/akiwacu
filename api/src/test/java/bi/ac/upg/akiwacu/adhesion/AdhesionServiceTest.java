package bi.ac.upg.akiwacu.adhesion;

import bi.ac.upg.akiwacu.adhesion.dto.AdhesionModificationRequest;
import bi.ac.upg.akiwacu.adhesion.dto.AdhesionRequest;
import bi.ac.upg.akiwacu.adhesion.mapper.AdhesionMapper;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.CycleRepository;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.tontine.Tontine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdhesionServiceTest {

    @Mock private AdhesionRepository adhesionRepository;
    @Mock private MembreRepository membreRepository;
    @Mock private CycleRepository cycleRepository;
    @Spy private AdhesionMapper adhesionMapper = new AdhesionMapper();
    @InjectMocks private AdhesionService adhesionService;

    @AfterEach
    void nettoyerTenant() { TenantContext.clear(); }

    @Test
    void shouldCreateActiveAdhesion() {
        TenantContext.setTontineId(7L);
        Membre membre = unMembre(7L);
        Cycle cycle = unCycle(7L);
        when(membreRepository.findById(3L)).thenReturn(Optional.of(membre));
        when(cycleRepository.findById(5L)).thenReturn(Optional.of(cycle));
        when(adhesionRepository.existsByMembreIdAndCycleIdAndMembreTontineId(3L, 5L, 7L)).thenReturn(false);
        when(adhesionRepository.save(any(Adhesion.class))).thenAnswer(invocation -> {
            Adhesion adhesion = invocation.getArgument(0);
            adhesion.setId(9L);
            return adhesion;
        });

        var response = adhesionService.creer(new AdhesionRequest(3L, 5L, LocalDate.of(2026, 8, 22)));

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.statut()).isEqualTo(StatutAdhesion.ACTIVE);
    }

    @Test
    void shouldRejectDuplicateAdhesion() {
        TenantContext.setTontineId(7L);
        when(membreRepository.findById(3L)).thenReturn(Optional.of(unMembre(7L)));
        when(cycleRepository.findById(5L)).thenReturn(Optional.of(unCycle(7L)));
        when(adhesionRepository.existsByMembreIdAndCycleIdAndMembreTontineId(3L, 5L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> adhesionService.creer(new AdhesionRequest(3L, 5L, LocalDate.now())))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void shouldRejectResourcesFromAnotherTontine() {
        TenantContext.setTontineId(7L);
        when(membreRepository.findById(3L)).thenReturn(Optional.of(unMembre(8L)));
        when(cycleRepository.findById(5L)).thenReturn(Optional.of(unCycle(8L)));

        assertThatThrownBy(() -> adhesionService.creer(new AdhesionRequest(3L, 5L, LocalDate.now())))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void shouldRejectUnknownAdhesion() {
        TenantContext.setTontineId(7L);
        when(adhesionRepository.findByIdAndMembreTontineId(404L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adhesionService.recuperer(404L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void shouldModifyAdhesionStatus() {
        TenantContext.setTontineId(7L);
        Adhesion adhesion = Adhesion.builder().membre(unMembre(7L)).cycle(unCycle(7L))
                .dateAdhesion(LocalDate.now()).statut(StatutAdhesion.ACTIVE).build();
        when(adhesionRepository.findByIdAndMembreTontineId(9L, 7L)).thenReturn(Optional.of(adhesion));

        var response = adhesionService.modifier(9L,
                new AdhesionModificationRequest(LocalDate.of(2026, 8, 22), StatutAdhesion.CLOTUREE));

        assertThat(response.statut()).isEqualTo(StatutAdhesion.CLOTUREE);
    }

    private Membre unMembre(Long tontineId) {
        Tontine tontine = Tontine.builder().nom("Tontine").build();
        tontine.setId(tontineId);
        Membre membre = Membre.builder().tontine(tontine).nom("Nom").prenom("Prenom").telephone("70000000").build();
        membre.setId(3L);
        return membre;
    }

    private Cycle unCycle(Long tontineId) {
        Tontine tontine = Tontine.builder().nom("Tontine").build();
        tontine.setId(tontineId);
        Cycle cycle = Cycle.builder().tontine(tontine).libelle("Cycle 2026").build();
        cycle.setId(5L);
        return cycle;
    }
}
