package bi.ac.upg.akiwacu.dashboard;

import bi.ac.upg.akiwacu.caisse.SensTransaction;
import bi.ac.upg.akiwacu.caisse.TransactionCaisseRepository;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.dashboard.dto.DashboardResponse;
import bi.ac.upg.akiwacu.membre.MembreRepository;
import bi.ac.upg.akiwacu.membre.StatutMembre;
import bi.ac.upg.akiwacu.pret.PretRepository;
import bi.ac.upg.akiwacu.remboursement.RemboursementRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DashboardService {

    private final MembreRepository membreRepository;
    private final CotisationRepository cotisationRepository;
    private final PretRepository pretRepository;
    private final RemboursementRepository remboursementRepository;
    private final TransactionCaisseRepository transactionRepository;
        private final MeterRegistry meterRegistry;
        private final ConcurrentHashMap<String, AtomicReference<Double>> metricValues = new ConcurrentHashMap<>();

    public DashboardService(MembreRepository membreRepository,
                            CotisationRepository cotisationRepository,
                            PretRepository pretRepository,
                            RemboursementRepository remboursementRepository,
                            TransactionCaisseRepository transactionRepository,
                            MeterRegistry meterRegistry) {
        this.membreRepository = membreRepository;
        this.cotisationRepository = cotisationRepository;
        this.pretRepository = pretRepository;
        this.remboursementRepository = remboursementRepository;
        this.transactionRepository = transactionRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public DashboardResponse agregats() {
        Long tontineId = TenantContext.getTontineId();

        long membresActifs = membreRepository.findAll().stream()
                .filter(membre -> membre.getTontine().getId().equals(tontineId))
                .filter(membre -> membre.getStatut() == StatutMembre.ACTIF)
                .count();

        BigDecimal cotisations = cotisationRepository.findAll().stream()
                .filter(cotisation -> cotisation.getCycle().getTontine().getId().equals(tontineId))
                .map(cotisation -> cotisation.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pretsEnCours = pretRepository.findAll().stream()
                .filter(pret -> pret.getCycle().getTontine().getId().equals(tontineId))
                .map(pret -> pret.soldeRestant().max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remboursements = remboursementRepository.findAll().stream()
                .filter(remboursement -> remboursement.getPret().getCycle().getTontine().getId().equals(tontineId))
                .map(remboursement -> remboursement.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal soldeCaisse = transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getTontine().getId().equals(tontineId))
                .map(transaction -> transaction.getSens() == SensTransaction.ENTREE
                        ? transaction.getMontant()
                        : transaction.getMontant().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardResponse response = new DashboardResponse(
                membresActifs, cotisations, pretsEnCours, remboursements, soldeCaisse);
        mettreAJourMetriques(tontineId, response);
        return response;
    }

    private void mettreAJourMetriques(Long tontineId, DashboardResponse response) {
        mettreAJour("akiwacu.membres.total", tontineId, (double) response.membresActifs());
        mettreAJour("akiwacu.cotisations.montant.total", tontineId,
                response.cotisationsTotal().doubleValue());
        mettreAJour("akiwacu.prets.montant.total", tontineId,
                response.pretsEnCours().doubleValue());
        mettreAJour("akiwacu.remboursements.montant.total", tontineId,
                response.remboursementsTotal().doubleValue());
        mettreAJour("akiwacu.solde.caisse", tontineId, response.soldeCaisse().doubleValue());
    }

    private void mettreAJour(String name, Long tontineId, double value) {
        String key = name + ":" + tontineId;
        AtomicReference<Double> reference = metricValues.computeIfAbsent(key, ignored -> {
            AtomicReference<Double> created = new AtomicReference<>(0D);
            Gauge.builder(name, created, current -> current.get())
                    .tag("tontine", tontineId.toString())
                    .register(meterRegistry);
            return created;
        });
        reference.set(value);
    }
}
