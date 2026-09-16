---
description: Génère la structure complète d'un domaine métier selon les conventions du projet
---

Crée le squelette complet du domaine `$ARGUMENTS` en suivant **exactement** la
structure décrite dans CLAUDE.md :

1. `<Entite>.java` — entité JPA étendant `BaseEntity`, avec `tontineId` si le
   domaine est multi-tenant (R1). Montants en `BigDecimal`, dates en `LocalDate`.
2. `<Entite>Repository.java` — `JpaRepository`, avec les méthodes de recherche
   **systématiquement filtrées par `tontineId`**.
3. `<Entite>Service.java` — injection par constructeur, `@Transactional` sur les
   méthodes d'écriture. Emplacement de TOUTES les règles métier.
4. `<Entite>Controller.java` — REST pur, aucune logique, annotations OpenAPI
   complètes sur chaque endpoint.
5. `dto/<Entite>Request.java` et `dto/<Entite>Response.java` — des records, avec
   Bean Validation sur la requête.
6. `mapper/<Entite>Mapper.java` — MapStruct.
7. `README.md` — 10 lignes : périmètre, propriétaire, règles métier couvertes.

Puis la classe de test correspondante avec les trois cas obligatoires
(nominal, erreur métier, exception) pour chaque méthode publique du service.

Avant de générer, demande-moi quelles règles R1–R8 s'appliquent à ce domaine si
ce n'est pas évident depuis CLAUDE.md.
