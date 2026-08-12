# MODÈLE DE DONNÉES — les 13 entités

**Ce document fait autorité.** Il est l'entrée de la PR socle et la source du MCD du
rapport. Un agent Claude Code qui écrit une entité lit ce fichier — il n'invente rien.

Conventions : tables et colonnes en `snake_case`, classes et champs en `camelCase`.
Montants en `NUMERIC(15,2)`, devise **BIF**. Dates métier en `DATE`, horodatages
d'audit en `TIMESTAMPTZ`.

---

## Vue d'ensemble

```
Tontine ─┬─< Membre ─────< Adhesion >───── Cycle
         ├─< Utilisateur                     │
         ├─< TransactionCaisse               ├─< Cotisation ──> Recu
         └─< Cycle                           └─< DemandePret ─┬─< VoteCommissaire
                                                              └── Pret ─┬─< Remboursement ──> Recu
                                                                        └──> Recu
```

`<` se lit « a plusieurs ». Toute entité métier porte `tontine_id` directement ou par
son parent : c'est ce qui rend **R1** applicable.

---

## 0. `BaseEntity` — classe mère, pas une table

`@MappedSuperclass`. Déjà écrite dans `common/BaseEntity.java`.

| Champ | Type | Note |
|---|---|---|
| `id` | `Long` | `IDENTITY` |
| `createdAt` / `createdBy` | `Instant` / `String` | non modifiables |
| `updatedAt` / `updatedBy` | `Instant` / `String` | |
| `version` | `Long` | `@Version` — verrou optimiste, évite la double saisie concurrente |

---

## 1. `Tontine` — `tontines`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `nom` | `VARCHAR(150)` | `NOT NULL`, `UNIQUE` |
| `description` | `TEXT` | |
| `dateCreation` | `DATE` | `NOT NULL` |
| `statut` | `VARCHAR(20)` | enum `StatutTontine` : `ACTIVE` `SUSPENDUE` `CLOTUREE` |

## 2. `Utilisateur` — `utilisateurs`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `tontine` | FK `tontines` | `NOT NULL` — **support de R1** |
| `email` | `VARCHAR(150)` | `NOT NULL`, `UNIQUE` |
| `motDePasse` | `VARCHAR(100)` | `NOT NULL`, BCrypt, **jamais exposé en DTO** |
| `nom` / `prenom` | `VARCHAR(100)` | `NOT NULL` |
| `telephone` | `VARCHAR(20)` | |
| `actif` | `BOOLEAN` | `NOT NULL DEFAULT true` |
| `roles` | table `utilisateur_roles` | `@ElementCollection`, enum `Role` : `ADMIN` `GESTIONNAIRE` `TRESORIER` `COMMISSAIRE` `MEMBRE` |

> Un utilisateur appartient à **une seule** tontine. `tontineId` entre dans les claims
> du JWT et n'est jamais lu depuis le corps d'une requête.

## 3. `Membre` — `membres`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `tontine` | FK `tontines` | `NOT NULL` |
| `utilisateur` | FK `utilisateurs` | **nullable** — un membre peut n'avoir aucun compte |
| `numeroMembre` | `VARCHAR(30)` | `UNIQUE (tontine_id, numero_membre)` |
| `nom` / `prenom` | `VARCHAR(100)` | `NOT NULL` |
| `telephone` | `VARCHAR(20)` | `NOT NULL` |
| `dateAdhesion` | `DATE` | `NOT NULL` |
| `statut` | `VARCHAR(20)` | enum `StatutMembre` : `ACTIF` `SUSPENDU` `SORTI` |

## 4. `Cycle` — `cycles`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `tontine` | FK `tontines` | `NOT NULL` |
| `libelle` | `VARCHAR(100)` | `NOT NULL` |
| `dateDebut` / `dateFin` | `DATE` | `NOT NULL`, `CHECK (date_fin > date_debut)` |
| `montantCotisation` | `NUMERIC(15,2)` | `NOT NULL`, `CHECK (> 0)` |
| `periodicite` | `VARCHAR(20)` | enum : `HEBDOMADAIRE` `MENSUELLE` |
| `statut` | `VARCHAR(20)` | enum `StatutCycle` : `OUVERT` `GELE` `CLOTURE` |
| `dateCloture` | `DATE` | nullable |

**Comportement à implémenter et à tester :**

```java
boolean peutAccueillirOperation()   // vrai si statut == OUVERT       → R2
boolean autoriseNouveauPret()       // vrai si statut == OUVERT       → R3
```

## 5. `Adhesion` — `adhesions`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `membre` | FK `membres` | `NOT NULL` |
| `cycle` | FK `cycles` | `NOT NULL` |
| `dateAdhesion` | `DATE` | `NOT NULL` |
| `statut` | `VARCHAR(20)` | enum : `ACTIVE` `CLOTUREE` |

> **`UNIQUE (membre_id, cycle_id)`** — un membre n'adhère qu'une fois par cycle.

## 6. `Cotisation` — `cotisations`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `cycle` | FK `cycles` | `NOT NULL` |
| `membre` | FK `membres` | `NOT NULL` |
| `montant` | `NUMERIC(15,2)` | `NOT NULL`, `CHECK (> 0)` |
| `dateCotisation` | `DATE` | `NOT NULL` |
| `modePaiement` | `VARCHAR(20)` | enum : `ESPECES` `MOBILE_MONEY` `VIREMENT` |
| `validePar` | FK `utilisateurs` | `NOT NULL` — **R5** |
| `verrouille` | `BOOLEAN` | `NOT NULL DEFAULT false` — **R8** |
| `recu` | FK `recus` | nullable jusqu'à émission |

```java
boolean estVerrouillee()   // vrai dès qu'un reçu est émis           → R8
```

## 7. `DemandePret` — `demandes_pret`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `cycle` | FK `cycles` | `NOT NULL` |
| `membre` | FK `membres` | `NOT NULL` |
| `montantDemande` | `NUMERIC(15,2)` | `NOT NULL`, `CHECK (> 0)` |
| `dureeMois` | `INTEGER` | `NOT NULL`, `CHECK (> 0)` |
| `motif` | `TEXT` | `NOT NULL` |
| `dateDemande` | `DATE` | `NOT NULL` |
| `statut` | `VARCHAR(20)` | enum : `SOUMISE` `APPROUVEE` `REJETEE` `DEBLOQUEE` |

```java
boolean quorumAtteint()   // ≥ 2 votes POUR de commissaires distincts → R4
```

## 8. `VoteCommissaire` — `votes_commissaire`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `demandePret` | FK `demandes_pret` | `NOT NULL` |
| `commissaire` | FK `utilisateurs` | `NOT NULL` |
| `sens` | `VARCHAR(10)` | enum : `POUR` `CONTRE` |
| `commentaire` | `TEXT` | |
| `dateVote` | `TIMESTAMPTZ` | `NOT NULL` |

> **`UNIQUE (demande_pret_id, commissaire_id)` — c'est ce qui fait réellement R4.**
> Un décompte écrit en Java se contourne par deux requêtes simultanées ; la contrainte
> en base, non. Le contrôle applicatif ne sert qu'à renvoyer un 409 lisible.

## 9. `Pret` — `prets`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `demandePret` | FK `demandes_pret` | `NOT NULL`, `UNIQUE` — relation 1–1 |
| `membre` | FK `membres` | `NOT NULL` |
| `cycle` | FK `cycles` | `NOT NULL` |
| `montantAccorde` | `NUMERIC(15,2)` | `NOT NULL`, `CHECK (> 0)` |
| `tauxInteret` | `NUMERIC(5,2)` | `NOT NULL DEFAULT 0` |
| `dateDeblocage` | `DATE` | `NOT NULL` |
| `dateEcheance` | `DATE` | `NOT NULL` — **R7** : ≤ `cycle.dateFin` |
| `statut` | `VARCHAR(20)` | enum : `ACTIF` `SOLDE` `EN_RETARD` |
| `validePar` | FK `utilisateurs` | `NOT NULL` — **R5** |
| `verrouille` | `BOOLEAN` | `NOT NULL DEFAULT false` — **R8** |
| `recu` | FK `recus` | nullable |

```java
BigDecimal montantDu()                  // voir la formule ci-dessous
BigDecimal soldeRestant()               // montantDu() − somme des remboursements
boolean    estEnRetard(LocalDate jour)  // jour > dateEcheance && soldeRestant() > 0
```

**Formule des intérêts — arrêtée, ne pasla changer sans passer par le daily :**

```
montantDu = montantAccorde + (montantAccorde × tauxInteret / 100)
```

Un **taux forfaitaire appliqué une seule fois** sur la durée du prêt, arrondi
`HALF_UP` à 2 décimales. Pas de capitalisation, pas de prorata mensuel.

C'est ce que pratiquent la plupart des tontines, c'est calculable de tête par un
trésorier, et c'est défendable en une phrase à la soutenance. `tauxInteret` vaut `0`
par défaut : la démonstration fonctionne sans jamais toucher au sujet.

> ⚠ **R6 porte sur `montantDemande`, pas sur `montantDu`.** Le plafond de 3 × épargne
> s'applique au **capital demandé**. Sinon un prêt conforme deviendrait non conforme
> par le seul effet des intérêts, ce qui n'a pas de sens métier.

## 10. `Remboursement` — `remboursements`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `pret` | FK `prets` | `NOT NULL` |
| `montant` | `NUMERIC(15,2)` | `NOT NULL`, `CHECK (> 0)` |
| `dateRemboursement` | `DATE` | `NOT NULL` |
| `validePar` | FK `utilisateurs` | `NOT NULL` — **R5** |
| `verrouille` | `BOOLEAN` | `NOT NULL DEFAULT false` — **R8** |
| `recu` | FK `recus` | nullable |

## 11. `TransactionCaisse` — `transactions_caisse`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `tontine` | FK `tontines` | `NOT NULL` |
| `cycle` | FK `cycles` | nullable — certains mouvements sont hors cycle |
| `sens` | `VARCHAR(10)` | enum : `ENTREE` `SORTIE` |
| `montant` | `NUMERIC(15,2)` | `NOT NULL`, `CHECK (> 0)` |
| `motif` | `VARCHAR(255)` | `NOT NULL` |
| `dateTransaction` | `DATE` | `NOT NULL` |
| `validePar` | FK `utilisateurs` | `NOT NULL` — **R5** |
| `referenceOperation` | `VARCHAR(50)` | trace l'opération d'origine, ex. `COTISATION:42` |

## 12. `Recu` — `recus`

| Champ | Type SQL | Contraintes |
|---|---|---|
| `numero` | `VARCHAR(30)` | `NOT NULL`, `UNIQUE` — séquence PostgreSQL |
| `typeOperation` | `VARCHAR(20)` | enum : `COTISATION` `DEBLOCAGE_PRET` `REMBOURSEMENT` |
| `operationId` | `BIGINT` | `NOT NULL` |
| `membre` | FK `membres` | `NOT NULL` |
| `montant` | `NUMERIC(15,2)` | `NOT NULL` |
| `dateEmission` | `TIMESTAMPTZ` | `NOT NULL` |
| `emisPar` | FK `utilisateurs` | `NOT NULL` |
| `cheminFichier` | `VARCHAR(255)` | chemin du PDF généré |

> **`UNIQUE (type_operation, operation_id)`** — une opération ne peut avoir qu'un reçu.
> C'est cette contrainte qui rend **R8** vérifiable : un reçu existe ⇒ l'opération est
> gelée.

**Séquence et format du numéro — arrêtés :**

```sql
CREATE SEQUENCE seq_numero_recu START 1 INCREMENT 1;
```

Format : **`REC-<année>-<6 chiffres>`**, par exemple `REC-2026-000042`.
Le `RecuService` de Juste consomme cette séquence exacte — le nom fait partie du
contrat, il ne se renomme pas.

> Le numéro est généré **en base**, pas en Java. Deux trésoriers qui émettent un reçu
> à la même seconde obtiendraient sinon le même numéro, et l'énoncé exige une
> numérotation séquentielle vérifiable.

Les **7 champs obligatoires du PDF** (énoncé §9) : numéro, date, nom du membre, type
d'opération, montant en chiffres et en lettres, nom de la tontine, validateur.

---

## Ordre des migrations Flyway

L'ordre est imposé par les clés étrangères. `V2__schema_initial.sql` crée tout d'un
bloc, dans cette séquence :

```
tontines → utilisateurs → utilisateur_roles → membres → cycles → adhesions
→ recus → cotisations → demandes_pret → votes_commissaire → prets
→ remboursements → transactions_caisse
```

`recus` vient avant `cotisations` parce que celle-ci le référence.

## Index à créer dès la V2

Sans eux, chaque écran de liste fait un balayage complet de table.

```sql
CREATE INDEX idx_membre_tontine        ON membres(tontine_id);
CREATE INDEX idx_cycle_tontine_statut  ON cycles(tontine_id, statut);
CREATE INDEX idx_cotisation_cycle      ON cotisations(cycle_id);
CREATE INDEX idx_cotisation_membre     ON cotisations(membre_id);
CREATE INDEX idx_pret_membre_statut    ON prets(membre_id, statut);
CREATE INDEX idx_remboursement_pret    ON remboursements(pret_id);
CREATE INDEX idx_caisse_tontine_cycle  ON transactions_caisse(tontine_id, cycle_id);
```

## Ce qui n'est PAS dans le socle

Les repositories sont créés **vides** (`extends JpaRepository<X, Long>`). Les méthodes
de requête appartiennent au propriétaire du domaine, pas à la PR socle — sinon quatre
personnes se marchent dessus sur les mêmes fichiers dès le D2.
