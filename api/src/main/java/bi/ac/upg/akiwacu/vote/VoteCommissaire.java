package bi.ac.upg.akiwacu.vote;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.demandepret.DemandePret;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * PROPRIÉTAIRE : Gloria.
 * La contrainte unique (demande_pret_id, commissaire_id) est ce qui fait
 * réellement R4 : un décompte écrit en Java se contourne avec deux requêtes
 * simultanées, la contrainte en base non. Le contrôle applicatif ne sert
 * qu'à renvoyer un 409 lisible plutôt qu'une erreur SQL brute.
 */
@Entity
@Table(name = "votes_commissaire", uniqueConstraints = @UniqueConstraint(columnNames = {"demande_pret_id", "commissaire_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteCommissaire extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_pret_id", nullable = false)
    private DemandePret demandePret;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commissaire_id", nullable = false)
    private Utilisateur commissaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "sens", length = 10, nullable = false)
    private SensVote sens;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_vote", nullable = false)
    private Instant dateVote;
}
