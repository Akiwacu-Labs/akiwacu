/**
 * PROPRIÉTAIRE : Andy.
 * Déclare le filtre Hibernate de la règle R1 (voir DECISIONS.md D-20).
 * Sa définition vit ici, à un seul endroit ; chaque entité qui veut
 * l'activer porte sa propre annotation {@code @Filter(name = "tontineFilter",
 * condition = "tontine_id = :tontineId")} — voir Utilisateur et Membre.
 * TenantFilterAspect l'active automatiquement sur chaque transaction.
 */
@org.hibernate.annotations.FilterDef(
        name = "tontineFilter",
        parameters = @org.hibernate.annotations.ParamDef(name = "tontineId", type = Long.class))
package bi.ac.upg.akiwacu.common;
