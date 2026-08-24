package bi.ac.upg.akiwacu.cycle.mapper;

import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.cycle.dto.CycleRequest;
import bi.ac.upg.akiwacu.cycle.dto.CycleResponse;
import org.springframework.stereotype.Component;

@Component
public class CycleMapper {

    public Cycle versEntite(CycleRequest request) {
        return Cycle.builder()
                .libelle(request.libelle())
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .montantCotisation(request.montantCotisation())
                .periodicite(request.periodicite())
                .build();
    }

    public void mettreAJour(Cycle cycle, CycleRequest request) {
        cycle.setLibelle(request.libelle());
        cycle.setDateDebut(request.dateDebut());
        cycle.setDateFin(request.dateFin());
        cycle.setMontantCotisation(request.montantCotisation());
        cycle.setPeriodicite(request.periodicite());
    }

    public CycleResponse versReponse(Cycle cycle) {
        return new CycleResponse(cycle.getId(), cycle.getLibelle(), cycle.getDateDebut(), cycle.getDateFin(),
                cycle.getMontantCotisation(), cycle.getPeriodicite(), cycle.getStatut(), cycle.getDateCloture());
    }
}
