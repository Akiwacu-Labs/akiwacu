package bi.ac.upg.akiwacu.common;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * PROPRIÉTAIRE : Andy.
 * Filet de sécurité de R1 : active tontineFilter (voir package-info.java)
 * avant tout appel à une méthode de repository Spring Data, sans que le
 * service appelant y pense. Une requête qui filtre déjà manuellement par
 * tontineId (le motif actuel dans les repositories, ex. CycleRepository)
 * reste protégée deux fois ; celle qui l'oublierait reste protégée quand
 * même.
 *
 * Le point de coupe cible l'EXÉCUTION des méthodes de repository, pas
 * "@annotation(Transactional)" : SimpleJpaRepository porte @Transactional
 * au niveau de la CLASSE, pas de chaque méthode, et un point de coupe
 * @annotation() ne remonte pas fiablement jusqu'à une annotation de classe
 * héritée. Vérifié en pratique (TenantFilterLiveProofTest, base réelle) :
 * la version @annotation(Transactional) ne s'activait jamais sur un appel
 * repository, donc ne filtrait rien silencieusement.
 *
 * S'applique aussi aux méthodes qui tournent avant authentification
 * (AuthService.login() interroge UtilisateurRepository) : TenantContext
 * n'y est pas encore posé, donc on n'active rien plutôt que de lever une
 * exception à chaque login.
 */
@Aspect
@Component
public class TenantFilterAspect {

    private final EntityManager entityManager;

    public TenantFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void activerFiltreTontine() {
        if (!TenantContext.estDefini()) {
            return;
        }
        entityManager.unwrap(Session.class)
                .enableFilter("tontineFilter")
                .setParameter("tontineId", TenantContext.getTontineId());
    }
}
