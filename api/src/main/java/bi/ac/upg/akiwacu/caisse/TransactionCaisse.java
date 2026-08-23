package bi.ac.upg.akiwacu.caisse;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PROPRIÉTAIRE : Klein.
 * `cycle` est nullable : certains mouvements de caisse sont hors cycle
 * (ex. dépôt initial de la tontine, frais administratifs).
 */
@Entity
@Table(name = "transactions_caisse")
@Filter(name = "tontineFilter", condition = "tontine_id = :tontineId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCaisse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tontine_id", nullable = false)
    private Tontine tontine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id")
    private Cycle cycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "sens", length = 10, nullable = false)
    private SensTransaction sens;

    @Column(name = "montant", precision = 15, scale = 2, nullable = false)
    private BigDecimal montant;

    @Column(name = "motif", length = 255, nullable = false)
    private String motif;

    @Column(name = "date_transaction", nullable = false)
    private LocalDate dateTransaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valide_par_id", nullable = false)
    private Utilisateur validePar;

    @Column(name = "reference_operation", length = 50)
    private String referenceOperation;
}
