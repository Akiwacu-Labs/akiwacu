# PLAN — reprise après le calendrier D1–D8

Ce fichier remplace `docs/PLANNING-8-JOURS.md`, archivé dans
`_ARCHIVE/01-PLANNING-8-JOURS.md`. Son calendrier (D1–D8, 6→13 août, soutenance le
14) a plus de dix jours de retard sur l'avancement réel du projet — le travail a
continué bien après le D8 sans qu'aucun document ne le dise. Les rituels et la
**Definition of Done** qu'il décrivait restent valables ; seul le calendrier change.

**⚠️ Une date à confirmer.** Ce plan suppose au moins deux semaines de délai à
partir d'aujourd'hui (25 août 2026), sans date de soutenance ferme au-delà — la
date du 14 août encore présente dans `CLAUDE.md` et le calendrier ci-dessous est
dépassée. Andy : confirme la vraie date avant de figer la Phase D (rapport,
répétition, gel des fonctionnalités).

---

## Priorité 0 — aujourd'hui / demain, tout le monde

1. **Merger #48** dès que sa CI passe (`gh pr checks 48`) — c'est le déblocage de
   tout le reste : le workflow tourne sur `push` **et** `pull_request` vers
   `develop`, donc aucune autre PR ne passe tant que #48 reste ouverte.
2. **Merger #49** (docs, déjà mergeable, aucune dépendance).
3. **Lire son issue** — #43 Andy · #44 Juste · #45 Benitha · #46 Gloria · #47
   Klein. Chacune a reçu aujourd'hui une nouvelle section « Qualité SonarQube —
   tri du code neuf ».
4. **Trier ses propres problèmes Sonar, sous 72 h (avant le 28 août — voir
   `docs/DECISIONS.md` D-37).** Filtrer New Code sur ses propres fichiers dans
   l'UI Sonar, corriger ce qui est réel (Blocker/Critical), résoudre le reste
   avec une raison écrite dans le commentaire Sonar — jamais en silence.
   `SONAR_GATE` ne repasse à `true` que quand c'est fait : l'énoncé (§11.2)
   exige que le pipeline s'arrête sur tout échec du Quality Gate, sans
   exception — ce n'est pas négociable, juste temporairement désarmé.

---

## Priorité 1 — cette semaine

**Andy**
- Distribuer SonarLint en mode connecté à l'équipe — clôt AKW-260, c'est le
  filet que D-11 prévoyait et qui n'a jamais été fait. Dépend du même accès
  Tailscale/LAN au VM Sonar (`192.168.0.52:9000`) déjà en cours de résolution.
- Vérifier SSH sur **vm-dev-g1 ET vm-prod-g1** avant de flipper
  `DEPLOY_ENABLED` — cette variable déclenche le déploiement sur les deux VM
  (`cicd-develop.yml` et `cicd-main.yml` la lisent toutes les deux), pas
  seulement dev. Ne pas flipper avant d'avoir vérifié les deux.
- Revérifier la couverture réelle (`./mvnw verify`) puis monter
  `jacoco.line.coverage` à `0.60` dans `api/pom.xml`. Le ratchet de D-10
  (50 → 60 → 80) est resté bloqué à 50 % depuis son armement le 24 août,
  alors que l'énoncé (§11.1) exige 80 % au final. Ne pas sauter directement à
  80 % — ça bloquerait toute PR aujourd'hui pour un chiffre non atteint.
- Décisions encore ouvertes sur #43 : verrou `TransactionCaisse`, extension du
  `@Filter` R1.

**Juste (#44) · Benitha (#45) · Gloria (#46) · Klein (#47)**
Avancer sur les items déjà listés dans sa propre issue — endpoints Phase C,
écran client, tri Sonar. Rien de nouveau à ajouter ici : la liste dans chaque
issue est à jour au 25 août, ce plan n'y ajoute que la synchronisation.

---

## Priorité 2 — une fois #43–47 vidés (fin de Phase C)

- `SONAR_GATE` repassé à `true`, vérifié vert.
- **Merger `develop` → `main`** pour prouver le déploiement vm-prod-g1 pour de
  vrai — jamais exercé une seule fois jusqu'ici (`deploy-vm-prod` toujours
  `skipped` sur tous les runs passés). C'est une exigence séparée de l'énoncé
  (§11.3), distincte de vm-dev-g1 : les deux pipelines sont vérifiés
  indépendamment à la soutenance.
- Décider si `jacoco.line.coverage` monte à `0.70` puis `0.80`, selon la
  couverture réelle mesurée à ce moment-là — pas avant, et pas en devinant.
- `bruno/` et `livrables/` : sortir de `.gitkeep` au fil de l'eau plutôt qu'au
  dernier moment — front-loader les gains faciles (export Grafana JSON, export
  OpenAPI) pendant que Phase C tourne encore.

---

## Phase D — durcissement et rapport

Reprend telle quelle la répartition du rapport déjà écrite dans l'ancien D7
(`_ARCHIVE/01-PLANNING-8-JOURS.md`) — la répartition reste bonne, seul le
calendrier autour d'elle était faux.

| Qui | Chapitre / livrable |
|---|---|
| **Andy** | Assemblage du rapport — contexte, besoins, architecture, les 2 ADR · export du `schema.sql` consolidé · export Postman de la collection Bruno |
| **Klein** | Terraform codifiant les VM existantes · captures Sonar, Trivy, Grafana avec interprétation · chapitre CI/CD |
| **Juste** | Conception de la base : MCD, MLD, diagramme de classes |
| **Benitha** | Jeu de données de démonstration · captures des écrans client |
| **Gloria** | Chapitre règles métier · captures Swagger |

**Checklist finale :** `docs/LIVRABLES.md`, 14/14, coché sur preuve — pas sur
promesse. Répétition générale une fois, tous les cinq. Snapshot Proxmox de
vm-prod-g1 juste avant dépôt des livrables.

---

## Ce qui ne change pas

Rituels (stand-up, revue de sprint, rétrospective), **Definition of Done** à
9 points, règles Git (branches `feat/<domaine>-<slug>`, pas de squash, PR
< 400 lignes) — tout ça reste défini dans `docs/PREMIERE-PR.md`, `CLAUDE.md` et
`docs/DECISIONS.md`. Ce plan ne remplace que le calendrier.
