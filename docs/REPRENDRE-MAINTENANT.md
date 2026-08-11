# REPRENDRE MAINTENANT — Andy et Klein

État arrêté le **lundi 10 août**. Ce document remplace toute autre liste de tâches
jusqu'à ce que la première CI soit verte.

---

## Où on en est réellement

| | Fait | Pas fait |
|---|---|---|
| **GitHub** | Org, dépôt public, socle poussé (7 août), invitations, secrets, protections de branche actives | Le dépôt est **en retard d'une semaine** sur les corrections. Un commit `fix(ci)` attend sur le PC d'Andy, non poussé. |
| **Proxmox** | PVE 9.1.1 inspecté, dépôts APT corrigés, template cloud-init, **3 VM en clone lié**, Docker, ufw, sysctl, **runner enregistré et Idle** | Accès SSH aux 3 VM : **à vérifier**. Ansible : arrêté sur l'ancien inventaire. |
| **Jira** | Site et projet `AKW` créés | Backlog vide — rien importé. C'est une bonne nouvelle : chemin propre. |
| **Équipe** | — | Juste, Benitha et Gloria n'ont **rien reçu**. |
| **Code** | — | Aucune ligne de Spring Boot. Pas de `pom.xml`. |

## Le nœud, en une phrase

**Vous êtes bloqués l'un par l'autre, mais à deux endroits différents** — donc aucun
des deux n'a de raison d'attendre l'autre pour commencer.

- La CI ne peut pas devenir verte sans le `pom.xml` d'**Andy**.
- Le déploiement ne peut pas fonctionner sans les clés SSH de **Klein**.

Travaillez en parallèle. Vous ne vous rejoignez qu'aux trois rendez-vous du bas de
cette page.

## Ce qui a changé dans la CI, et pourquoi

Le workflow échouait à l'étape SonarQube — un serveur qui n'existe pas encore. Avec
une branche protégée exigeant une vérification verte, **aucune PR n'aurait pu merger,
y compris la PR socle**. Deux étapes sont désormais conditionnées à une variable de
dépôt :

| Variable | Effet quand elle vaut `true` | Qui l'active | Quand |
|---|---|---|---|
| `COVERAGE_GATE` | Le seuil JaCoCo devient bloquant | Klein | après le merge du socle |
| `SONAR_ENABLED` | L'analyse Sonar et le Quality Gate s'exécutent | Klein | quand SonarQube tourne sur `vm-devops-g1` |

Tant qu'elles sont absentes, ces étapes sont sautées et le reste du pipeline tourne
normalement. Ce n'est pas un contournement de la notation : les deux seront actives
avant la revue de sprint du D3.

---

# ANDY — 4 blocs, environ 3 h

## A1 · Resynchroniser le socle · 20 min

> 🖥️ **SUR : ta machine (WSL)** — `~/projects/Akiwacu`

Ton dépôt date du 7 août. Depuis, ont changé : les deux workflows, `CODEOWNERS`,
l'emplacement du `Dockerfile`, le `client/`, et huit documents de `docs/`.

```bash
cd ~/projects/Akiwacu
git status                    # doit être propre, 1 commit d'avance

K=/mnt/c/Users/Andy/Documents/JEE\&DevOps

# 1 — le scaffolding corrigé écrase l'ancien
cp -r $K/project-kit/repo-scaffolding/. .

# 2 — le Dockerfile a déménagé dans api/ : l'ancien à la racine doit partir
git rm -f --ignore-unmatch Dockerfile

# 3 — la documentation : 12 fichiers au lieu de 4
cp $K/team-packs/_COMMUN/*.md docs/
rm -f docs/INSTALLER-MON-PACK.md

# 4 — les fiches de rôle (elles ont changé aussi)
mkdir -p docs/roles
for M in 1 2 3 4 5; do
  cp $K/team-packs/pack-M$M-*/LIS-MOI-DABORD.md docs/roles/M$M-role.md
done

# 5 — vérifier avant de commiter
ls docs/          # 12 fichiers + roles/
ls docs/roles/    # 5 fichiers
ls api/ client/   # Dockerfile · Dockerfile + nginx.conf
git status
```

```bash
git add -A
git commit -m "docs(repo): documentation complète, ADR, matrice des règles, carte des machines"
git push origin develop
```

> Le push direct sur `develop` fonctionne parce que la protection a été posée avec
> `enforce_admins: false` — tu es administrateur, tu passes outre. C'est légitime
> pour le commit d'amorçage. **Personne d'autre ne pousse en direct.**
>
> Si le push est refusé malgré tout :
> ```bash
> REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
> gh api -X DELETE /repos/$REPO/branches/develop/protection
> git push origin develop
> ```
> et tu remets la protection au rendez-vous n° 3.

**Préviens Klein dès que c'est poussé.** Il doit faire `git pull` avant de reprendre
Ansible, sinon il travaille sur l'inventaire qui l'a bloqué vendredi.

## A2 · Jira · 25 min

Le backlog est vide, donc pas de nettoyage à faire.

1. `Paramètres du projet` → `Importer` → `project-kit/02-BACKLOG-JIRA.csv`
2. Mapper : `Issue Type` `Summary` `Description` `Assignee` `Story Points` `Labels` `Priority`
   — **il n'y a ni colonne Sprint ni epic, c'est voulu**
3. « Map values » : associer `Andy` `Klein` `Juste` `Benitha` `Gloria` aux comptes
4. Vérifier : **93 stories au backlog**
5. `Backlog` → `Créer un sprint` × 3, puis dater chacun :

| Sprint | Début | Fin |
|---|---|---|
| `Sprint 1 — Socle et pipeline` | jeu. 6 | sam. 8 août |
| `Sprint 2 — Règles métier et client` | dim. 9 | mar. 11 août |
| `Sprint 3 — Gel et rapport` | mer. 12 | jeu. 13 août |

6. Glisser dans Sprint 1 tout ce qui porte les labels `D1` `D2` `D3`, puis
   **`Démarrer le sprint`**
7. Colonnes du tableau : Backlog · To Do · **In Progress (WIP 2)** · In Review · Done

Détail si tu bloques : `docs/GUIDE-JIRA-SCRUM.md` partie C.

## A3 · Envoyer les packs · 10 min

Les trois autres perdent une journée entière par jour d'attente. C'est le geste au
meilleur rapport temps/effet de la journée.

À chacun : son zip `team-packs/pack-M<n>-<prénom>.zip` + le lien du dépôt + une ligne :

> Ton pack est en pièce jointe. Ouvre `INSTALLER-MON-PACK.md` en premier, il fait
> tout : dépôt et pack côte à côte, Obsidian, Claude Code. Compte 10 minutes.
> Ensuite `LIS-MOI-DABORD.md`. Daily à \<heure\>.

Ajoute la ligne de clonage exacte :
`gh repo clone <ton-org>/Akiwacu Akiwacu`

## A4 · La PR socle · le reste de la journée

C'est le seul travail qui débloque quatre personnes. Tout le reste peut attendre.

Une PR, sur `feat/socle-entites` :

- `api/pom.xml` — Spring Boot 3, Java 21, JPA, Security, Actuator, Flyway,
  springdoc, JaCoCo, wrapper `mvnw` commité
- les **13 entités** : `BaseEntity` `Utilisateur` `Membre` `Tontine` `Cycle`
  `Adhesion` `Cotisation` `DemandePret` `VoteCommissaire` `Pret` `Remboursement`
  `TransactionCaisse` `Recu`
- les migrations Flyway correspondantes
- les repositories vides
- `application.yml` + `application-dev.yml`

> **Le seuil JaCoCo doit être à 0 dans ce `pom.xml`.** Le socle n'a presque pas de
> tests : un seuil à 50 % ferait échouer la PR qui apporte le code. Klein le relève
> au rendez-vous n° 3.

Klein approuve (il est co-propriétaire de tous les chemins), la CI passe, tu merges.

---

# KLEIN — 3 blocs, environ 2 h

## K1 · Les clés SSH — le point d'incertitude · 15 min

C'est l'étape 1.4 qui avait été sautée. Sans elle, aucun déploiement automatique
n'est possible, et c'est la moitié de la note DevOps.

> 🖥️ **SUR : ta machine**

```bash
for IP in 50 51 52; do
  echo -n "192.168.0.$IP : "
  ssh -o BatchMode=yes -o ConnectTimeout=4 -i ~/.ssh/gha_deploy \
      deployer@192.168.0.$IP hostname 2>/dev/null || echo "ÉCHEC"
done
```

**Les trois répondent ?** Passe à K2.

**Sinon**, génère la paire et pose la clé publique sur chaque VM :

```bash
[ -f ~/.ssh/gha_deploy ] || ssh-keygen -t ed25519 -f ~/.ssh/gha_deploy \
    -C "gha-deploy-akiwacu" -N ""
cat ~/.ssh/gha_deploy.pub
```

Puis, pour chaque VM qui ne répond pas :

> 🖥️ **SUR : l'hôte Proxmox** — `https://<ip-proxmox>:8006` → la VM → `Cloud-Init`
>
> Champ `SSH public key` → coller la clé → `Regenerate Image` →
> **`qm stop <id>` puis `qm start <id>`**. Un `reboot` ne réapplique pas cloud-init.
>
> IDs : `301` = vm-dev-g1 · `302` = vm-prod-g1 · `303` = vm-devops-g1

Relance la boucle de vérification. Quand les trois répondent :

**Envoie la clé privée à Andy** — il la met dans les secrets `SSH_KEY_DEV` et
`SSH_KEY_PROD`. Vérifiez ensemble que `SSH_HOST_DEV=192.168.0.50`,
`SSH_HOST_PROD=192.168.0.51` et `SSH_USER_*=deployer`.

## K2 · Ansible sur les 3 VM · 45 min

**Attends qu'Andy ait poussé (A1).** Puis :

```bash
cd ~/projects/Akiwacu
git checkout develop && git pull origin develop
```

`infra/ansible/` arrive **avec ce pull** : `ansible.cfg`, l'inventaire et les
4 playbooks sont des fichiers réels dans le dépôt. Tu n'as rien à recopier depuis le
guide, et rien à créer dans `~/akiwacu/` — ce dossier hors dépôt était une erreur du
guide, il est corrigé.

L'inventaire cible `vm-dev-g1` `.50`, `vm-prod-g1` `.51` et `vm-devops-g1` `.52`,
groupés `app` et `devops`. `vm-ci` et `vm-mon` n'existent plus — c'était ça qui te
bloquait.

```bash
cd infra/ansible
ls        # ansible.cfg  group_vars/  inventory/  playbooks/  roles/

# les collections utilisées par les playbooks, une seule fois
ansible-galaxy collection install community.general community.docker ansible.posix

ansible all -m ping                      # 3 pong attendus
ansible-playbook playbooks/00-base.yml   # paquets, Docker, ufw, fuseau horaire
ansible-playbook playbooks/10-app.yml    # .50 et .51 : dossier, ports, node-exporter
```

`ansible.cfg` déclare l'inventaire : pas besoin de `-i` depuis ce dossier.

Les playbooks `20-monitoring.yml` et `30-sonarqube.yml` lisent un vault chiffré que
tu crées d'abord — voir `infra/ansible/group_vars/all/README.md`. Ils viennent après.

Détail pas à pas : `pack-M2-Klein/GUIDE-INFRA-PAS-A-PAS.md` · vue d'ensemble :
`infra/README.md`.

## K3 · Les deux variables, et attendre le socle · 5 min

```bash
cd ~/projects/Akiwacu
gh variable list                       # les deux doivent être absentes ou false
```

Elles restent **désactivées** aujourd'hui. Tu les actives au rendez-vous n° 3.

Ensuite, tant que le `pom.xml` d'Andy n'existe pas, tu ne peux pas rendre la CI verte.
Occupe ce temps utilement :

- installe **SonarQube** sur `vm-devops-g1` (`vm.max_map_count=524288` d'abord) —
  c'est sur ton chemin critique du D3
- prépare `docker-compose.dev.yml` et vérifie que `docker compose config` passe
- lis `docs/ADR-002-RUNNER-AUTO-HEBERGE.md` : c'est **ton** chapitre de soutenance

---

# Les trois rendez-vous

Ce sont les seuls moments où vous devez vous parler. Entre les deux, chacun avance.

| N° | Déclencheur | Qui fait quoi | Sans ça… |
|---|---|---|---|
| **1** | Andy a poussé le socle v2 (A1) | Klein fait `git pull` avant K2 | Klein rejoue le blocage de vendredi sur l'ancien inventaire |
| **2** | Les 3 VM répondent en SSH (K1) | Klein envoie la clé privée · Andy la met dans les secrets et vérifie les IP | Le job de déploiement échoue en fin de pipeline, après 8 minutes de build |
| **3** | La PR socle est mergée (A4) | Klein active `COVERAGE_GATE`, remet le seuil JaCoCo à 50 % dans le `pom.xml`, puis `SONAR_ENABLED` quand SonarQube tourne · Andy vérifie que la protection de branche est bien en place | Le projet reste sans porte de qualité, et ça se voit à la notation |

---

# Les trois autres, une fois leur pack reçu

Ils ne peuvent rien coder tant que le socle n'est pas mergé — mais ils ont **quatre
heures de travail utile** qui ne dépendent de personne :

| | Aujourd'hui, sans attendre |
|---|---|
| **Juste** | Poste configuré · dépôt cloné · brouillon du MCD · lecture de `SPEC-RECUS.md` |
| **Benitha** | Poste configuré · dépôt cloné · machine à états du cycle écrite sur papier · `SPECS-GHERKIN.md` |
| **Gloria** | Poste configuré · dépôt cloné · `SPECS-GHERKIN.md` · les pièges de R4 et R6 dans `docs/MATRICE-REGLES-METIER.md` |

Dis-leur explicitement : **ne commencez pas à coder d'entités.** Elles arrivent dans
la PR socle, et tout ce qui aura été écrit avant sera à jeter.

---

# La journée est réussie si, ce soir

- [ ] `develop` contient le socle corrigé et 12 documents dans `docs/`
- [ ] Les 3 VM répondent en SSH avec `gha_deploy`
- [ ] Les 93 stories sont dans Jira, Sprint 1 démarré
- [ ] Les 3 packs sont partis
- [ ] La PR socle est ouverte — mergée si possible
- [ ] Une exécution de CI est passée au vert au moins une fois

Les deux dernières lignes sont celles qui comptent. Les quatre premières prennent
1 h 30 et sont mécaniques.
