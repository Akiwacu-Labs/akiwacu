package bi.ac.upg.akiwacu.recu;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PROPRIÉTAIRE : Juste (service partagé de génération PDF).
 * `operationId` pointe vers la ligne d'origine (cotisation, prêt ou
 * remboursement) selon `typeOperation` — c'est une référence polymorphe
 * portée en base, pas une clé étrangère JPA. La contrainte unique
 * (type_operation, operation_id) est ce qui rend R8 vérifiable : un reçu
 * existe pour une opération ⇒ cette opération est gelée.
 */
@Entity
@Table(name = "recus", uniqueConstraints = @UniqueConstraint(columnNames = {"type_operation", "operation_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recu extends BaseEntity {

    @Column(name = "numero", length = 30, nullable = false, unique = true)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", length = 20, nullable = false)
    private TypeOperationRecu typeOperation;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @Column(name = "montant", precision = 15, scale = 2, nullable = false)
    private BigDecimal montant;

    @Column(name = "date_emission", nullable = false)
    private Instant dateEmission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emis_par_id", nullable = false)
    private Utilisateur emisPar;

    @Column(name = "chemin_fichier", length = 255)
    private String cheminFichier;

    @Lob
    @Column(name = "contenu_pdf", columnDefinition = "bytea")
    private byte[] contenuPdf;
}
