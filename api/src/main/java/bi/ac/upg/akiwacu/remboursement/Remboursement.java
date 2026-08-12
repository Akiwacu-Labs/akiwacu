package bi.ac.upg.akiwacu.remboursement;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.pret.Pret;
import bi.ac.upg.akiwacu.recu.Recu;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PROPRIÉTAIRE : Klein.
 * `verrouille` porte R8, comme sur Cotisation et Pret.
 */
@Entity
@Table(name = "remboursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Remboursement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pret_id", nullable = false)
    private Pret pret;

    @Column(name = "montant", precision = 15, scale = 2, nullable = false)
    private BigDecimal montant;

    @Column(name = "date_remboursement", nullable = false)
    private LocalDate dateRemboursement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valide_par_id", nullable = false)
    private Utilisateur validePar;

    @Builder.Default
    @Column(name = "verrouille", nullable = false)
    private boolean verrouille = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recu_id")
    private Recu recu;
}
