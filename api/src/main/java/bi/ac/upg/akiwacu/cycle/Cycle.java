package bi.ac.upg.akiwacu.cycle;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.tontine.Tontine;
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
 * `peutAccueillirOperation()` et `autoriseNouveauPret()` sont la logique
 * derrière R2 et R3 : le service ne fait qu'appeler ces méthodes, il ne
 * réimplémente pas la condition sur `statut`.
 */
@Entity
@Table(name = "cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cycle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tontine_id", nullable = false)
    private Tontine tontine;

    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "montant_cotisation", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantCotisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite", length = 20, nullable = false)
    private Periodicite periodicite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutCycle statut;

    @Column(name = "date_cloture")
    private LocalDate dateCloture;

    /** R2 — une opération financière n'est acceptée que sur un cycle ouvert. */
    public boolean peutAccueillirOperation() {
        return this.statut == StatutCycle.OUVERT;
    }

    /** R3 — aucun prêt si le cycle est gelé ou clôturé. */
    public boolean autoriseNouveauPret() {
        return this.statut == StatutCycle.OUVERT;
    }
}
