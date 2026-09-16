# CARTE DES MACHINES — où lancer quoi

**Lis cette page une fois. Elle répond à « je tape ça où ? ».**

Chaque bloc de commandes des guides porte un bandeau :

> 🖥️ **SUR : \<machine\>** — \<dossier ou contexte\>

Si un bloc n'en a pas, c'est un oubli — signale-le.

---

## Les cinq contextes

| # | Machine | Comment y accéder | Ce qu'on y fait |
|---|---|---|---|
| 1 | **Ta machine** (WSL / Linux / macOS) | ton terminal | `git`, `gh`, `ssh`, `ansible`, `terraform`, ton IDE |
| 2 | **L'hôte Proxmox** | navigateur → `https://<ip-proxmox>:8006` → bouton `>_ Shell`<br>ou `ssh root@<ip-proxmox>` | `qm`, `pct`, `pvesm`, `df -h /` |
| 3 | **vm-dev-g1** — `192.168.0.50` | `ssh -i ~/.ssh/gha_deploy deployer@192.168.0.50` | Docker de l'environnement de développement |
| 4 | **vm-prod-g1** — `192.168.0.51` | `ssh -i ~/.ssh/gha_deploy deployer@192.168.0.51` | Docker de l'environnement de production |
| 5 | **vm-devops-g1** — `192.168.0.52` | `ssh -i ~/.ssh/gha_deploy deployer@192.168.0.52` | runner GitHub, SonarQube, Prometheus, Grafana |

---

## La règle qui évite 90 % des erreurs

| Si la commande commence par… | Elle tourne sur… |
|---|---|
| `qm`, `pct`, `pvesm`, `pveum` | **l'hôte Proxmox** — nulle part ailleurs |
| `gh`, `git` | **ta machine**, dans `~/projects/Akiwacu` |
| `ansible`, `ansible-playbook`, `terraform` | **ta machine**, dans `~/projects/Akiwacu/infra/…` |
| `ssh -i ~/.ssh/gha_deploy deployer@…` | **ta machine** — elle t'emmène sur une VM |
| `docker`, `apt`, `systemctl`, `ufw` | **la VM où tu es connecté** |

`qm: command not found` signifie systématiquement : tu es sur ta machine au lieu de l'hôte Proxmox.

---

## Deux pièges de session

**`export` ne survit pas à la fermeture du terminal.** Si tu reprends un guide dans une
nouvelle fenêtre, `$REPO`, `$ORG` et `$G` sont vides. Toutes les commandes `gh api`
renverront **404** avec une URL du type `/repos//collaborators/…`.

Réflexe en début de session, sur ta machine :

```bash
cd ~/projects/Akiwacu
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
ORG=${REPO%%/*}
echo "ORG=[$ORG]  REPO=[$REPO]"
```

**Une modification de cloud-init exige `qm stop` puis `qm start`.** Un simple `reboot`
ne réapplique pas la configuration.

---

## Qui a le droit de faire quoi

| Action | Qui | Pourquoi |
|---|---|---|
| Créer/supprimer des VM Proxmox | **Klein** | Serveur partagé avec les 3 autres groupes |
| Copier le scaffolding depuis le dossier Windows | **Andy uniquement** | Les fichiers sources sont sur son PC. Les autres font `git pull`. |
| `gh secret set`, protections de branche | **Andy uniquement** | Nécessite le rôle admin sur le dépôt |
| Générer le token du runner | **Andy**, à la demande de Klein | Expire en 1 heure |
| Pousser sur `develop` | **personne directement** | Toujours par PR |

---

## Le flux du socle projet

C'est la source de confusion la plus fréquente. Les fichiers de départ vivent sur le PC
d'Andy, pas dans le dépôt.

```
PC d'Andy : C:\…\JEE&DevOps\project-kit\repo-scaffolding\
        │
        │  ANDY seul, une fois : cp + git commit + git push
        ▼
GitHub : Akiwacu-Labs/Akiwacu (branche develop)
        │
        │  TOUS : git pull
        ▼
~/projects/Akiwacu sur chaque machine
```

**Personne d'autre qu'Andy ne copie depuis `/mnt/c/Users/Andy/…`.** Ce chemin n'existe
que chez lui. Si un guide te demande de le faire et que tu n'es pas Andy, c'est une
erreur du guide.

Quand Andy pousse une correction du socle, les autres récupèrent :

```bash
cd ~/projects/Akiwacu
git checkout develop && git pull origin develop
```
