package bi.ac.upg.akiwacu.pret;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.demandepret.DemandePret;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.recu.Recu;
import bi.ac.upg.akiwacu.remboursement.Remboursement;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PROPRIÉTAIRE : Gloria.
 * Intérêts : taux forfaitaire mensuel sur le capital, pas de capitalisation
 * ni d'amortissement dégressif — voir DECISIONS.md D-25.
 */
@Entity
@Table(name = "prets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pret extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_pret_id", nullable = false, unique = true)
    private DemandePret demandePret;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    @Column(name = "montant_accorde", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantAccorde;

    @Builder.Default
    @Column(name = "taux_interet", precision = 5, scale = 2, nullable = false)
    private BigDecimal tauxInteret = BigDecimal.ZERO;

    @Column(name = "duree_mois", nullable = false)
    private Integer dureeMois;

    @Column(name = "date_deblocage", nullable = false)
    private LocalDate dateDeblocage;

    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutPret statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valide_par_id", nullable = false)
    private Utilisateur validePar;

    @Builder.Default
    @Column(name = "verrouille", nullable = false)
    private boolean verrouille = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recu_id")
    private Recu recu;

    @Builder.Default
    @OneToMany(mappedBy = "pret", fetch = FetchType.LAZY)
    private List<Remboursement> remboursements = new ArrayList<>();

    /** montantAccorde + (montantAccorde × tauxInteret / 100 × dureeMois) — D-25. */
    public BigDecimal montantDu() {
        BigDecimal interets = montantAccorde
                .multiply(tauxInteret)
                .multiply(BigDecimal.valueOf(dureeMois))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return montantAccorde.add(interets);
    }

    /** montantDu() − somme des remboursements déjà enregistrés. */
    public BigDecimal soldeRestant() {
        BigDecimal totalRembourse = remboursements.stream()
                .map(Remboursement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return montantDu().subtract(totalRembourse);
    }

    /** échéance dépassée et solde non nul. */
    public boolean estEnRetard(LocalDate jour) {
        return jour.isAfter(dateEcheance) && soldeRestant().compareTo(BigDecimal.ZERO) > 0;
    }
}
