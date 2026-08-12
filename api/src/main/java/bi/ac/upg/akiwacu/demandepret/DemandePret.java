package bi.ac.upg.akiwacu.demandepret;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.vote.SensVote;
import bi.ac.upg.akiwacu.vote.VoteCommissaire;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PROPRIÉTAIRE : Gloria.
 * `quorumAtteint()` porte R4. La contrainte unique (demande_pret_id,
 * commissaire_id) sur `votes_commissaire` garantit déjà qu'un commissaire ne
 * vote qu'une fois ; on recompte quand même les commissaires distincts ici
 * pour que la règle métier soit lisible directement dans le code Java.
 */
@Entity
@Table(name = "demandes_pret")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandePret extends BaseEntity {

    private static final int QUORUM_MINIMUM = 2;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @Column(name = "montant_demande", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantDemande;

    @Column(name = "duree_mois", nullable = false)
    private Integer dureeMois;

    @Column(name = "motif", columnDefinition = "TEXT", nullable = false)
    private String motif;

    @Column(name = "date_demande", nullable = false)
    private LocalDate dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutDemandePret statut;

    @Builder.Default
    @OneToMany(mappedBy = "demandePret", fetch = FetchType.LAZY)
    private List<VoteCommissaire> votes = new ArrayList<>();

    /** R4 — au moins deux commissaires distincts ont voté POUR. */
    public boolean quorumAtteint() {
        long commissairesPour = votes.stream()
                .filter(vote -> vote.getSens() == SensVote.POUR)
                .map(vote -> vote.getCommissaire().getId())
                .distinct()
                .count();
        return commissairesPour >= QUORUM_MINIMUM;
    }
}
