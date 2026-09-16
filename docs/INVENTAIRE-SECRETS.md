# INVENTAIRE DES SECRETS ET VARIABLES

**Aucune valeur ne figure dans ce fichier.** Il dit *quoi* existe, *où*, *qui* le pose
et *comment* le remplacer. Le dépôt est **public** : c'est la seule façon d'écrire
une documentation des secrets qui soit versionnable.

Propriétaire de tous les secrets : **Andy**. GitHub exige le droit `admin` sur le
dépôt pour créer un secret ou une variable — un accès `write` ne suffit pas. Ce n'est
pas une contrainte subie, c'est la bonne répartition : celui qui pilote détient les
identifiants de déploiement, ceux qui développent n'en ont pas besoin.

---

## 1. Secrets GitHub Actions

`Settings → Secrets and variables → Actions → Secrets`, ou `gh secret list`.

| Nom | Contenu | Posé quand | Utilisé par |
|---|---|---|---|
| `SSH_KEY_DEV` | clé **privée** `~/.ssh/gha_deploy` | D3 | `deploy-vm-dev` |
| `SSH_HOST_DEV` | `192.168.0.50` | D3 | `deploy-vm-dev` |
| `SSH_USER_DEV` | `deployer` | D3 | `deploy-vm-dev` |
| `POSTGRES_PASSWORD_DEV` | **généré**, `openssl rand -hex 16` | D3 | `docker compose` sur vm-dev-g1 |
| `JWT_SECRET_DEV` | **généré**, `openssl rand -hex 32` (256 bits, exigé par HS256) | D3 | l'API sur vm-dev-g1 |
| `SONAR_TOKEN` | jeton émis par SonarQube | quand SonarQube tourne | `build-test-quality` |
| `SONAR_HOST_URL` | `http://192.168.0.52:9000` | idem | `build-test-quality` |
| `SSH_*_PROD`, `POSTGRES_PASSWORD_PROD`, `JWT_SECRET_PROD` | équivalents production | avant la démo | `cicd-main.yml` |
| `GITHUB_TOKEN` | — | **jamais** | fourni automatiquement par Actions |

`GITHUB_TOKEN` n'est pas à créer. GitHub l'injecte à chaque job, avec les droits
déclarés dans le bloc `permissions:` du workflow. C'est lui qui pousse sur GHCR.

### Deux secrets sont inventés, pas trouvés

`POSTGRES_PASSWORD_DEV` et `JWT_SECRET_DEV` n'existent nulle part avant qu'on les
crée. Ils deviennent la vérité au moment où on les pose.

```bash
mkdir -p ~/.akiwacu-secrets && chmod 700 ~/.akiwacu-secrets
openssl rand -hex 16 | tr -d '\n' > ~/.akiwacu-secrets/postgres_dev
openssl rand -hex 32 | tr -d '\n' > ~/.akiwacu-secrets/jwt_dev
chmod 600 ~/.akiwacu-secrets/*

gh secret set POSTGRES_PASSWORD_DEV < ~/.akiwacu-secrets/postgres_dev
gh secret set JWT_SECRET_DEV        < ~/.akiwacu-secrets/jwt_dev
```

**`-hex` et non `-base64` :** la valeur traverse `export VAR='…'` puis une URL JDBC.
Un `/`, un `+` ou une apostrophe casse l'un des deux, et l'erreur affichée est un
refus d'authentification qui ne désigne rien.

**`tr -d '\n'` :** sans lui, le retour à la ligne fait partie du secret. Postgres
reçoit un mot de passe avec `\n`, l'application un sans. Une heure perdue, garantie.

**`~/.akiwacu-secrets` est hors du dépôt.** Pas dans `~/projects/Akiwacu`, même
ignoré par `.gitignore`. Un dépôt public ne mérite pas cette confiance.

---

## 2. Variables GitHub Actions

Même écran, onglet `Variables`. Elles ne sont **pas** chiffrées : elles s'affichent
en clair dans les journaux. Ce sont des interrupteurs, jamais des identifiants.

| Nom | Effet quand `true` | À activer |
|---|---|---|
| `COVERAGE_GATE` | `jacoco:check` devient bloquant | après le merge du socle, seuil à 0.50 |
| `SONAR_ENABLED` | analyse + Quality Gate SonarQube | quand SonarQube répond |
| `CLIENT_ENABLED` | build et publication du client React | D5 |
| `DEPLOY_ENABLED` | déploiement sur vm-dev-g1 | quand les 5 secrets `*_DEV` existent |

```bash
gh variable set DEPLOY_ENABLED --body true
gh variable list
```

**Toujours l'interrupteur en dernier.** `DEPLOY_ENABLED=true` sans les secrets ne
produit rien d'autre qu'un pipeline rouge.

---

## 3. Ce qui ne vit pas sur GitHub

| Quoi | Où | Dans git ? |
|---|---|---|
| Mot de passe Postgres **local** de chacun | `api/.env` sur son poste | non — `.gitignore` |
| `infra/ansible/vault.yml` | poste d'Andy et de Klein | **non** — un vault sur dépôt public n'est protégé que par sa passphrase |
| Clés SSH privées | `~/.ssh/` | jamais |
| Jeton d'enregistrement du runner | usage unique, expire en 1 h | jamais |
| Copie de référence des valeurs | `~/.akiwacu-secrets/`, mode 600 | jamais |

Le mot de passe Postgres local n'a aucune raison d'être le même que
`POSTGRES_PASSWORD_DEV`. Ce sont deux bases différentes sur deux machines
différentes.

---

## 4. On ne relit jamais un secret

`gh secret list` affiche les **noms** et la date de dernière modification. Jamais les
valeurs — ni en ligne de commande, ni dans l'interface web. C'est le comportement
attendu, pas une limitation.

Conséquence : **une valeur perdue ne se retrouve pas, elle se remplace.** D'où la
copie dans `~/.akiwacu-secrets` posée au moment de la création.

Remplacer un secret : le reposer avec le même nom, la valeur est écrasée.

```bash
openssl rand -hex 32 | tr -d '\n' > ~/.akiwacu-secrets/jwt_dev
gh secret set JWT_SECRET_DEV < ~/.akiwacu-secrets/jwt_dev
```

Changer `JWT_SECRET_DEV` invalide tous les jetons déjà émis : les sessions ouvertes
sont fermées. Sans conséquence ici, à connaître ailleurs.

---

## 5. Le piège du volume Postgres

`POSTGRES_PASSWORD` n'est lu qu'à l'**initialisation** du répertoire de données.
Si un volume existe déjà sur vm-dev-g1, changer le secret ne change pas le mot de
passe de la base.

Symptôme : `password authentication failed for user "akiwacu"` sur un déploiement
qui devrait fonctionner. Correctif, sur vm-dev-g1 :

```bash
cd ~/akiwacu && docker compose down -v    # -v supprime le volume
```

`-v` détruit les données. En développement c'est sans importance ; ailleurs cette
commande ne se tape pas sans réfléchir.

---

## 6. Vérification avant la soutenance

```bash
gh secret list      # 5 secrets *_DEV au minimum
gh variable list     # DEPLOY_ENABLED=true, les autres selon l'avancement
```

Et la question que le jury peut poser — « où sont vos mots de passe ? » :

> Aucun n'est dans le dépôt, qui est public. Ils sont dans les secrets GitHub
> Actions, chiffrés, injectés dans le job en variables d'environnement et masqués
> dans les journaux. Seul le compte administrateur du dépôt peut les poser, et
> personne ne peut les relire. Ce fichier documente lesquels existent, sans aucune
> valeur.
