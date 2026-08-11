package bi.ac.upg.akiwacu.tontine;

import bi.ac.upg.akiwacu.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * PROPRIÉTAIRE : Juste.
 * Racine de l'isolation multi-tenant (R1) : toute autre entité métier porte
 * un tontineId, directement ou via son parent.
 */
@Entity
@Table(name = "tontines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tontine extends BaseEntity {

    @Column(name = "nom", length = 150, nullable = false, unique = true)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutTontine statut;
}
