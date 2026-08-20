package bi.ac.upg.akiwacu.utilisateur;

import bi.ac.upg.akiwacu.common.BaseEntity;
import bi.ac.upg.akiwacu.tontine.Tontine;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import java.util.HashSet;
import java.util.Set;

/**
 * PROPRIÉTAIRE : Andy.
 * Un utilisateur appartient à une seule tontine : tontineId entre dans les
 * claims du JWT et ne doit jamais être lu depuis le corps d'une requête (R1).
 *
 * @Filter reste sans effet tant que TenantFilterAspect ne l'a pas activé —
 * c'est le cas pendant AuthService.login(), qui doit justement pouvoir
 * chercher un email dans toutes les tontines pour déterminer laquelle.
 */
@Entity
@Table(name = "utilisateurs")
@Filter(name = "tontineFilter", condition = "tontine_id = :tontineId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tontine_id", nullable = false)
    private Tontine tontine;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", length = 100, nullable = false)
    private String motDePasse;

    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    @Column(name = "prenom", length = 100, nullable = false)
    private String prenom;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Builder.Default
    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    // EAGER : les rôles sont lus à chaque vérification d'autorisation Spring
    // Security, autant éviter un aller-retour paresseux systématique.
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "utilisateur_roles", joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Set<Role> roles = new HashSet<>();
}
