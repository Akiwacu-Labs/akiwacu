# TUTO 30 MIN — Benitha — Cycles et cotisations (R2, R5, R8)

> Format : tutoriel complet. Chaque étape = **l'idée** → **l'appel REST** → **le code réel** → **ce que ça prouve**.
> Complément de `04-benitha-cycles-cotisations.md` (le plan temps à temps).

---

## 0. Vos 30 minutes, minute par minute

| Temps | Contenu | Support |
|---|---|---|
| 00:00–03:00 | Cas du trésorier : saisir des cotisations vite, sans se tromper de groupe. | schéma trésorier |
| 03:00–08:00 | La machine à états du cycle : `OUVERT → GELE → CLOTURE`, transitions interdites. | diagramme d'états |
| 08:00–13:00 | JEE/Spring : entité `Cycle`, `CycleService`, la garde partagée `CycleGuardService`, repositories JPA. | code projeté |
| 13:00–18:00 | REST : endpoints cycle/cotisation, DTO, validation, codes HTTP. | Swagger |
| 18:00–24:00 | **Démo** : cotisation normale, batch (saisie rapide), gel du cycle, nouvelle cotisation → **409**. | Swagger + terminal |
| 24:00–27:00 | Tests R2/R5, isolation tenant, lien avec le reçu (Juste). | test outputs |
| 27:00–30:00 | Questions + relais vers Gloria (prêts) et Klein (caisse/monitoring). | — |

---

## 1. Le vocabulaire du domaine

| Terme | Définition simple | Dans le projet |
|---|---|---|
| **Cycle** | Une période de fonctionnement de la tontine : début, fin, et un état. Chaque opération financière (cotisation, prêt, remboursement, caisse) doit appartenir au cycle **actif**. | `Cycle.java`, `StatutCycle.java` |
| **Machine à états** | Un objet ne change d'état que par des transitions autorisées. Ici : `OUVERT → GELE`, `GELE → OUVERT` ou `GELE → CLOTURE`. | `CycleService.changerStatut()` |
| **R2** | « Toute opération financière appartient à un cycle actif ». Une opération sur un cycle gelé/clôturé est refusée → **409**. | `CycleGuardService.assertCycleActif` |
| **R5** | Chaque opération enregistre le trésorier validateur. | `ValidateurCourantService` |
| **Vehicle de check (R8)** | Un reçu verrouille l'opération → modification interdite (409). | `Cotisation.estVerrouillee()` |
| **Garde partagée** | Un seul service centralise la définition « cycle actif » réutilisée par cotisation, prêt, remboursement, caisse. | `CycleGuardService` |
| **Batch** | Un endpoint qui accepte plusieurs cotisations en un seul appel (saisie rapide du trésorier). | `POST /api/cotisations/batch` |

---

## 2. Le cœur du domaine : la machine à états du cycle

### Le diagramme (à dessiner ou projeter)

```
                 gel
   ┌──────────────────────────────┐
   │                              ▼
 [ OUVERT ]       [ GELE ]  ──clôture──► [ CLOTURE ]
   │  ▲                             ▲
   │  └──────── réouvrir ───────────┘
   │
   └── rien d'autre n'est permis
```

### Règles de transition implémentées (exactement)

`api/src/main/java/bi/ac/upg/akiwacu/cycle/CycleService.java` :

```java
@Transactional
public CycleResponse changerStatut(Long id, StatutCycle cible) {
    Cycle cycle = trouver(id);
    StatutCycle precedent = cycle.getStatut();

    if (precedent == cible) {
        throw new RegleMetierException("Le cycle possède déjà ce statut");
    }
    if (precedent == StatutCycle.CLOTURE) {
        throw new RegleMetierException("Un cycle clôturé ne peut pas être rouvert");
    }
    if (precedent == StatutCycle.OUVERT && cible != StatutCycle.GELE) {
        throw new RegleMetierException(
            "Transition de cycle non autorisée : un cycle ouvert doit d'abord être gelé");
    }
    if (precedent == StatutCycle.GELE && cible != StatutCycle.CLOTURE
            && cible != StatutCycle.OUVERT) {
        throw new RegleMetierException("Transition de cycle non autorisée");
    }
    if (precedent == StatutCycle.GELE && cible == StatutCycle.OUVERT
            && cycleRepository.existsByTontineIdAndStatut(
                TenantContext.getTontineId(), StatutCycle.OUVERT)) {
        throw new RegleMetierException("Un seul cycle peut être ouvert par tontine");
    }
    if (cible == StatutCycle.CLOTURE) {
        CycleActiveLoanPort port = activeLoanPort.getIfAvailable();
        if (port == null) {
            throw new RegleMetierException(
                "R3 : clôture indisponible tant que l'adaptateur des prêts actifs n'est pas branché");
        }
        if (port.existePretActif(cycle.getId(), TenantContext.getTontineId())) {
            throw new RegleMetierException("R3 : impossible de clôturer un cycle avec des prêts actifs");
        }
        cycle.setDateCloture(LocalDate.now());
    }
    cycle.setStatut(cible);
    return cycleMapper.versReponse(cycle);
}
```

> **À dire :** « Trois leçons : (1) TOUTE transition passe par ce service, aucun
> endpoint ne touche la base directement. (2) Une transition interdite lève
> `RegleMetierException` → HTTP 409. (3) Clôturer un cycle avec des prêts actifs est
> refusé (R3), grâce à un port `CycleActiveLoanPort` branché sur le domaine prêt. »

### La création — un seul cycle ouvert par tontine

```java
@Transactional
public CycleResponse creer(CycleRequest request) {
    Long tontineId = TenantContext.getTontineId();
    verifierDates(request);
    if (cycleRepository.existsByTontineIdAndStatut(tontineId, StatutCycle.OUVERT)) {
        throw new RegleMetierException("Un seul cycle peut être ouvert par tontine");
    }
    Cycle cycle = cycleMapper.versEntite(request);
    cycle.setTontine(tontineRepository.findById(tontineId)
            .orElseThrow(() -> new RessourceIntrouvableException("Tontine introuvable")));
    cycle.setStatut(StatutCycle.OUVERT);
    return cycleMapper.versReponse(cycleRepository.save(cycle));
}
```

---

## 3. La garde partagée (votre chef-d'œuvre réutilisé par tout le monde)

`api/src/main/java/bi/ac/upg/akiwacu/cycle/CycleGuardService.java` :

```java
@Service
@RequiredArgsConstructor
public class CycleGuardService {

    private final CycleRepository cycleRepository;

    /**
     * Vérifie qu'un cycle {@link StatutCycle#OUVERT} existe pour cette tontine.
     *
     * @param tontineId identifiant de la tontine issu du contexte authentifié
     * @return le cycle ouvert à utiliser par l'opération appelante
     * @throws RegleMetierException si aucun cycle actif n'existe — R2 (HTTP 409)
     */
    public Cycle assertCycleActif(Long tontineId) {
        return cycleRepository.findByTontineIdAndStatut(tontineId, StatutCycle.OUVERT)
                .orElseThrow(() -> new RegleMetierException(
                        "R2 : aucun cycle actif pour cette tontine, l'opération est impossible"));
    }
}
```

> **À dire :** « R2 n'est pas dupliquée : cotisation, prêt, remboursement et caisse
> appellent TOUS `assertCycleActif` au début de leur opération. Une seule définition,
> un seul message d'erreur — impossible que deux services décident différemment de
> ce qu'est un cycle actif. »

---

## 4. Démo 1 — Enregistrer une cotisation (R2 + R5 + reçu)

### L'appel REST
```
POST /api/cotisations
Authorization: Bearer <jeton>
Content-Type: application/json

{
  "cycleId": 1,
  "membreId": 2,
  "montant": 25000,
  "dateCotisation": "2026-09-16",
  "modePaiement": "ESPECES"
}
```

### La réponse réelle (structure de `CotisationResponse`)
```json
201 Created
{
  "id": 12,
  "membreId": 2,
  "cycleId": 1,
  "montant": 25000,
  "dateCotisation": "2026-09-16",
  "modePaiement": "ESPECES",
  "valideParId": 7,
  "verrouille": true,
  "recuId": 5
}
```
> `valideParId` = le trésorier authentifié (R5). `verrouille: true` + `recuId` = le reçu
> a été émis → l'opération ne peut plus être modifiée (R8).

### Le code réel du service

`api/src/main/java/bi/ac/upg/akiwacu/cotisation/CotisationService.java` :

```java
@Transactional
public CotisationResponse creer(CotisationRequest request) {
    Cycle cycle = cycleGuardService.assertCycleActif(TenantContext.getTontineId());  // R2
    verifierCycleDemande(cycle, request.cycleId());
    Membre membre = trouverMembre(request.membreId());
    verifierMemeTontine(membre, cycle);                                              // R1
    Cotisation cotisation = Cotisation.builder()
            .cycle(cycle)
            .membre(membre)
            .montant(request.montant())
            .dateCotisation(request.dateCotisation())
            .modePaiement(request.modePaiement())
            .validePar(validateurCourantService.obtenir())                          // R5
            .build();
    Cotisation saved = cotisationRepository.save(cotisation);
    recuService.genererPourCotisation(saved.getId());                               // reçu PDF + verrou
    return cotisationMapper.versReponse(saved);
}
```

### Les 4 vérifications à montrer une par une
1. **R2** : `assertCycleActif` → cycle ouvert exigé, sinon 409.
2. **R1** : `verifierMemeTontine` → le membre et le cycle appartiennent bien à la tontine du jeton.
3. **R5** : `validateurCourantService.obtenir()` → le trésorier vient de la sécurité, pas du client.
4. **Reçu** : `recuService.genererPourCotisation` → un PDF est émis et l'opération est **verrouillée** (R8, cf. tuto Juste).

```java
private void verifierMemeTontine(Membre membre, Cycle cycle) {
    Long tenant = TenantContext.getTontineId();
    if (!tenant.equals(membre.getTontine().getId()) || !tenant.equals(cycle.getTontine().getId())) {
        throw new RessourceIntrouvableException("Ressource hors de la tontine courante");
    }
}
```

---

## 5. Démo 2 — Le batch (saisie rapide multi-membres)

### L'appel REST
```
POST /api/cotisations/batch
Authorization: Bearer <jeton>
Content-Type: application/json

{
  "cotisations": [
    { "membreId": 2,  "cycleId": 1, "montant": 25000, "dateCotisation": "2026-09-16", "modePaiement": "ESPECES" },
    { "membreId": 3,  "cycleId": 1, "montant": 25000, "dateCotisation": "2026-09-16", "modePaiement": "ESPECES" },
    { "membreId": 17, "cycleId": 1, "montant": 25000, "dateCotisation": "2026-09-16", "modePaiement": "MOBILE_MONEY" }
  ]
}
```
(chaque ligne est un `CotisationRequest` complet : le lot réutilise exactement le même DTO que l'endpoint simple)

### Le code réel — réutilisation pure

```java
@Transactional
public List<CotisationResponse> creerLot(CotisationBatchRequest request) {
    return request.cotisations().stream().map(this::creer).toList();
}
```

**À dire :** « Le batch n'invente PAS une seconde logique : il réutilise `creer()`
pour chaque ligne → mêmes règles R2/R1/R5, même génération de reçu. Une ligne en
erreur = 409, et toute la transaction est reportée. C'est exactement le besoin du
trésorier qui saisit 30 membres à la réunion. »

---

## 6. Démo 3 — Geler le cycle, puis tenter une cotisation → HTTP 409

### Étape A — geler le cycle
```
PATCH /api/cycles/1/statut
Authorization: Bearer <jeton>
Content-Type: application/json

{ "statut": "GELE" }
```
(requête réelle `CycleStatutRequest` du projet : le body porte uniquement le statut cible ;
endpoint réservé à `ADMIN`/`GESTIONNAIRE`)

### Étape B — la nouvelle cotisation est refusée
```
POST /api/cotisations
Authorization: Bearer <jeton>
Content-Type: application/json

{ "cycleId": 1, "membreId": 2, "montant": 25000, "dateCotisation": "2026-09-16" }
```

### La réponse réelle du serveur
```json
409 Conflict
{
  "status": 409,
  "error": "Conflict",
  "message": "R2 : aucun cycle actif pour cette tontine, l'opération est impossible",
  "path": "/api/cotisations"
}
```

> **CAPTURE** : le 409 avec le message R2 complet. C'est LE point de démo qui montre
> qu'une règle métier n'est pas une annotation mais du code exécuté dans le service.

### Et côté modification — le double verrou R2 + R8

```java
@Transactional
public CotisationResponse modifier(Long id, CotisationModificationRequest request) {
    Cycle cycleActif = cycleGuardService.assertCycleActif(TenantContext.getTontineId());  // R2
    Cotisation cotisation = trouver(id);
    if (!cycleActif.getId().equals(cotisation.getCycle().getId())) {
        throw new RegleMetierException("R2 : une cotisation d'un cycle inactif ne peut pas être modifiée");
    }
    if (cotisation.estVerrouillee()) {
        throw new RegleMetierException("R8 : une cotisation ayant un reçu ne peut pas être modifiée");
    }
    cotisation.setMontant(request.montant());
    cotisation.setDateCotisation(request.dateCotisation());
    cotisation.setModePaiement(request.modePaiement());
    return cotisationMapper.versReponse(cotisation);
}
```

---

## 7. Le contrôleur — REST pur, sans logique

`api/src/main/java/bi/ac/upg/akiwacu/cotisation/CotisationController.java` :

```java
@RestController
@RequestMapping("/api/cotisations")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'TRESORIER')")
public class CotisationController {

    private final CotisationService cotisationService;

    @PostMapping
    @Operation(summary = "Enregistrer une cotisation", description = "R2, R5 et génération du reçu.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cotisation et reçu créés"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Cycle inactif ou règle métier violée")})
    public ResponseEntity<CotisationResponse> creer(@Valid @RequestBody CotisationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotisationService.creer(request));
    }

    @PostMapping("/batch")
    @Operation(summary = "Enregistrer un lot de cotisations",
               description = "Transaction atomique avec un reçu par ligne.")
    public ResponseEntity<List<CotisationResponse>> creerLot(@Valid @RequestBody CotisationBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotisationService.creerLot(request));
    }
}
```

> **À dire :** « Zéro règle métier dans le contrôleur : il reçoit HTTP, valide le DTO,
> appelle le service, et répond le bon code 201/400/409. `@PreAuthorize` restreint à
> ADMIN/GESTIONNAIRE/TRESORIER — un MEMBRE reçoit 403. »

---

## 8. L'inventaire de VOS fichiers

| Fichier | Contenu réel résumé |
|---|---|
| `cycle/Cycle.java` | `@Entity` : dateDebut, dateFin, statut, tontine ; méthodes comportement (ex. `peutAccueillirOperation()`). |
| `cycle/StatutCycle.java` | enum `OUVERT`, `GELE`, `CLOTURE`. |
| `cycle/CycleRepository.java` | `JpaRepository` + `findByTontineIdAndStatut`, `existsByTontineIdAndStatut`, `findByIdAndTontineId`. |
| `cycle/CycleService.java` | création (1 cycle ouvert max), transitions d'état, clôture R3 via port. |
| `cycle/CycleGuardService.java` | **R2 pour toute l'app** : `assertCycleActif(tontineId)`. |
| `cycle/CycleController.java`, `dto/*`, `mapper/CycleMapper.java` | REST + records + MapStruct. |
| `cycle/CycleActiveLoanPort.java` / `PretActiveLoanAdapter.java` | le port branché sur le domaine prêt pour la clôture R3. |
| `cycle/Periodicite.java` | périodicité du cycle. |
| `cotisation/Cotisation.java` | `@Entity` : montant, date, modePaiement, membre, cycle, validePar, recu, verrouille ; `estVerrouillee()`. |
| `cotisation/CotisationRepository.java` | `findAllByMembreTontineId`, `findByIdAndMembreTontineId`, `sommeParMembreEtCycle` (utilisé pour R6). |
| `cotisation/CotisationService.java` | créer/batch/modifier/lister avec R2, R1, R5, R8 + reçu. |
| `cotisation/CotisationController.java` | endpoints REST + `@PreAuthorize` + Swagger. |
| `cotisation/ModePaiement.java`, `dto/*`, `mapper/CotisationMapper.java` | enum + records + MapStruct. |

---

## 9. Correspondance ÉNONCÉ → votre démonstration

| L'énoncé demande | Votre preuve |
|---|---|
| « R2 : opération dans un cycle actif » | `CycleGuardService.assertCycleActif` + test `shouldRejectOperationOnInactiveCycle()`. Démo 409 après gel. |
| « R3 : aucun prêt si cycle gelé/clôturé » | transitions `changerStatut` + port R3 sur la clôture ; démo clôture refusée si prêt actif. |
| « R5 : trésorier validateur » | `validateurCourantService.obtenir()` posé sur chaque cotisation + test `shouldRecordValidatingTreasurer()` (avec Klein). |
| « R8 : opération reçue verrouillée » | `estVerrouillee()` + `RegleMetierException` sur `modifier` → 409. |
| « Entités, services, contrôleurs, tests » | `CycleTest`, `CycleGuardServiceTest`, `CotisationServiceTest`, `CotisationControllerTest`. |
| « Saisie multi-lignes (cas métier) » | endpoint `POST /api/cotisations/batch`. |
| « REST bien formé » | 201 création, 409 conflit, 403 rôle, DTO validés, `@PreAuthorize`. |

---

## 10. Les pièges et questions probables

- **« Pourquoi `OUVERT → GELE → CLOTURE` et pas directement ouvert→clôturé ? »** →
  Geler suspend les opérations SANS perdre l'historique ; clôturer termine
  définitivement. La transition directe ouverte→clôturé est interdite pour forcer
  l'étape de gel (verrouillage propre). Tranquille aussi : un gelé peut revenir
  à OUVERT si aucun autre cycle ouvert n'existe.
- **« Un montant peut-il venir du client ? »** → Il entre par le DTO mais est validé
  par le serveur (`@Positive`, `BigDecimal`), rattaché au cycle/membre AUTHENTIFIÉS.
  Le client ne choisit jamais le périmètre (R1).
- **« Pourquoi la garde est-elle un service à part ? »** → Parce que 4 domaines en ont
  besoin. La mettre dans `CycleService` forcerait les autres à dépendre de tout un
  service ; la mettre dans un service dédié garde une dépendance minimale et testable.
- **« C'est quoi le batch d'un point de vue base ? »** → `@Transactional` sur
  `creerLot` : si un membre est introuvable en ligne 14 sur 30, AUCUNE ligne n'est
  sauvegardée (tout ou rien).

---

> **Prérequis démo** : API + PostgreSQL démarrés, un cycle OUVERT créé, au moins deux
> membres, un compte TRESORIER/GESTIONNAIRE pour passer `@PreAuthorize`.
>
> **Alertes honnêtes** : le client React « Le Compteur » comporte des écrans mais
> beaucoup de fichiers locaux sont non commités (branche `docs/planning-phase-c-d`).
> Montrer l'API comme preuve principale ; le client seulement s'il build et son
> parcours sont confirmés.