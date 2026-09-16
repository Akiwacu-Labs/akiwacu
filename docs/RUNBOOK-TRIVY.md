# RUNBOOK — amorcer et rafraîchir la base Trivy

**Machine : `vm-devops-g1` (192.168.0.52), en SSH.** Propriétaire : **Klein**.

Le pipeline n'essaie plus de télécharger la base de vulnérabilités pendant un job.
Elle vit sur la VM et le job la lit. Pourquoi : `DECISIONS.md` D-30.

---

## 1. Amorçage — à faire UNE fois

Le téléchargement dure environ **30 minutes** (107 Mo à 60 Ko/s). Il faut donc le
lancer dans un `tmux`, sinon une déconnexion SSH le tue et tout est à refaire.

```bash
ssh deployer@192.168.0.52
tmux new -s trivy

docker run --rm \
  -v /home/runner/.trivy-cache:/root/.cache \
  aquasec/trivy:latest image --download-db-only --timeout 60m
```

`Ctrl-b` puis `d` détache la session ; la commande continue.
`tmux attach -t trivy` pour revenir voir où elle en est.

**Vérification — c'est la seule qui compte :**

```bash
sudo su runner                                    # /home/runner est en 750
ls -lh /home/runner/.trivy-cache/trivy/db/trivy.db
df -h /home                                       # la base pèse 1,2 Go
```

Attendu : **environ 1,2 Go**. La base voyage compressée (107 Mo) et se décompresse
sur disque — ne cherche pas un fichier de 107 Mo, il n'existe pas.

**Deux pièges de chemin, tous les deux déjà rencontrés :**

| Ce qu'on tape spontanément | Ce qui marche | Pourquoi |
|---|---|---|
| `ls /home/runner/.trivy-cache/db/` → *No such file* | `…/.trivy-cache/**trivy**/db/` | Trivy crée son propre sous-dossier `trivy/` sous la racine du cache |
| `ls …` en `deployer` → *Permission denied* | `sudo su runner` d'abord | `/home/runner` est en `750 runner:runner` |

`Permission denied` et `No such file` ne veulent donc **pas** dire que la base est
absente. Ils veulent dire que tu regardes au mauvais endroit, ou avec le mauvais
utilisateur. La CI, elle, tourne en `runner` : c'est son point de vue qui compte.

**Le silence n'est pas un échec.** `--download-db-only` qui rend la main sans rien
afficher signifie que la base locale est déjà à jour. Pour voir ce qu'il fait
réellement, ajoute `--debug` :

```bash
docker run --rm -v /home/runner/.trivy-cache:/root/.cache \
  aquasec/trivy:latest --debug image --download-db-only
```

Les deux lignes à lire : `DB update was skipped because the local DB is the latest`
et `DB info … next_update=…`.

Si la base est vraiment absente, le pipeline s'arrête à l'étape Trivy avec
`Base Trivy absente de …` — c'est voulu, ça vaut mieux qu'une expiration de sept
minutes qui n'explique rien.

> Le conteneur Trivy tourne en `root` : les fichiers du cache appartiennent à
> `root`. Ce n'est pas un problème, le job utilise le même conteneur `root`.
> N'y touche pas avec `chown`.

---

## 2. Rafraîchissement — une fois par semaine

La base vieillit. Trivy la considère périmée au bout de 24 h mais, avec
`--skip-db-update`, il s'en contente et le signale par un avertissement.
Une base d'une semaine reste pertinente pour un projet noté ; une base d'un mois
n'a plus de valeur d'analyse.

Même commande, le lundi matin, dans un `tmux` :

```bash
docker run --rm \
  -v /home/runner/.trivy-cache:/root/.cache \
  aquasec/trivy:latest image --download-db-only --timeout 60m
```

**Ne pas la lancer pendant qu'un pipeline tourne.** Deux processus Trivy qui
écrivent le même cache le corrompent, et il faut alors tout retélécharger :

```bash
# uniquement si le cache est corrompu
rm -rf /home/runner/.trivy-cache/trivy
```

---

## 3. Ce que l'étape analyse réellement

```
--pkg-types os
```

Les paquets du système de base de l'image — Alpine pour l'API, Alpine/nginx pour le
client. **Pas** les dépendances Java de `app.jar` : cela exige la `trivy-java-db`,
environ 700 Mo, soit plus de trois heures de téléchargement ici.

C'est une limitation réelle, et elle se dit telle quelle au jury : une CVE dans une
dépendance Maven ne serait pas remontée par cette étape. Ce qui la couvre en partie :
le Quality Gate SonarQube et une lecture de `./mvnw dependency:tree`.

Si la liaison le permet un jour, la base Java s'amorce de la même façon —
et on retire `--pkg-types os` :

```bash
docker run --rm \
  -v /home/runner/.trivy-cache:/root/.cache \
  aquasec/trivy:latest image --download-java-db-only --timeout 240m
```

---

## 4. Quand l'analyse échoue avec `exit code 1`

Ce n'est **pas** une panne. `--severity CRITICAL --exit-code 1` signifie : une CVE
critique et corrigeable a été trouvée dans l'image. Le pipeline fait son travail.

Trois réponses acceptables, dans cet ordre :

1. **Mettre à jour l'image de base** — `eclipse-temurin:21-jre-alpine` bouge souvent.
   `docker pull` puis relancer. C'est presque toujours la bonne réponse.
2. **Ignorer une CVE précise, avec sa justification écrite**, dans un fichier
   `.trivyignore` à la racine de `api/` : un identifiant par ligne, précédé d'un
   commentaire disant pourquoi. Un `.trivyignore` sans commentaire est un aveu.
3. **Rien**, et l'expliquer dans le rapport. Une CVE analysée et argumentée vaut
   mieux qu'un seuil abaissé en silence.

**Ce qu'on ne fait pas :** passer `--severity` à `HIGH` pour faire disparaître le
rouge. Le rapport `trivy-report.json`, lui, sort en `CRITICAL,HIGH,MEDIUM` sans
seuil bloquant — c'est le livrable, il est complet ; l'étape bloquante est plus
stricte, c'est normal.

---

## 5. Où atterrissent les rapports

```
/home/runner/akiwacu-artifacts/run-<numéro>/trivy-report.json
/home/runner/akiwacu-artifacts/derniere/          # lien vers la dernière exécution
```

Récupération au D7. `/home/runner` est en `750` et le seul compte joignable en SSH
est `deployer` : la copie passe par un dépôt intermédiaire, **sans toucher aux
permissions du home**.

Sur la VM :

```bash
sudo cp -r /home/runner/akiwacu-artifacts/derniere /tmp/artefacts-ci
sudo chown -R deployer:deployer /tmp/artefacts-ci
```

Depuis ton poste :

```bash
scp -r deployer@192.168.0.52:/tmp/artefacts-ci ./livrables/ci/
```

> On ne relâche pas les droits de `/home/runner` pour une copie qu'on fait une fois.
> Un `chmod` sur un répertoire personnel se garde ; un `cp` dans `/tmp` s'oublie
> tout seul.
