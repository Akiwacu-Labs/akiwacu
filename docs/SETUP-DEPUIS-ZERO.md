# SETUP DEPUIS ZÉRO — l'ordre corrigé

Ce fichier est la séquence complète du montage du projet, dans l'ordre où elle aurait
dû être exécutée. Il ne remplace pas les runbooks détaillés : il dit **quoi faire,
dans quel ordre, et ce qui casse si l'ordre change**.

Sers-t'en pour repartir de zéro, ou pour vérifier qu'aucune étape n'a été sautée.

**Durée totale : environ 4 h**, dont 2 h en parallèle entre Andy et Klein.

---

## Le graphe des dépendances

Tout le reste découle de ce schéma. Les blocs A, B et D démarrent en même temps.

```
   ÉTAPE 0 — Reconnaissance
        │
        ├──────────────┬──────────────┐
        ▼              ▼              ▼
   A · GitHub      B · Proxmox    D · Jira
   (Andy 45min)    (Klein 2h)     (Andy 40min)
        │              │              │
        └──────┬───────┘              │
               ▼                      │
        C · Jonction                  │
        runner + 1re CI verte         │
               │                      │
               ▼                      │
        A6 · Protections de branche   │
               │                      │
               └──────────┬───────────┘
                          ▼
                  F · Packs + kickoff
                          │
                          ▼
                  PR socle (13 entités)
```

**Les deux inversions qui coûtent une demi-journée si on les rate :**

1. **Les protections de branche s'activent après la première CI verte** (C2 → A6).
   Une protection exige une vérification nommée. Tant que le workflow n'a jamais
   tourné, ce nom n'existe pas côté GitHub : la première PR reste bloquée sur une
   vérification qui n'arrivera jamais.

2. **Les clés SSH se génèrent avant de cloner les VM** (B4 → B5). La clé publique
   part dans cloud-init au moment du clonage. Générée après, il faut réouvrir
   chaque VM par la console Proxmox pour la poser à la main.

---

# ÉTAPE 0 — Reconnaissance · 20 min

> 🖥️ **SUR : l'hôte Proxmox** puis **ta machine**

**À faire avant de dimensionner quoi que ce soit.** Le premier plan de ce projet
prévoyait 4 VM, 20 Go de RAM et 200 Go de disque. Le serveur n'avait rien de tout ça
de libre.

```bash
# sur l'hôte Proxmox
pveversion                    # PVE 8 et PVE 9 n'ont pas les mêmes dépôts APT
qm list                       # qui d'autre occupe le serveur ?
pvesm status                  # espace réellement libre
free -h                       # RAM totale
```

Note trois chiffres : **disque libre**, **RAM totale**, **VM des autres groupes**.

> Sur un serveur partagé, la ressource rare est le **disque**, pas la RAM. Un fichier
> qcow2 occupe sa place même quand la VM est éteinte. Dimensionne sur le disque.

Côté GitHub, relève les **handles réels** des cinq membres. Un handle inventé dans
`CODEOWNERS` échoue en silence : le fichier est accepté, les propriétaires ne sont
jamais sollicités.

✅ **Terminé quand :** tu connais la version de PVE, l'espace libre, et les 5 handles.

---

# BLOC A — GitHub · Andy · 45 min

Détail complet : [`RUNBOOK-GITHUB-CLI.md`](../pack-M1-Andy/RUNBOOK-GITHUB-CLI.md)

### A1 · Organisation et dépôt public

Organisation gratuite, puis dépôt **public** (voir `DECISIONS.md` D-04 — la protection
de branche n'est gratuite que sur un dépôt public). Branche par défaut : `develop`.

### A2 · Cloner en local

```bash
mkdir -p ~/projects && cd ~/projects
gh repo clone <org>/Akiwacu && cd Akiwacu
```

Si `git push` échoue avec `Could not resolve hostname github.com`, c'est le DNS de
WSL. Si l'échec porte sur une clé refusée, bascule la remote en HTTPS :
`git remote set-url origin https://github.com/<org>/Akiwacu.git`

### A3 · Copier le socle — **avant** d'inviter l'équipe

> ⚠ Étape réservée à Andy. Les chemins `/mnt/c/Users/Andy/…` n'existent que sur son
> PC. Tous les autres récupèrent avec `git pull`.

Copie le scaffolding **et** la documentation, y compris les fiches de rôle. L'ordre
compte : si tu invites avant de pousser, le premier clone de chacun ne contient pas
son fichier de rôle, et son agent Claude Code démarre sans contexte.

### A4 · Inviter

Les 4 développeurs en `push`. L'enseignant en **Triage** — et surtout **pas** dans
`CODEOWNERS` : un utilisateur Triage y est silencieusement ignoré.

> Réflexe de session : `export` ne survit pas à la fermeture du terminal. Si `$REPO`
> est vide, tous les `gh api` renvoient 404 sur une URL du type `/repos//…`.
> ```bash
> REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner); ORG=${REPO%%/*}
> ```

### A5 · Secrets

`gh secret set` pour les mots de passe PostgreSQL, la clé privée SSH de déploiement,
le secret JWT, le jeton SonarQube. **Aucun secret dans le dépôt** — il est public.

### A6 · Protections de branche — ⏸ **à faire après C2**

Ne les active pas maintenant. Reviens ici quand la première CI est verte.

✅ **Bloc A terminé quand :** le dépôt existe, `develop` contient le socle, les
6 personnes sont invitées, les secrets sont posés.

---

# BLOC B — Proxmox · Klein · 2 h

Détail complet : [`GUIDE-INFRA-PAS-A-PAS.md`](../pack-M2-Klein/GUIDE-INFRA-PAS-A-PAS.md)
· reprise en cours de route : [`REPRENDRE-ICI.md`](../pack-M2-Klein/REPRENDRE-ICI.md)

> 🖥️ Les commandes `qm`, `pct`, `pvesm` tournent **sur l'hôte Proxmox** — jamais
> ailleurs. `qm: command not found` signifie systématiquement que tu es sur ta machine.

### B1 · Dépôts APT

PVE 9 utilise le format **deb822** (`/etc/apt/sources.list.d/*.sources`), pas
l'ancien `.list`. Ne redirige jamais la sortie vers `/dev/null` : c'est ce qui masque
l'échec et fait perdre vingt minutes plus loin.

### B2 · Image cloud et template

Télécharger l'image cloud Debian, créer une VM, y attacher le disque, configurer
cloud-init, convertir en template.

> Le template n'est ni démarré, ni supprimé, jamais. Les trois VM en dépendent.

### B3 · Clés SSH — ⚠ **avant** le clonage

```bash
ssh-keygen -t ed25519 -f ~/.ssh/gha_deploy -C "gha-deploy-akiwacu" -N ""
```

La clé **publique** entre dans cloud-init avant le clonage. La clé **privée** devient
le secret GitHub `SSH_PRIVATE_KEY` (Andy, étape A5).

C'est l'étape la plus souvent sautée du projet — et celle qui coûte le plus cher à
rattraper.

### B4 · Cloner les 3 VM — en **clone lié**

| Nom | ID | vCPU | RAM | Disque | IP |
|---|---|---|---|---|---|
| `vm-dev-g1` | 301 | 2 | 2560 Mo | 32 Go | `192.168.0.50` |
| `vm-prod-g1` | 302 | 2 | 2560 Mo | 32 Go | `192.168.0.51` |
| `vm-devops-g1` | 303 | 4 | 6144 Mo | 32 Go | `192.168.0.52` |

Clone **lié**, pas complet : le clone lié ne recopie pas le disque de base.

> Toute modification de cloud-init exige `qm stop` puis `qm start`. Un `reboot` ne
> réapplique pas la configuration.

### B5 · Démarrer et vérifier

```bash
# depuis ta machine
for IP in 50 51 52; do ssh -i ~/.ssh/gha_deploy deployer@192.168.0.$IP hostname; done
```

Les trois doivent répondre avant de continuer.

### B6 · Ansible : Docker, utilisateurs, pare-feu

L'inventaire décrit **trois** machines, regroupées `app` (.50, .51) et `devops` (.52).
Si tu vois `vm-ci` ou `vm-mon` quelque part, c'est un résidu de l'ancien plan à 4 VM —
corrige-le.

✅ **Bloc B terminé quand :** les 3 VM répondent en SSH et `docker --version` sort une
version sur chacune.

---

# BLOC C — Jonction · Klein + Andy · 30 min

**Ce bloc exige A et B terminés.**

### C1 · Runner auto-hébergé sur `vm-devops-g1`

Le jeton d'enregistrement **expire en 1 heure**. Klein le demande, Andy le génère au
moment où Klein est prêt à taper la commande — pas la veille.

Le runner doit apparaître **Idle** dans `Settings → Actions → Runners`.

> C'est la dépendance la plus critique du projet. Un runner GitHub hébergé ne peut
> pas joindre `192.168.0.50` derrière le NAT de l'université. Aucun réglage ne
> contourne cela — voir `DECISIONS.md` D-13.

### C2 · Première CI verte sur `develop`

Ouvrir une PR triviale et la faire passer. Vérifier trois points propres au monorepo :

- les jobs Maven portent `working-directory: api`
- les contextes de build Docker sont `./api` et `./client`
- le chemin JaCoCo est `api/target/site/jacoco/`

### C3 · ⏪ Revenir à A6 — activer les protections

Maintenant que le workflow a tourné au moins une fois, ses vérifications sont
sélectionnables :

- `develop` : 1 revue approuvée · vérifications de statut requises · pas de push forcé
- `main` : idem, plus l'environnement protégé avec approbation manuelle

✅ **Bloc C terminé quand :** le runner est Idle, la CI est verte, les deux branches
sont protégées, et une PR de test se merge normalement.

---

# BLOC D — Jira et Confluence · Andy · 40 min

Détail complet : [`GUIDE-JIRA-SCRUM.md`](GUIDE-JIRA-SCRUM.md) partie C

### D1 · Site et projet

Projet **Scrum**, template Software development, clé `AKW`. Inviter les 6 personnes
(Jira Free autorise 10 utilisateurs).

### D2 · Colonnes et limite WIP

Backlog · To Do · **In Progress (max 2)** · In Review (max 3) · Done.

### D3 · Importer le CSV

`02-BACKLOG-JIRA.csv` — 93 stories. Colonnes mappées : `Issue Type · Summary ·
Description · Assignee · Story Points · Labels · Priority`.

> **Aucune colonne `Sprint`, aucune colonne d'epic.** Dans un projet team-managed, le
> lien parent-enfant ne s'importe pas et une valeur de sprint crée un sprint fantôme —
> sans nom, sans dates, en double. Le regroupement passe par les labels.

Si un import bancal est déjà en place, nettoie d'abord : supprimer les sprints
fantômes, supprimer les tickets importés, vider les epics résiduels.

### D4 · Créer et dater les 3 sprints à la main

Les dates ne s'importent jamais par CSV. Sans dates, pas de burndown.

### D5 · Remplir le Sprint 1 et le **démarrer**

Glisser tout ce qui porte les labels `D1` `D2` `D3`. Un sprint non démarré n'alimente
ni le tableau ni le burndown.

### D6 · Connecter GitHub for Jira

Chaque ticket affiche alors ses branches, commits, PR et déploiements. C'est la
traçabilité que le correcteur voit en un clic.

### D7 · Confluence

Activer sur le même site. Espace `Akiwacu`, arborescence du rapport en 10 chapitres.

✅ **Bloc D terminé quand :** 93 stories au backlog, 3 sprints datés, Sprint 1 démarré,
GitHub connecté.

---

# BLOC F — Packs et kickoff · 45 min

1. Envoyer à chacun son pack (`pack-M<n>-<prénom>.zip`) + le lien du dépôt.
2. Chacun exécute `INSTALLER-MON-PACK.md` : dépôt et pack côte à côte, Obsidian,
   `~/.claude/CLAUDE.md` personnel.
3. **Sprint planning, 30 min.** Objectif du Sprint 1 en une phrase :
   *« un push sur develop se déploie tout seul sur vm-dev-g1 ».*
4. Andy enchaîne sur la **PR socle** : 13 entités JPA + migrations Flyway.

✅ **Setup terminé quand :** chacun a fait tourner `./mvnw verify` en local et poussé
un commit.

---

# Les huit pièges, en un coup d'œil

| # | Piège | Symptôme | Parade |
|---|---|---|---|
| 1 | Protections activées avant la 1re CI | La 1re PR attend une vérification inexistante | Protéger après C2 |
| 2 | Clés SSH générées après le clonage | Pas d'accès SSH aux VM | B3 avant B4 |
| 3 | `pom.xml` cherché à la racine | `mvn` ne trouve rien | `working-directory: api` |
| 4 | Filtres `paths:` sur les workflows | PR immergeable en permanence | Ne pas filtrer |
| 5 | `CODEOWNERS` à un seul propriétaire | L'auteur ne peut approuver sa propre PR | Deux handles par chemin |
| 6 | Enseignant dans `CODEOWNERS` | Ignoré en silence | Triage n'y figure pas |
| 7 | Colonne `Sprint` dans le CSV Jira | Sprints fantômes, en double, sans dates | Importer au backlog seul |
| 8 | Dimensionner avant d'inspecter le serveur | Plan de 4 VM sur un disque saturé | Étape 0 en premier |

Deux pièges de session qui reviennent tout le temps :

- **`export` ne survit pas au terminal.** `$REPO` vide → tous les `gh api` en 404.
- **Un `reboot` ne réapplique pas cloud-init.** Il faut `qm stop` puis `qm start`.
