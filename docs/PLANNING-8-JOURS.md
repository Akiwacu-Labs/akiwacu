# PLANNING — 8 jours

**D1 jeudi 6 août → D8 jeudi 13 août 2026. Soutenance vendredi 14 août.**
Week-end travaillé. Aucun jour de rattrapage.

---

## Équipe

| Code | Nom | GitHub | Domaine |
|---|---|---|---|
| **M1** | Andy Miguel Habyarimana | `@miguelandy875` | Chef de projet · Architecte · Sécurité — `auth` `utilisateur` `membre` |
| **M2** | Klein de Guy Mugisha | `@Gkcoding-prog` | DevOps · QA — `caisse` `remboursement` `dashboard` |
| **M3** | Juste Daxa Ayikunde | `@daxa257` | `tontine` `adhesion` `recu` |
| **M4** | Benitha Gahimbare | `@gahibenitha` | `cycle` `cotisation` — règles **R2 R3** |
| **M5** | Gloria Muhimpundu | `@muhimpundugloria` | `demandepret` `vote` `pret` — règles **R4 R6 R7** |

Enseignant : `@cincotech` — accès **Triage** (lecture, revue, commentaires ; aucun push).

---

## Périmètre

13 entités · ~75 endpoints · 8 règles métier · 3 types de reçus PDF · client web complet ·
chaîne CI/CD à deux environnements · stack de monitoring · rapport de 15 à 25 pages.

## Les quatre jalons qui conditionnent le reste

| Jalon | Échéance |
|---|---|
| Socle mergé : 13 entités JPA + Flyway | **D1** |
| CI verte sur `develop` | **D2** |
| Déploiement automatique sur vm-dev-g1 | **D3** |
| Gel des fonctionnalités | **D7, 12 h** |

## Règle de déblocage

Bloqué **plus de 30 minutes** sur un même point : écrire dans le groupe.

---

## Sprints

| Sprint | Jours | Objectif | Porte de sortie |
|---|---|---|---|
| **Sprint 1** | D1–D3 (6 → 8 août) | Socle, CRUD, pipeline | Push `develop` → déploiement vm-dev-g1 automatique |
| **Sprint 2** | D4–D6 (9 → 11 août) | Règles métier, client, qualité | Couverture ≥ 80 %, Quality Gate vert, vm-prod-g1 en ligne |
| **Sprint 3** | D7–D8 (12 → 13 août) | Gel, rapport, répétition | 13 livrables déposés, démos répétées |

---

## Rituels

| Rituel | Quand | Durée | Format |
|---|---|---|---|
| **Sprint planning** | D1, D4, D7 au matin | 30 min | Objectif du sprint, prise des tickets, estimation. Maximum 2 min de débat par ticket. |
| **Daily stand-up** | Tous les jours, heure fixe | 15 min | Trois questions : livré la veille · livré ce jour · point de blocage. Aucun débat technique en séance. |
| **Affinage du backlog** | D2 et D5 | 15 min | Découpage des tickets au-delà de 8 points, écriture des critères d'acceptation manquants. |
| **Revue de sprint** | D3 et D6 au soir | 30 min | Démonstration en direct sur vm-dev-g1, 5 min par personne. |
| **Rétrospective** | Après chaque revue | 15 min | Stop / Continue / Start. Une seule action retenue, avec un responsable. |
| **Répétition soutenance** | D8 après-midi | 90 min | 5 min par personne, puis questions. |

### Vélocité

La vélocité est estimée et mesurée à chaque sprint. Elle n'est pas utilisée pour planifier
le sprint suivant : trois sprints courts ne suffisent pas à la stabiliser.

L'indicateur de pilotage retenu est le **cycle time** — durée entre « In Progress » et
« Done » :

| Cycle time | Lecture |
|---|---|
| < 24 h | normal |
| 24–48 h | ticket sous-découpé |
| > 48 h | blocage à traiter |

---

# D1 — jeudi 6 août · Fondations

| Qui | Livrables |
|---|---|
| **Andy** | Organisation GitHub, dépôt public, protections de branche, invitations · sites Jira et Confluence · sprint planning · **PR socle : 13 entités JPA + Flyway** |
| **Klein** | 3 VM Proxmox créées · Docker sur les trois · **runner GitHub auto-hébergé enregistré** · squelette Ansible |
| **Juste** | Poste configuré · dépôt cloné · application locale opérationnelle · énoncé lu · brouillon du MCD |
| **Benitha** | Poste configuré · dépôt cloné · application locale opérationnelle · machine à états du cycle rédigée |
| **Gloria** | Poste configuré · dépôt cloné · application locale opérationnelle · specs Gherkin lues |

**Porte de sortie :** `./mvnw verify` vert en local pour chacun · 1 commit poussé par personne ·
les 3 VM répondent en SSH · le runner apparaît en « Idle ».

---

# D2 — vendredi 7 août · CI et sécurité

| Qui | Livrables |
|---|---|
| **Andy** | Spring Security + JWT + rôles · `GlobalExceptionHandler` · configuration OpenAPI · `TenantContext` (R1) |
| **Klein** | `Dockerfile` multi-stage · `docker-compose` · **workflow CI build + test vert** · playbooks Ansible de base |
| **Juste** | Package `tontine` · `GET /api/tontines` opérationnel |
| **Benitha** | Package `cycle` · `GET /api/cycles` opérationnel |
| **Gloria** | Package `demandepret` · `GET /api/demandes-pret` opérationnel |

**Porte de sortie :** CI verte sur `develop` · Swagger UI accessible · au moins 2 PR mergées.

Si la CI n'est pas verte à la fin de D2, D3 s'ouvre par une mobilisation collective sur le pipeline.

---

# D3 — samedi 8 août · CRUD et déploiement

| Qui | Livrables |
|---|---|
| **Andy** | CRUD utilisateurs et membres avec tests · initialisation du client React · **génération du client API depuis OpenAPI** |
| **Klein** | SonarQube sur vm-devops-g1 · JaCoCo seuil 50 % · Trivy · publication GHCR · **déploiement automatique vm-dev-g1** |
| **Juste** | CRUD tontine et adhésion avec tests · **interface `RecuService` publiée** |
| **Benitha** | CRUD cycle et cotisation avec tests · **`CycleGuardService` publié et annoncé** |
| **Gloria** | CRUD demande de prêt, vote et prêt avec tests · contrainte unique `(demande_id, commissaire_id)` |

**Porte de sortie — revue de sprint + rétrospective :** push sur `develop` → déploiement
automatique sur vm-dev-g1 · tous les endpoints répondent · Swagger accessible depuis vm-dev-g1.

---

# D4 — dimanche 9 août · Règles métier

Sprint planning au matin.

| Qui | Livrables |
|---|---|
| **Andy** | **R1** isolation multi-tenant · **R8** verrouillage après reçu · audit **R5** · revues de PR prioritaires |
| **Klein** | Remboursements et caisse avec **R5** · seuil JaCoCo 60 % · endpoints dashboard |
| **Juste** | **Service de reçus PDF livré** — dépendance de Klein, Benitha et Gloria |
| **Benitha** | **R2** cycle actif · **R3** blocage des prêts · machine à états complète |
| **Gloria** | **R4** commissaires distincts · **R6** plafond d'épargne · **R7** échéance de cycle |

**Porte de sortie :** les 8 règles métier disposent de leur test nommé au vert · le service
de reçus est appelable par les trois domaines concernés.

---

# D5 — lundi 10 août · Client

| Qui | Livrables |
|---|---|
| **Andy** | Écrans login, guards par rôle, utilisateurs, membres |
| **Klein** | Prometheus · Grafana · métriques métier Micrometer · écrans dashboard et caisse |
| **Juste** | Écrans tontine, adhésion, téléchargement de reçu |
| **Benitha** | **Écran de saisie rapide des cotisations** · écrans cycles |
| **Gloria** | Écrans demande de prêt, vote commissaire, détail du prêt, échéancier |

**Porte de sortie :** le parcours de démonstration complet fonctionne de bout en bout sur
vm-dev-g1 — connexion → création de tontine → ouverture de cycle → saisie de cotisations →
demande de prêt → deux votes → approbation → déblocage → reçu PDF → tableau de bord.

---

# D6 — mardi 11 août · Qualité et production

| Qui | Livrables |
|---|---|
| **Andy** | Collection Bruno assemblée · durcissement sécurité · revue de couverture globale |
| **Klein** | **Seuil JaCoCo 80 %** · Quality Gate vert · **merge `main` → déploiement vm-prod-g1** · dashboards Grafana |
| **Juste** | Couverture ≥ 80 % sur ses packages |
| **Benitha** | Couverture ≥ 80 % · écrans restants |
| **Gloria** | Couverture ≥ 80 % · tests de contrôleur · écrans restants |

**Porte de sortie — revue de sprint + rétrospective :** couverture ≥ 80 % · Quality Gate vert ·
vm-prod-g1 en ligne · toutes les fonctionnalités de l'API accessibles depuis le client.

---

# D7 — mercredi 12 août · Gel et rapport

**Gel des fonctionnalités à 12 h.** Après cette heure : corrections de bugs uniquement.
Sprint planning au matin pour la répartition du rapport.

| Qui | Livrables |
|---|---|
| **Andy** | Assemblage du rapport sur Confluence · contexte, besoins, architecture, les 2 ADR · export du `schema.sql` consolidé · export Postman de la collection Bruno |
| **Klein** | **Terraform** codifiant les VM existantes · captures Sonar, Trivy, Grafana avec interprétation · chapitre CI/CD |
| **Juste** | Conception de la base : MCD, MLD, diagramme de classes |
| **Benitha** | **Jeu de données de démonstration** · captures des écrans client |
| **Gloria** | Chapitre règles métier · captures Swagger |

**Porte de sortie :** rapport à 80 % · captures prises · aucun test en échec.

---

# D8 — jeudi 13 août · Répétition et livraison

| Créneau | Contenu |
|---|---|
| Matin | Finalisation du rapport · export PDF · vérification des 13 livrables |
| 14 h | **Répétition générale, 90 min** — 5 min par personne, puis questions |
| 17 h | **Snapshot Proxmox de vm-prod-g1** · dernier push · dépôt des livrables |

---

# Vendredi 14 août — Soutenance

Chacun présente 5 minutes : 2 endpoints en direct sur **vm-prod-g1**, 1 règle métier avec son
test, 1 écran client, 1 PR relue, son historique de commits dans l'onglet Insights.

---

## Definition of Done

Une story est terminée lorsque les neuf points sont satisfaits.

1. Branche `feat/<domaine>-<slug>` créée depuis `develop`
2. Entité · repository · service · contrôleur · DTO · mapper
3. Règles métier dans la couche service
4. Tests : cas nominal, cas d'erreur, exception
5. Couverture ≥ 80 % sur le package concerné
6. Endpoint annoté OpenAPI et visible dans Swagger
7. Écran client consommant l'endpoint
8. Requête ajoutée à la collection Bruno
9. CI verte, 1 revue approuvée, merge dans `develop`, endpoint vérifié sur vm-dev-g1

## Cadence quotidienne

- 2 commits minimum, messages conformes aux Conventional Commits
- 1 PR ouverte ou mergée
- Les revues en attente depuis plus de 3 h passent avant tout autre travail
- Blocage de plus de 30 minutes : signalement dans le groupe

## Suppléance

Andy reprend Juste et Gloria · Klein reprend Benitha · Andy reprend Klein sur l'infrastructure.
Chaque package dispose d'un `README.md` permettant une reprise en 30 minutes.
