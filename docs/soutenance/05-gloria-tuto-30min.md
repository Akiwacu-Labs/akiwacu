# TUTO 30 MIN — Gloria — Demandes de prêt, votes et règles métier (R4, R6, R7, R2, R1)

> Format : tutoriel complet. Chaque étape = **l'idée** → **l'appel REST** → **le code réel** → **ce que ça prouve**.
> Complément de `05-gloria-prets-regles.md` (le plan temps à temps).

---

## 0. Vos 30 minutes, minute par minute

| Temps | Contenu | Support |
|---|---|---|
| 00:00–03:00 | Problème métier : demander un prêt, le faire voter aux commissaires, le débloquer, le suivre. | schéma du workflow |
| 03:00–08:00 | Modèle : `DemandePret`, `VoteCommissaire`, `Pret` — relations + quorum. | diagramme entités |
| 08:00–13:00 | REST : endpoints demande, votes, décision, prêt, échéancier, filtres par statut. | Swagger |
| 13:00–22:00 | **Démo complète** : demande → vote POUR commissaire 1 → vote refusé du même (R4) → vote POUR commissaire 2 → demande **APPROUVEE** → déblocage → prêt + échéancier. | terminal + Swagger |
| 22:00–26:00 | **Démo des refus** : R6 (montant > 3 × épargne), R7 (échéance > fin de cycle) → 409. | terminal |
| 26:00–28:00 | Tests, isolation tenant (R1), sécurité des rôles. | test outputs |
| 28:00–30:00 | Questions + relais vers le reçu PDF (Juste) et la CI/CD (Klein). | — |

---

## 1. Le vocabulaire du domaine

| Terme | Définition simple | Dans le projet |
|---|---|---|
| **Demande de prêt (SOUMISE)** | La requête d'un membre pour obtenir un prêt : montant, durée en mois, échéance. | `DemandePret.java`, `StatutDemandePret` |
| **Commissaire** | Un membre habilité à voter POUR ou CONTRE. | `VoteCommissaire.java`, rôle utilisateur |
| **Quorum (R4)** | ≥ 2 votes POUR de commissaires **distincts** pour approuver ; ≥ 2 CONTRE pour rejeter. | `VoteService.mettreAJourStatut()` |
| **Déblocage** | Le trésorier transforme une demande APPROUVEE en prêt réel (argent qui sort). | `PretService.debloquerPret()` |
| **Échéancier** | La vue « combien reste à payer, quelle est l'échéance ». | `PretService.echeancier()` |
| **R6** | Montant du prêt ≤ 3 × l'épargne du membre sur le cycle actif. | `PretService.verifierLimiteMontant()` |
| **R7** | L'échéance du prêt ≤ la fin du cycle. | `PretService.verifierEcheance()` |
| **R2 déléguée** | Pas question de décider soi-même « cycle actif » : on appelle `CycleGuardService` de Benitha. | `DemandePretService` |

---

## 2. Le workflow en une image

```
membre                     commissaires                     trésorier
  │                            │                               │
  ▼                            ▼                               ▼
demande          votes POUR/CONTRE (distincts)
  SOUMISE        ├─ vote #1 POUR ────────────────┐
                 ├─ vote #2 POUR (même commiss.) ─┤  R4 → 409     (1 seul vote / commissaire)
                 ├─ vote #3 POUR (autre) ─────────┤  quorum ≥2 → APPROUVEE
                 └─ vote #4 CONTRE ───────────────┘  (≥2 CONTRE → REJETEE)
                                                      │
                                                      ▼
                                             déblocage (R3 + R5)
                                                      │
                                                      ▼
                                             Pret ACTIF + reçu PDF
                                                      │
                                                      ▼
                                             échéancier / remboursements
```

---

## 3. Démo 1 — Créer une demande de prêt (R2, R1, R6, R7)

### L'appel REST
```
POST /api/demandes-pret
Authorization: Bearer <jeton>
Content-Type: application/json

{
  "membreId": 2,
  "montantDemande": 300000,
  "dureeMois": 6,
  "dateEcheance": "2026-12-31",
  "motif": "Construction d'une maison"
}
```

### Le code réel — D'où viennent les contrôles

`api/src/main/java/bi/ac/upg/akiwacu/demandepret/DemandePretService.java` :

```java
@Transactional
public DemandePretResponse demanderPret(DemandePretRequest requete) {
    Long tontineId = TenantContext.getTontineId();
    Cycle cycleActif = cycleGuardService.assertCycleActif(tontineId);        // R2 déléguée à Benitha
    Membre membre = membreRepository.findById(requete.membreId())
            .orElseThrow(() -> new RessourceIntrouvableException("Membre introuvable"));

    if (!membre.getTontine().getId().equals(tontineId)) {                    // R1
        throw new RessourceIntrouvableException("Membre introuvable");
    }
    var utilisateurCourant = validateurCourantService.obtenir();
    if (membre.getUtilisateur() == null
            || !membre.getUtilisateur().getId().equals(utilisateurCourant.getId())) {
        throw new AccessDeniedException(
                "Un membre ne peut soumettre une demande que pour son propre compte");
    }

    pretService.verifierLimiteMontant(                                        // R6
            membre.getId(), cycleActif.getId(), requete.montantDemande());
    pretService.verifierEcheance(cycleActif, requete.dateEcheance());         // R7

    DemandePret demande = demandePretMapper.versEntite(requete);
    demande.setCycle(cycleActif);
    demande.setMembre(membre);
    demande.setDateDemande(LocalDate.now());
    demande.setStatut(StatutDemandePret.SOUMISE);
    return demandePretMapper.versReponse(demandePretRepository.save(demande));
}
```

> **À dire :** « 4 responsabilités, 4 endroits : R2 appartient à Benitha (je la
> délègue, je ne la réécris pas), R1 vient du contexte authentifié, R6 et R7 sont
> des méthodes de `PretService` — le domaine prêt. Le contrôle du status est en
> `SOUMISE`, puis la base reçoit PAR UN SEUL `save`. »

### R6 — le code réel

`api/src/main/java/bi/ac/upg/akiwacu/pret/PretService.java` :

```java
private static final BigDecimal MULTIPLICATEUR_R6 = BigDecimal.valueOf(3);

/** R6 — le capital demandé ne dépasse pas trois fois les cotisations du membre sur le cycle actif. */
@Transactional(readOnly = true)
public void verifierLimiteMontant(Long membreId, Long cycleId, BigDecimal montantDemande) {
    BigDecimal epargne = cotisationRepository.sommeParMembreEtCycle(membreId, cycleId);
    BigDecimal plafond = epargne.multiply(MULTIPLICATEUR_R6);

    if (montantDemande.compareTo(plafond) > 0) {
        throw new RegleMetierException(
            "R6 : le montant demandé (%s BIF) dépasse trois fois l'épargne du membre (%s BIF)"
                .formatted(montantDemande, epargne));
    }
}

/** R7 — l'échéance demandée reste dans les bornes du cycle. */
@Transactional(readOnly = true)
public void verifierEcheance(Cycle cycle, LocalDate dateEcheance) {
    if (dateEcheance.isAfter(cycle.getDateFin())) {
        throw new RegleMetierException(
            "R7 : l'échéance (%s) dépasse la fin du cycle (%s)".formatted(dateEcheance, cycle.getDateFin()));
    }
}
```

> **À dire :** « `BigDecimal` — jamais `double` : c'est de l'argent. `compareTo` évite
> les pièges de comparaison de décimaux. L'épargne vient de
> `sommeParMembreEtCycle`, une requête d'agrégation du repository. »

---

## 4. Démo 2 — Les votes (R4 : quorum de 2 commissaires DISTINCTS)

### Étape A — Vote POUR du commissaire 1
```
POST /api/demandes-pret/1/votes
Authorization: Bearer <jeton commissaire 1>
Content-Type: application/json

{ "sens": "POUR", "commentaire": "Épargne saine" }
```

### Étape B — Deuxième vote DU MÊME commissaire → refus R4
```
POST /api/demandes-pret/1/votes
Authorization: Bearer <jeton commissaire 1>
Content-Type: application/json

{ "sens": "POUR", "commentaire": "double clic" }
```

Réponse réelle :
```json
409 Conflict
{
  "status": 409,
  "error": "Conflict",
  "message": "R4 : un commissaire ne peut voter qu'une seule fois",
  "path": "/api/demandes-pret/1/votes"
}
```

### Étape C — Vote POUR du commissaire 2 → quorum atteint → APPROUVEE

### Le code réel du vote

`api/src/main/java/bi/ac/upg/akiwacu/vote/VoteService.java` :

```java
@Transactional
public VoteResponse voter(Long demandePretId, VoteRequest requete) {
    Long tontineId = TenantContext.getTontineId();
    DemandePret demande = demandePretRepository.findById(demandePretId)
            .orElseThrow(() -> new RessourceIntrouvableException("Demande de prêt introuvable"));
    verifierTontine(demande.getCycle().getTontine().getId(), tontineId);        // R1

    if (demande.getStatut() != StatutDemandePret.SOUMISE) {
        throw new RegleMetierException("La demande de prêt a déjà fait l'objet d'une décision finale");
    }

    Utilisateur commissaire = utilisateurCourant(tontineId);
    if (voteRepository.existsByDemandePretIdAndCommissaireId(demandePretId, commissaire.getId())) {
        throw new RegleMetierException("R4 : un commissaire ne peut voter qu'une seule fois");
    }

    var vote = VoteCommissaire.builder()
            .demandePret(demande)
            .commissaire(commissaire)
            .sens(requete.sens())
            .commentaire(requete.commentaire())
            .dateVote(Instant.now())
            .build();
    VoteCommissaire voteEnregistre = voteRepository.save(vote);
    mettreAJourStatut(demande);           // quorum évalué ici
    return voteMapper.versReponse(voteEnregistre);
}

private void mettreAJourStatut(DemandePret demande) {
    long pour = voteRepository.countByDemandePretIdAndSens(demande.getId(), SensVote.POUR);
    long contre = voteRepository.countByDemandePretIdAndSens(demande.getId(), SensVote.CONTRE);

    if (pour >= 2) {
        demande.setStatut(StatutDemandePret.APPROUVEE);
        demandePretRepository.save(demande);
    } else if (contre >= 2) {
        demande.setStatut(StatutDemandePret.REJETEE);
        demandePretRepository.save(demande);
    }
}
```

> **À dire :** « R4 a DEUX protections qui se complètent : le contrôle lisible ici
> (`existsByDemandePretIdAndCommissaireId`) et la contrainte unique déclarée dans
> `VoteCommissaire` — même si deux requêtes arrivent en parallèle, la base refuse le
> doublon. Le quorum est calculé par deux comptages POUR/CONTRE. »
>
> **À noter :** le commissaire est RÉSOLU depuis le contexte de sécurité
> (`utilisateurCourant`), jamais depuis le body → impossible de voter pour un autre.

---

## 5. Démo 3 — Débloquer le prêt (R3 + R5 + reçu)

### L'appel REST (réservé au trésorier — `hasRole('TRESORIER')`)
```
POST /api/prets
Authorization: Bearer <jeton trésorier>
Content-Type: application/json

{ "demandePretId": 1, "dateEcheance": "2026-12-31" }
```

### Le code réel

`api/src/main/java/bi/ac/upg/akiwacu/pret/PretService.java` :

```java
@Transactional
public PretResponse debloquerPret(PretDisbursementRequest requete) {
    Long tontineId = TenantContext.getTontineId();
    Cycle cycleActif = cycleGuardService.assertCycleActif(tontineId);          // R2
    DemandePret demande = demandePretRepository.findById(requete.demandePretId())
            .orElseThrow(() -> new RessourceIntrouvableException("Demande de prêt introuvable"));

    verifierTenant(demande, tontineId);                                        // R1
    if (demande.getStatut() != StatutDemandePret.APPROUVEE) {
        throw new RegleMetierException("Le prêt ne peut être débloqué que pour une demande approuvée");
    }
    if (!demande.getCycle().getId().equals(cycleActif.getId())) {
        throw new RegleMetierException("R3 : la demande n'appartient pas au cycle actif");
    }
    verifierEcheance(cycleActif, requete.dateEcheance());                      // R7
    if (pretRepository.findByDemandePretId(demande.getId()).isPresent()) {
        throw new RegleMetierException("Un prêt existe déjà pour cette demande");
    }

    Utilisateur validateur = validateurCourantService.obtenir();               // R5
    verifierTenant(validateur.getTontine().getId(), tontineId);

    Pret pret = Pret.builder()
            .demandePret(demande)
            .membre(demande.getMembre())
            .cycle(cycleActif)
            .montantAccorde(demande.getMontantDemande())
            .dureeMois(demande.getDureeMois())
            .dateDeblocage(LocalDate.now())
            .dateEcheance(requete.dateEcheance())
            .statut(StatutPret.ACTIF)
            .validePar(validateur)
            .build();
    Pret saved = pretRepository.save(pret);
    demande.setStatut(StatutDemandePret.DEBLOQUEE);
    demandePretRepository.save(demande);
    recuService.genererPourPret(saved.getId());                                // reçu PDF + verrou R8
    return toResponse(saved);
}
```

> **À dire :** « Débloquer = re-vérifier TOUT au moment où l'argent sort : la demande
> doit être APPROUVEE, le cycle toujours actif (R3 — il a pu être gelé entre-temps),
> l'échéance dans les bornes (R7), et un seul prêt par demande. Le trésorier
> validateur est enregistré (R5), puis le reçu est généré (R8 verrouille l'opération). »

---

## 6. Démo 4 — L'échéancier

```
GET /api/prets/1/echeancier
Authorization: Bearer <jeton>
```

### Le code réel

```java
@Transactional(readOnly = true)
public PretScheduleResponse echeancier(Long pretId) {
    Long tontineId = TenantContext.getTontineId();
    Pret pret = pretRepository.findById(pretId)
            .orElseThrow(() -> new RessourceIntrouvableException("Prêt introuvable"));
    verifierTenant(pret.getMembre().getTontine().getId(), tontineId);
    return new PretScheduleResponse(pret.getId(), pret.getDureeMois(), pret.montantDu(),
            pret.soldeRestant(), pret.getDateDeblocage(), pret.getDateEcheance());
}
```

**À dire :** « `montantDu()` et `soldeRestant()` sont des MÉTHODES DE L'ENTITÉ
`Pret.java` — du comportement objet, testable sans base de données, exigé par
l'énoncé §11.1 (tests d'entités). »

---

## 7. Les endpoints REST de votre domaine (récapitulatif)

| Méthode | URL | Rôle / réponse |
|---|---|---|
| `POST` | `/api/demandes-pret` | créer une demande (R2, R1, R6, R7) → 201, rôle MEMBRE |
| `GET` | `/api/demandes-pret?statut=SOUMISE` | lister/filtrer par statut (PR #53, merci de confirmer sur `develop`) |
| `GET` | `/api/demandes-pret/{id}` | détail d'une demande |
| `GET` | `/api/demandes-pret/{id}/votes/decision` | résumé de décision / quorum (PR #53, merci de confirmer sur `develop`) |
| `POST` | `/api/demandes-pret/{id}/votes` | voter POUR/CONTRE (R4), rôle COMMISSAIRE |
| `GET` | `/api/demandes-pret/{id}/votes` | lister les votes |
| `POST` | `/api/prets` | débloquer une demande approuvée (R3, R5), rôle TRESORIER |
| `GET` | `/api/prets` | lister les prêts |
| `GET` | `/api/prets/{id}` | détail d'un prêt |
| `GET` | `/api/prets/{id}/echeancier` | échéancier |

> Chaque `@Operation` Swagger documente les codes 200/201/400/403/404/**409**.
> Par exemple le vote déclare explicitement le `409` de R4.

---

## 8. L'inventaire de VOS fichiers

| Fichier | Contenu réel résumé |
|---|---|
| `demandepret/DemandePret.java` | `@Entity` : montantDemande, dureeMois, dateEcheance, dateDemande, statut, membre, cycle. |
| `demandepret/StatutDemandePret.java` | enum `SOUMISE`, `APPROUVEE`, `REJETEE`, `DEBLOQUEE`. |
| `demandepret/DemandePretController.java` | endpoints REST « demandes-pret » avec filtre par statut et résumé de décision. |
| `demandepret/DemandePretService.java` | créer/lister/trouver avec R2 (déléguée), R1, R6, R7. |
| `demandepret/DemandePretRepository.java` | `findByMembreTontineId`, recherche par statut. |
| `vote/VoteCommissaire.java` | `@Entity` : sens, commentaire, dateVote, commissaire, demandePret + contrainte unique `(demande, commissaire)`. |
| `vote/VoteService.java` | voter (R4), quorum, lister, tenant. |
| `vote/VoteCommissaireRepository.java` | `existsByDemandePretIdAndCommissaireId`, `countByDemandePretIdAndSens`. |
| `vote/SensVote.java` | enum `POUR` / `CONTRE`. |
| `pret/Pret.java` | `@Entity` : montantAccorde, dureeMois, dates, statut, membre, cycle, demande ; `montantDu()`, `soldeRestant()`. |
| `pret/StatutPret.java` | enum `ACTIF` etc. |
| `pret/PretService.java` | verifierLimiteMontant (R6), verifierEcheance (R7), debloquerPret (R3/R5/reçu), lister/trouver/echeancier. |
| `pret/PretController.java`, `pret/dto/*` | REST + records (`PretResponse`, `PretScheduleResponse`, `PretDisbursementRequest`). |

---

## 9. Correspondance ÉNONCÉ → votre démonstration

| L'énoncé demande | Votre preuve |
|---|---|
| « R4 : prêt approuvé par ≥ 2 commissaires distincts » | `existsByDemandePretIdAndCommissaireId` + `countByDemandePretIdAndSens` + contrainte unique. Test `shouldRejectApprovalFromSameCommissionerTwice()`. |
| « R6 : montant ≤ 3 × épargne » | `verifierLimiteMontant` + `sommeParMembreEtCycle`. Test `shouldRejectLoanExceedingThreeTimesSavings()`. |
| « R7 : échéance ≤ fin de cycle » | `verifierEcheance`. Test `shouldRejectDueDateAfterCycleEnd()`. |
| « R2 : cycle actif » (déléguée) | `cycleGuardService.assertCycleActif` appelé dans `demanderPret` et `debloquerPret`. |
| « R3 : pas de prêt si cycle gelé/clôturé » | revérification du cycle au déblocage + `R3` dans `CycleService`. |
| « R1 : isolation » | `verifierTontine` partout ; une ressource d'un autre tenant se lit comme introuvable (404). |
| « R5 : trésorier validateur » | `validateurCourantService.obtenir()` posé sur le prêt. |
| « R8 : reçu verrou » | `genererPourPret` → opération verrouillée (visible via la boîte « reçu » du reçu). |
| « Workflow de prêt complet » | démo : SOUMISE → APPROUVEE (2 votes distincts) → DEBLOQUEE + prêt ACTIF + échéancier. |

---

## 10. Les pièges et questions probables

- **« Comment garanti-on deux commissaires DISTINCTS ? »** → Le service compte les
  identifiants `existsByDemandePretIdAndCommissaireId` (un vote/commissaire) et la
  contrainte unique en base est le filet. Deux votes du même commissaire → 409 R4.
- **« Quelle épargne est utilisée pour R6 ? »** → `sommeParMembreEtCycle` : la somme
  des cotisations du MÊME membre sur le cycle ACTIF. **Réponse honnête exigée :** si
  le calcul observé diffère de ce que le jury suppose, montrez la méthode dans
  `CotisationRepository` et dites « c'est la définition implémentée ; on peut
  documenter la règle avec l'équipe ».
- **« Pourquoi re-vérifier le cycle au déblocage ? »** → Une demande peut être
  approuvée, puis le cycle gelé : l'argent ne doit pas sortir d'un cycle non actif (R3).
- **« Pourquoi les règles sont dans les services ? »** → Pour être testables SANS
  serveur web et pour les réutiliser partout (R6/R7 sont appelés à la demande ET au
  déblocage). Un contrôleur qui décide tout seul est indéfendable à l'oral.
- **« Que se passe-t-il après une décision finale ? »** → `vote` refuse : `statut !=
  SOUMISE` → « la demande a déjà fait l'objet d'une décision finale ». Pas de
  règle de transition non convenue (cf. notes de PR #53).

---

> **Prérequis démo** : API + PostgreSQL démarrés, un cycle OUVERT, un membre avec
> cotisation enregistrée (épargne > 0 pour passer R6), AU MOINS deux comptes avec le
> rôle COMMISSAIRE dans la même tontine, un compte TRESORIER pour le déblocage.
>
> **Alertes honnêtes** : le checkout local est une branche de documentation ancienne ;
> des endpoints supplémentaires peuvent n'exister que sur `develop`/GitHub. Confirmer
> sur `develop` ce qu'on présente comme intégré ; ne promettre aucun endpoint visible
> uniquement dans un PR non merge.