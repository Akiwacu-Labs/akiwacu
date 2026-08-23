package bi.ac.upg.akiwacu.dashboard.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long membresActifs,
        BigDecimal cotisationsTotal,
        BigDecimal pretsEnCours,
        BigDecimal remboursementsTotal,
        BigDecimal soldeCaisse) {
}
