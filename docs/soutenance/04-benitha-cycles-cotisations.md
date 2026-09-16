# Soutenance — Benitha

## Message à défendre

Je possède la machine à états des cycles et les cotisations. Toute opération financière
commence par la vérification d'un cycle ouvert : c'est R2. Un cycle peut être gelé ou
clôturé, et une opération interdite renvoie 409 au lieu de modifier silencieusement la
base.

## Ce que je montre

1. `GET /api/cycles` puis le statut `OUVERT`.
2. Création d'une cotisation avec `POST /api/cotisations`.
3. Enregistrement batch `POST /api/cotisations/batch`.
4. Passage du cycle à `GELE`, nouvelle cotisation : HTTP 409.
5. `CycleGuardService.assertCycleActif(...)` et les tests de cycle/cotisation.
6. Le total de cotisations et le reçu PDF obtenu après succès.

## Déroulé de mes 30 minutes

| Temps | Contenu |
|---|---|
| 0–3 min | Cas d'usage du trésorier et objectif de saisie rapide |
| 3–8 min | Cycle métier : états `OUVERT`, `GELE`, `CLOTURE` et transitions |
| 8–13 min | JEE/Spring : entité Cycle, service, garde partagée et repository JPA |
| 13–18 min | REST : endpoints cycle/cotisation, DTO, validation et codes HTTP |
| 18–24 min | Démonstration cotisation normale, batch puis refus HTTP 409 après gel |
| 24–27 min | Tests R2, tenant isolation et génération du reçu |
| 27–30 min | Questions et relais vers Gloria/Klein pour prêts et caisse |

## Explication métier/API en une minute

« Le cycle est une machine à états. Les endpoints REST ne changent pas directement la
base : ils appellent le service, qui vérifie l'état du cycle, le tenant et les droits.
La même garde est réutilisée par les autres opérations financières, ce qui évite des
implémentations contradictoires de R2. »

## Explication métier

La garde est partagée parce que prêt, remboursement, cotisation et caisse ont tous
besoin de la même définition d'un cycle actif. La règle est dans le service, donc elle
s'applique aussi si un appel ne vient pas de l'interface web.

## Correspondance Énoncé → ma présentation

| L'énoncé demande | Où je le montre (tuto : `04-benitha-tuto-30min.md`) |
|---|---|
| R2 : opération dans un cycle actif | §3 `CycleGuardService.assertCycleActif` (garde partagée) + test `shouldRejectOperationOnInactiveCycle()`. Démo gel → 409. |
| R3 : pas de prêt si cycle gelé/clôturé | §2 `changerStatut` + port `CycleActiveLoanPort` sur clôture. |
| R5 : trésorier validateur | §4 `validateurCourantService.obtenir()` sur chaque cotisation. |
| R8 : opération reçue = verrouillée | §6 `estVerrouillee()` + `RegleMetierException` sur `modifier`. |
| Saisie rapide (cas métier) | §5 `POST /api/cotisations/batch` (transaction atomique, reçu par ligne). |
| Tests + REST + DTO | `CycleTest`, `CycleGuardServiceTest`, `CotisationServiceTest`, `CotisationControllerTest` ; `@PreAuthorize` + codes 201/409/403. |
| Individuel : « explique ton code » | §1 vocabulaire (machine à états, batch, garde), §8 inventaire, §10 pièges. |

## Preuves dans le dépôt

- `api/src/main/java/bi/ac/upg/akiwacu/cycle/`
- `api/src/main/java/bi/ac/upg/akiwacu/cotisation/`
- `CycleGuardServiceTest`
- `CycleTest`, `CotisationServiceTest`, `CotisationControllerTest`
- Commits Benitha visibles dans develop : cycle CRUD, garde R2, cotisations et batch.

## Questions probables

**Pourquoi `OUVERT → GELE → CLOTURE` ?** Geler suspend les opérations sans perdre
l'historique ; clôturer termine définitivement la période. Les transitions invalides
sont refusées par la règle métier.

**Un montant peut-il venir du client ?** Il est validé par le serveur et associé au
cycle/tenant authentifié ; le client ne choisit pas un autre périmètre.

**Pourquoi le batch ?** Il répond au besoin métier du trésorier qui saisit rapidement
plusieurs membres, tout en réutilisant les validations de chaque opération.

## Limite à dire clairement

La version client présente dans le checkout est récente et comporte des écrans, mais
beaucoup de fichiers sont encore non commités. Je montrerai l'API comme preuve
principale et le client comme consommateur si son build et son parcours sont confirmés.
