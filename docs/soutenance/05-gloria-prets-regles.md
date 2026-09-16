# Soutenance — Gloria

## Message à défendre

Je possède `demandepret`, `vote` et `pret`. Mon workflow couvre la demande, les votes
des commissaires, la décision, le prêt, l'échéancier et les contrôles R4, R6 et R7.
Le métier est exécuté dans les services et testé séparément des contrôleurs.

## Démonstration idéale

1. Créer une demande de prêt sur un cycle ouvert.
2. Vote POUR du commissaire 1.
3. Deuxième vote du même commissaire : refus R4.
4. Vote POUR du commissaire 2 : quorum atteint, demande approuvée.
5. Afficher le prêt et l'échéancier.
6. Soumettre un montant supérieur à trois fois l'épargne : refus R6.
7. Soumettre une échéance après la fin du cycle : refus R7.

Chaque refus doit être un HTTP 409 avec un message métier compréhensible.

## Déroulé de mes 30 minutes

| Temps | Contenu |
|---|---|
| 0–3 min | Problème métier : demander, décider et suivre un prêt |
| 3–8 min | Modèle JEE : demande, vote, prêt, service transactionnel et repositories JPA |
| 8–13 min | REST : endpoints de demande, votes, décision, prêt et échéancier |
| 13–22 min | Démonstration complète : deux commissaires distincts, quorum et prêt |
| 22–26 min | Démonstration des refus R4, R6 et R7 en HTTP 409 |
| 26–28 min | Tests, isolation tenant et sécurité des rôles |
| 28–30 min | Questions et relais vers le reçu PDF/DevOps |

## Explication REST/JEE en une minute

« Le contrôleur REST expose le workflow, mais ne décide pas lui-même. Le service
transactionnel charge la demande, vérifie le cycle, le tenant et les votes distincts,
puis change l'état. JPA persiste cette décision et les DTO empêchent d'exposer
directement les entités. »

## Correspondance Énoncé → ma présentation

| L'énoncé demande | Où je le montre (tuto : `05-gloria-tuto-30min.md`) |
|---|---|
| R4 : ≥ 2 commissaires distincts | §4 `existsByDemandePretIdAndCommissaireId` + `countByDemandePretIdAndSens` + contrainte unique. Test `shouldRejectApprovalFromSameCommissionerTwice()`. |
| R6 : montant ≤ 3 × épargne | §3 `verifierLimiteMontant` + `sommeParMembreEtCycle`. Test `shouldRejectLoanExceedingThreeTimesSavings()`. |
| R7 : échéance ≤ fin de cycle | §3 `verifierEcheance`. Test `shouldRejectDueDateAfterCycleEnd()`. |
| R2 / R3 déléguées | §3 `cycleGuardService.assertCycleActif` + revérification au déblocage (R3). |
| R1 : isolation | §3/4 `verifierTontine` partout (404 sur ressource d'un autre tenant). |
| R5 : trésorier validateur | §5 `validateurCourantService.obtenir()` posé sur le prêt. |
| Workflow complet + REST | §7 tableau des endpoints : demande, votes, décision, prêt, échéancier, filtre par statut. |
| Tests + entités | `Pret.montantDu()`/`soldeRestant()` (tests d'entités), `VoteServiceTest`, `PretServiceTest`. |
| Individuel : « explique ton code » | §1 vocabulaire (quorum, déblocage, échéancier), §8 inventaire, §10 pièges. |

## Preuves dans le dépôt

- `api/src/main/java/bi/ac/upg/akiwacu/demandepret/`
- `api/src/main/java/bi/ac/upg/akiwacu/vote/`
- `api/src/main/java/bi/ac/upg/akiwacu/pret/`
- `VoteServiceTest.shouldRejectApprovalFromSameCommissionerTwice`
- `PretServiceTest.shouldRejectLoanExceedingThreeTimesSavings`
- `PretServiceTest.shouldRejectDueDateAfterCycleEnd`
- Tests de tenant et de cycle liés aux demandes/prêts.

## Réponses techniques

**Comment garantir deux commissaires distincts ?** Le service compte les identifiants
distincts, et la base doit empêcher le doublon `(demande, commissaire)`.

**Quelle épargne pour R6 ?** Je donne la définition réellement implémentée dans le
service et je la montre dans le code/README. Si elle n'est pas explicitement écrite,
je ne l'improvise pas devant le jury : je présente le calcul observé et propose de
documenter cette décision.

**Pourquoi vérifier le cycle au déblocage ?** Un cycle peut être gelé après la demande.
La décision finale doit donc revérifier l'état au moment où l'argent sort.

## Prudence sur l'état Git

Le checkout local pointe sur une branche de documentation ancienne alors que le
handoff décrit des évolutions plus récentes. Les endpoints et règles supplémentaires
doivent être confirmés sur `develop`/GitHub avant de les présenter comme intégrés.
Le workflow de prêt reste démontrable avec le code local, mais je ne promets pas un
endpoint apparu uniquement dans un PR non visible ici.
