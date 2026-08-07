# CLAUDE.md — Akiwacu (plateforme de gestion des tontines)

> Ce fichier est lu automatiquement par Claude Code à chaque session.
> Il garantit que les 5 membres de l'équipe produisent du code **cohérent**.
> Toute modification passe par une PR `docs/`.

## Projet

API REST Spring Boot + client web React pour la gestion d'associations d'épargne
et de crédit communautaires (tontines). Projet académique — Bac+4 Génie Logiciel,
Université Polytechnique de Gitega.

**Délai : 8 jours** — du jeudi 6 au jeudi 13 août 2026, soutenance le vendredi 14.
Équipe de 5. Quand tu proposes une approche, privilégie ce qui marche vite et se
défend à l'oral plutôt que ce qui est le plus élégant. Signale ce qui ne tiendra
pas dans le délai.

**L'évaluation est individuelle.** Chaque membre doit pouvoir expliquer son code
à l'oral. Ne génère jamais de code qu'un étudiant de niveau Bac+4 ne pourrait pas
comprendre et défendre. Préfère le simple et l'explicite au clever.

## Stack imposée — ne pas dévier

**Backend :** Java 21 · Spring Boot 3 · Spring Data JPA · Hibernate · Maven ·
Spring Security · JWT · Spring Boot Actuator · Flyway
**Base :** PostgreSQL 16
**Doc :** springdoc-openapi (Swagger UI)
**Monitoring :** Micrometer · Prometheus · Grafana
**Tests :** JUnit 5 · Mockito · AssertJ · Testcontainers · JaCoCo
**Conteneurs :** Docker · Docker Compose
**CI/CD :** GitHub Actions · GHCR · SonarQube · Trivy
**Client :** React 18 · TypeScript · Vite · Tailwind · shadcn/ui · TanStack Query ·
React Hook Form + Zod · client API généré depuis OpenAPI

N'introduis **aucune** autre dépendance sans le demander explicitement.

## Structure du dépôt — MONOREPO

```
akiwacu/
├── api/          ← Spring Boot (pom.xml ICI, pas à la racine)
├── client/       ← React + Vite + TypeScript
├── infra/        ← ansible/ et terraform/  (Klein)
├── monitoring/   ← prometheus.yml et grafana/*.json
├── docs/         ← planning, guides, roles/, adr/
├── bruno/        ← collections .bru (livrable, + export Postman en D7)
└── livrables/    ← schema.sql, rapports exportés (D7-D8)
```

Les commandes Maven se lancent depuis `api/`. Les workflows GitHub Actions
utilisent `defaults.run.working-directory: api` et des filtres de chemin
(`api/**`, `client/**`) pour ne rebuild que ce qui a changé.

## Architecture des packages (dans `api/src/main/java/`)

```
bi.ac.upg.akiwacu
├── config/           # M1 uniquement — SecurityConfig, OpenApiConfig, JpaAuditConfig
├── common/           # M1 — BaseEntity, GlobalExceptionHandler, exceptions métier, TenantContext
├── auth/             # M1
├── utilisateur/      # M1
├── membre/           # M1
├── tontine/          # M3
├── adhesion/         # M3
├── recu/             # M3  (service partagé de génération PDF)
├── cycle/            # M4
├── cotisation/       # M4
├── demandepret/      # M5
├── vote/             # M5
├── pret/             # M5
├── remboursement/    # M2
├── caisse/           # M2
└── dashboard/        # M2
```

Chaque package domaine suit **exactement** cette structure :

```
<domaine>/
├── <Entite>.java                 # @Entity, extends BaseEntity
├── <Entite>Repository.java       # extends JpaRepository
├── <Entite>Service.java          # RÈGLES MÉTIER ICI
├── <Entite>Controller.java       # REST uniquement, aucune logique
├── dto/
│   ├── <Entite>Request.java      # record + validation Bean Validation
│   └── <Entite>Response.java     # record
├── mapper/<Entite>Mapper.java    # MapStruct
└── README.md                     # 10 lignes : périmètre, propriétaire, règles couvertes
```

## Règles métier — R1 à R8

Elles sont le cœur de la notation. **Toujours dans la couche service, jamais dans
le contrôleur.** Chacune a un test unitaire portant le nom indiqué.

| Règle | Énoncé | Propriétaire | Test obligatoire |
|---|---|---|---|
| **R1** | Isolation totale des données par tontine | M1 | `shouldNotAccessDataFromAnotherTontine()` |
| **R2** | Toute opération financière appartient à un cycle actif | M4 | `shouldRejectOperationOnInactiveCycle()` |
| **R3** | Aucun prêt si le cycle est gelé ou clôturé | M4/M5 | `shouldRejectLoanWhenCycleFrozen()` |
| **R4** | Prêt approuvé par ≥ 2 commissaires **distincts** | M5 | `shouldRejectApprovalFromSameCommissionerTwice()` |
| **R5** | Chaque opération financière enregistre le trésorier validateur | M2 | `shouldRecordValidatingTreasurer()` |
| **R6** | Montant du prêt ≤ 3 × épargne du membre | M5 | `shouldRejectLoanExceedingThreeTimesSavings()` |
| **R7** | Échéance du prêt ≤ fin du cycle | M5 | `shouldRejectDueDateAfterCycleEnd()` |
| **R8** | Opération validée avec reçu → non modifiable ni supprimable | M1 | `shouldRejectModificationOfReceiptedOperation()` |

### R1 — non négociable

`tontineId` provient **toujours** du JWT via `TenantContext.getTontineId()`.
**Jamais** du body de la requête, jamais d'un `@PathVariable` non vérifié.
Toute requête repository sur une entité multi-tenant est filtrée par `tontineId`.
Si tu écris une requête qui pourrait retourner des données d'une autre tontine,
c'est un bug de sécurité — signale-le.

### R2 — garde partagée

Appelle `CycleGuardService.assertCycleActif(tontineId)` au début de toute
opération financière (cotisation, prêt, remboursement, transaction de caisse).

## Conventions de code

- **Java 21** : records pour les DTO, `switch` expressions, pattern matching quand ça clarifie
- **Lombok** : `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`. Pas de `@Data` sur les entités.
- **Injection par constructeur** uniquement. Jamais `@Autowired` sur un champ.
- **Montants** : `BigDecimal`, jamais `double` ni `float`. C'est de l'argent.
- **Dates** : `LocalDate` / `LocalDateTime`. Jamais `java.util.Date`.
- **Transactions** : `@Transactional` sur la méthode de service, pas sur le contrôleur.
- **Exceptions** : lever des exceptions métier typées (`RegleMetierException`,
  `RessourceIntrouvableException`, `OperationVerrouilleeException`). Elles sont
  traduites en codes HTTP par `GlobalExceptionHandler`. Ne jamais lever `RuntimeException` nue.
- **Codes HTTP** : 200 lecture · 201 création · 204 suppression · 400 validation ·
  401 non authentifié · 403 rôle insuffisant · 404 introuvable · **409 violation de règle métier**
- **Nommage** : classes en anglais technique (`Service`, `Repository`, `Controller`),
  domaine en français (`Cotisation`, `Pret`, `Remboursement`). Messages d'erreur en français.
- **Aucune valeur en dur.** Tout dans `application.yml`.
- **Aucun secret dans le code.** Variables d'environnement uniquement.

## Conventions de tests

Pattern **AAA** (Arrange-Act-Assert), AssertJ, `@DisplayName` en français.

```java
@Test
@DisplayName("R6 — refuse un prêt supérieur à 3 fois l'épargne du membre")
void shouldRejectLoanExceedingThreeTimesSavings() {
    // Arrange
    var membre = unMembreAvecEpargne(new BigDecimal("100000"));
    var demande = uneDemandeDePret(new BigDecimal("400000"));
    when(cotisationRepository.sommeParMembreEtCycle(any(), any()))
        .thenReturn(new BigDecimal("100000"));

    // Act & Assert
    assertThatThrownBy(() -> pretService.approuver(demande.getId()))
        .isInstanceOf(RegleMetierException.class)
        .hasMessageContaining("R6");
}
```

**Pour chaque endpoint, trois tests minimum :** cas nominal · cas d'erreur métier ·
cas d'exception (ressource introuvable, non autorisé).

- Services : Mockito pur, pas de contexte Spring
- Contrôleurs : `@WebMvcTest` + `MockMvc` + `@MockBean` sur le service
- Repositories : `@DataJpaTest`
- Intégration : Testcontainers PostgreSQL (M2)

**Objectif de couverture : 80 % lignes** sur `service/` et `controller/`.

## OpenAPI

Chaque endpoint est annoté. Sans annotation, la story n'est pas *Done*.

```java
@Operation(summary = "Enregistrer une cotisation",
           description = "Enregistre une cotisation pour un membre sur le cycle actif. Applique R2 et R5.")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Cotisation enregistrée"),
    @ApiResponse(responseCode = "409", description = "Cycle inactif (R2)"),
    @ApiResponse(responseCode = "404", description = "Membre introuvable")
})
```

## Git

- Branches : `feat/<domaine>-<slug>`, `fix/`, `chore/`, `docs/`
- Conventional Commits : `feat(cotisation): ajouter la saisie rapide multi-membres`
- **Jamais de push direct** sur `develop` ni `main`
- **Pas de squash merge** — l'historique individuel est noté
- PR < 400 lignes modifiées

## Client React

- Client API **généré** depuis `/v3/api-docs` — ne jamais écrire un appel `fetch` à la main
- TanStack Query pour tout appel serveur. Pas de `useEffect` + `fetch`.
- React Hook Form + Zod pour tous les formulaires
- shadcn/ui pour les composants. Pas de CSS custom sauf nécessité.
- **Mobile-first** : conçois pour 360 px de large d'abord, puis élargis
- Routes protégées par rôle : ADMIN · GESTIONNAIRE · TRESORIER · COMMISSAIRE · MEMBRE

## Règle sur l'assistance IA

L'énoncé autorise l'IA pour une première version des tests, mais chaque étudiant
**doit pouvoir expliquer chaque ligne à la soutenance**.

Quand tu génères du code :
1. privilégie la lisibilité sur la concision ;
2. commente les passages non évidents **en français** ;
3. n'introduis aucune abstraction qu'un étudiant de Bac+4 ne saurait pas justifier ;
4. quand tu génères un test, explique en 3 lignes ce qu'il vérifie et quel bug il
   attraperait — c'est ce que l'auteur relira en D8 ;
5. si on te demande `/explique`, explique comme à un étudiant interrogé demain.

## Commandes utiles

```bash
cd api && ./mvnw clean verify            # build + tests + JaCoCo
cd api && ./mvnw test -Dtest=PretServiceTest   # un seul test
docker compose up -d                     # PostgreSQL + app en local
docker compose logs -f api               # logs
cd client && pnpm dev                    # client React
cd client && pnpm gen:api                # régénère le client API depuis OpenAPI
```

Swagger local : http://localhost:8080/swagger-ui.html
Actuator : http://localhost:8080/actuator/health · /actuator/prometheus
