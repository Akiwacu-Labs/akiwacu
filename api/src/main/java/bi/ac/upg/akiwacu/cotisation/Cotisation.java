package bi.ac.upg.akiwacu.cotisation;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.recu.Recu;
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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PROPRIÉTAIRE : Benitha.
 * `verrouille` porte R8 : dès qu'un reçu est émis pour cette cotisation, le
 * service le passe à true et plus aucune modification n'est autorisée.
 */
@Entity
@Table(name = "cotisations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotisation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @Column(name = "montant", precision = 15, scale = 2, nullable = false)
    private BigDecimal montant;

    @Column(name = "date_cotisation", nullable = false)
    private LocalDate dateCotisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 20, nullable = false)
    private ModePaiement modePaiement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valide_par_id", nullable = false)
    private Utilisateur validePar;

    @Builder.Default
    @Column(name = "verrouille", nullable = false)
    private boolean verrouille = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recu_id")
    private Recu recu;

    /** R8 — vraie dès qu'un reçu a été émis pour cette cotisation. */
    public boolean estVerrouillee() {
        return this.verrouille || this.recu != null;
    }
}
