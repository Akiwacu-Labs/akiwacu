# MATRICE DES RÈGLES MÉTIER R1 → R8

Chaque règle a **un propriétaire nommé**, **un test au nom imposé** et **une ligne dans
le rapport**. Aucune règle orpheline.

Les noms de tests ne sont pas négociables : ce sont eux qui prouvent la conformité à
l'énoncé §8, et c'est ce qu'on ouvre devant le jury.

---

| Règle | Énoncé | Propriétaire | Où c'est implémenté | Test obligatoire |
|---|---|---|---|---|
| **R1** | Isolation totale des données par tontine | **Andy** | Filtre Hibernate `@FilterDef` + `tontineId` injecté depuis le JWT, **jamais** depuis le corps de la requête | `shouldNotAccessDataFromAnotherTontine()` |
| **R2** | Toute opération financière appartient à un cycle actif | **Benitha** | `CycleGuardService.assertCycleActif()`, appelé par cotisation, prêt, remboursement et caisse | `shouldRejectOperationOnInactiveCycle()` |
| **R3** | Aucun prêt si le cycle est gelé ou clôturé | **Benitha** (garde) + **Gloria** (appel) | Machine à états du cycle — vérification **au déblocage**, pas seulement à la demande | `shouldRejectLoanWhenCycleFrozen()` |
| **R4** | Prêt approuvé par au moins 2 commissaires **distincts** | **Gloria** | Contrainte unique `(demande_id, commissaire_id)` en base + décompte dans le service | `shouldRejectApprovalFromSameCommissionerTwice()` |
| **R5** | Chaque opération enregistre le trésorier validateur | **Klein** (+ **Andy** pour l'audit) | Champ `validePar` non nullable, alimenté depuis le `SecurityContext` | `shouldRecordValidatingTreasurer()` |
| **R6** | Montant du prêt ≤ 3 × épargne du membre | **Gloria** | `PretService` — épargne = somme des cotisations du membre sur le cycle | `shouldRejectLoanExceedingThreeTimesSavings()` |
| **R7** | Échéance du prêt ≤ fin du cycle | **Gloria** | Validation dans `PretService` à la création | `shouldRejectDueDateAfterCycleEnd()` |
| **R8** | Opération validée avec reçu → non modifiable ni supprimable | **Andy** (+ **Juste** pour les reçus) | Flag `verrouille` + garde dans le service, réponse HTTP **409 Conflict** | `shouldRejectModificationOfReceiptedOperation()` |

> **Toutes les règles sont implémentées dans la couche service, jamais dans le
> contrôleur.** C'est un point de revue de code non négociable et c'est ce que
> l'enseignant vérifiera en premier.

---

## Les dépendances croisées à annoncer au daily

Trois règles ne sont pas portées par une seule personne. Elles se coordonnent à voix
haute, pas par message.

| Interface partagée | Publiée par | Consommée par | Échéance |
|---|---|---|---|
| `CycleGuardService.assertCycleActif()` | Benitha | Gloria, Klein, Juste | **D3** — stub d'abord, implémentation au D4 |
| `RecuService` | Juste | Klein, Benitha, Gloria | **D3** interface, **D4** implémentation |
| `TenantContext` | Andy | tout le monde | **D2** |

Un stub publié tôt vaut mieux qu'une implémentation parfaite publiée tard : les
dépendants doivent pouvoir **compiler**.

---

## Les pièges connus, règle par règle

**R1 — le piège du `tontineId` dans le corps de la requête.** Si l'identifiant de
tontine arrive du client, le contrôle d'accès est délégué au client. N'importe qui
peut le modifier. Il vient des claims du JWT, point.

**R3 — le piège du moment de la vérification.** Une demande de prêt peut être déposée
alors que le cycle est ouvert, puis débloquée après le gel. La vérification doit
avoir lieu **au déblocage**, pas seulement à la création de la demande.

**R4 — le piège du contrôle uniquement applicatif.** Un décompte fait en Java se
contourne par deux requêtes simultanées. La contrainte unique
`(demande_id, commissaire_id)` en base est ce qui garantit réellement la règle ; le
contrôle applicatif ne sert qu'à renvoyer un message propre.

**R6 — le piège de la définition.** « L'épargne du membre » n'est pas défini par
l'énoncé. Nous retenons : *la somme des cotisations du membre sur le cycle en cours*.
**Écrire cette définition dans le rapport** — c'est une question de soutenance
garantie, et une définition assumée vaut mieux qu'une définition implicite.

**R8 — le piège du périmètre.** Le verrouillage porte sur toute opération ayant généré
un reçu : cotisation, déblocage de prêt, remboursement. Pas seulement les cotisations.

---

## Ce qu'on montre à la soutenance

Pour chaque règle, dans cet ordre — trois minutes suffisent :

1. le test, ouvert dans l'IDE, avec son nom exact
2. le service où la règle vit, et pourquoi elle n'est pas dans le contrôleur
3. la tentative de violation en direct sur Swagger, et le code HTTP renvoyé
