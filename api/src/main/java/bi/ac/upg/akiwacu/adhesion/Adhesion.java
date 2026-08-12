package bi.ac.upg.akiwacu.adhesion;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.membre.Membre;
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

import java.time.LocalDate;

/**
 * PROPRIÉTAIRE : Juste.
 * Un membre n'adhère qu'une fois par cycle — voir la contrainte unique
 * (membre_id, cycle_id) portée par cette table.
 */
@Entity
@Table(name = "adhesions", uniqueConstraints = @UniqueConstraint(columnNames = {"membre_id", "cycle_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adhesion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    @Column(name = "date_adhesion", nullable = false)
    private LocalDate dateAdhesion;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutAdhesion statut;
}
