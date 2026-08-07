# GUIDE GIT & GITHUB — pas à pas, sans rien supposer

> Ce guide part du principe que tu n'as jamais travaillé à cinq sur un dépôt.
> Suis-le à la lettre. Si une commande renvoie une erreur que tu ne comprends pas,
> **copie-colle l'erreur dans le groupe** — ne bricole pas.

---

## 0. Une fois pour toutes : configurer Git

```bash
git config --global user.name "Ton Prénom Nom"
git config --global user.email "ton.email@exemple.com"   # LE MÊME que sur GitHub
git config --global init.defaultBranch main
git config --global pull.rebase true          # évite les merges parasites
git config --global core.autocrlf input       # Linux/Mac/WSL
```

> ⚠ L'email **doit** être celui de ton compte GitHub, sinon tes commits ne te seront
> pas attribués. Et l'évaluation est **individuelle** : un commit non attribué est un
> commit qui ne compte pas pour toi. Vérifie sur github.com → Settings → Emails.

Vérification :
```bash
git config --global --list | grep user
```

---

## 1. Installer GitHub CLI et se connecter

**Ubuntu / WSL :**
```bash
sudo mkdir -p -m 755 /etc/apt/keyrings
wget -qO- https://cli.github.com/packages/githubcli-archive-keyring.gpg \
  | sudo tee /etc/apt/keyrings/githubcli-archive-keyring.gpg > /dev/null
sudo chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
  | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update && sudo apt install gh -y
```

**Connexion :**
```bash
gh auth login
```
Réponds : `GitHub.com` → `HTTPS` → `Yes` (authentifier Git avec tes identifiants) →
`Login with a web browser` → copie le code affiché → colle-le dans le navigateur.

```bash
gh auth status     # doit afficher "Logged in to github.com as <ton-pseudo>"
```

---

## 2. Cloner le dépôt

```bash
cd ~
gh repo clone <ORG>/akiwacu
cd akiwacu
git checkout develop        # on ne travaille JAMAIS sur main
```

Vérifie que tu es au bon endroit :
```bash
git branch --show-current   # doit afficher : develop
git remote -v               # doit pointer vers le dépôt de l'équipe
```

---

## 3. Le cycle de travail — à répéter pour CHAQUE fonctionnalité

C'est la boucle que tu vas faire 15 à 20 fois en 8 jours. Apprends-la par cœur.

### Étape 1 — Prendre un ticket Jira et le passer « In Progress »

Ne commence jamais à coder sans ticket. Voir [`GUIDE-JIRA-SCRUM.md`](GUIDE-JIRA-SCRUM.md).

### Étape 2 — Partir de `develop` à jour

```bash
git checkout develop
git pull origin develop     # TOUJOURS. Sinon tu pars d'une base périmée.
```

### Étape 3 — Créer ta branche

```bash
git checkout -b feat/cotisation-saisie-rapide
```

**Nommage — non négociable :**

| Préfixe | Quand | Exemple |
|---|---|---|
| `feat/` | nouvelle fonctionnalité | `feat/pret-calcul-echeancier` |
| `fix/` | correction de bug | `fix/vote-double-commissaire` |
| `test/` | ajout de tests seuls | `test/cycle-machine-etats` |
| `chore/` | outillage, dépendances | `chore/ajout-jacoco` |
| `docs/` | documentation | `docs/readme-cotisation` |

> **Une branche = une fonctionnalité = 1 à 2 jours maximum.**
> Jamais de branche personnelle longue durée du type `branche-benitha`. Voir §7.

### Étape 4 — Coder, et committer souvent

```bash
git add src/main/java/bi/ac/upg/akiwacu/cotisation/CotisationService.java
git commit -m "feat(cotisation): ajouter la saisie rapide multi-membres"
```

**Format des messages — Conventional Commits :**

```
<type>(<domaine>): <ce que ça fait, à l'infinitif>
```

Bons exemples :
```
feat(cotisation): ajouter la saisie rapide multi-membres
fix(pret): corriger le calcul de l'épargne pour R6
test(vote): couvrir le refus de double vote d'un commissaire
docs(readme): documenter le lancement local
chore(ci): activer le seuil JaCoCo à 80%
```

Interdits — ils te coûtent des points, la qualité des commits est **notée** :
```
update      fix bug      wip      .      asdf      "modifs"
```

> **Minimum 2 commits par jour ouvré.** La régularité est un critère d'évaluation
> explicite de l'énoncé. Un seul gros commit le dernier jour est très visible et très
> mal noté.

### Étape 5 — Pousser ta branche

```bash
git push -u origin feat/cotisation-saisie-rapide
```

### Étape 6 — Ouvrir la Pull Request

```bash
gh pr create --base develop --fill
```

Ou en remplissant proprement (recommandé) :
```bash
gh pr create --base develop \
  --title "feat(cotisation): saisie rapide multi-membres" \
  --body "Closes AKW-42

## Ce que fait cette PR
Permet au trésorier d'enregistrer plusieurs cotisations en une seule requête.

## Règles métier
- R2 : vérifie le cycle actif via CycleGuardService
- R5 : enregistre le trésorier validateur

## Comment tester
POST /api/cotisations/batch avec le payload d'exemple du README.

## Assistance IA
- [x] Code généré avec assistance IA
- [x] Relu ligne par ligne, je peux l'expliquer intégralement"
```

Le template de PR se remplit automatiquement — **coche vraiment les cases**, ne les
laisse pas vides.

### Étape 7 — Attendre la CI et la revue

```bash
gh pr checks --watch      # suit la CI en direct
gh pr view --web          # ouvre la PR dans le navigateur
```

Si la CI est rouge : tu corriges **sur la même branche**, tu recommittes, tu repousses.
La PR se met à jour toute seule.

### Étape 8 — Merger

Une fois la CI verte **et** une revue approuvée :

```bash
gh pr merge --merge --delete-branch
```

> ⚠ **`--merge`, jamais `--squash`.** Le squash écrase tes commits individuels en un
> seul. Or c'est exactement ton historique individuel qui est noté. Ne squash jamais.

### Étape 9 — Vérifier sur VM-DEV

Le merge déclenche le déploiement. Deux minutes plus tard :

```bash
curl http://<IP-VM-DEV>:8080/actuator/health
```

Puis ouvre Swagger sur VM-DEV et **exécute réellement ton endpoint**. Tant que tu ne
l'as pas vu répondre sur VM-DEV, ta story n'est pas *Done*.

### Étape 10 — Fermer le ticket Jira

Passe-le en « Done ». Voir [`GUIDE-JIRA-SCRUM.md`](GUIDE-JIRA-SCRUM.md).

---

## 4. Relire la PR de quelqu'un d'autre

**Ta participation aux revues est notée séparément.** Une revue orale n'existe pas dans
l'historique GitHub — donc elle ne compte pas. Écris-les.

```bash
gh pr list                          # voir les PR ouvertes
gh pr checkout 12                   # récupérer la PR n°12 en local pour la tester
gh pr diff 12                       # voir le diff
```

Checklist du relecteur — colle-la en commentaire :

```
- [ ] Règles métier dans le service, pas dans le contrôleur
- [ ] Cas d'erreur ET cas nominal testés
- [ ] Pas de secret, pas de valeur en dur
- [ ] Endpoint annoté OpenAPI
- [ ] tontineId pris depuis le JWT, jamais depuis le body (R1)
- [ ] Montants en BigDecimal, jamais double
- [ ] Je comprends ce code sans que l'auteur me l'explique
```

Approuver ou demander des changements :
```bash
gh pr review 12 --approve --body "Vérifié : R2 bien appelée dans le service, tests d'erreur présents."
gh pr review 12 --request-changes --body "Ligne 42 : R6 compare au total cotisé, mais l'épargne devrait être nette des prêts en cours. Cas non couvert par les tests."
```

Un commentaire utile cite **une ligne précise** et **un problème précis**.
`👍 LGTM` ne vaut aucun point.

---

## 5. Quand ça se passe mal

### « Mon push est refusé »
```
! [rejected] develop -> develop (fetch first)
```
Tu essaies de pousser sur `develop`. **C'est interdit et c'est protégé.** Crée une branche :
```bash
git checkout -b feat/mon-truc
git push -u origin feat/mon-truc
```

### « J'ai des conflits de merge »
```bash
git checkout develop && git pull origin develop
git checkout ma-branche
git rebase develop
# Git s'arrête sur chaque conflit. Ouvre le fichier, cherche les <<<<<<<
# Garde le bon code, supprime les marqueurs <<<<<<< ======= >>>>>>>
git add <fichier-corrigé>
git rebase --continue
git push --force-with-lease        # --force-with-lease, JAMAIS --force
```
> Si tu as plus de 3 conflits, **arrête-toi et appelle Andy**. Ne force rien.

### « J'ai commité sur develop par erreur »
```bash
git branch feat/sauvetage        # sauve ton travail sur une nouvelle branche
git reset --hard origin/develop  # remet develop propre
git checkout feat/sauvetage      # continue là
```

### « J'ai commité un mot de passe / un secret »
**Préviens Andy immédiatement.** Ne te contente pas de le supprimer dans un commit
suivant : il reste dans l'historique, et le dépôt est **public**. Il faut régénérer
le secret et réécrire l'historique. Ce n'est pas grave si c'est dit tout de suite.

### « J'ai perdu mon travail »
```bash
git reflog        # liste tout ce que Git a mémorisé, même les commits "perdus"
git checkout <hash>
```
Git n'oublie presque jamais rien pendant 90 jours.

---

## 6. Commandes de survie

```bash
git status                    # où j'en suis
git log --oneline -10         # mes 10 derniers commits
git log --author="Benitha" --oneline    # vérifier MES commits
git diff                      # ce que j'ai modifié et pas encore ajouté
git diff --staged             # ce qui est ajouté et pas encore commité
git stash                     # mettre de côté temporairement
git stash pop                 # récupérer
gh pr status                  # état de mes PR
gh run list --limit 5         # 5 derniers builds CI
gh run watch                  # suivre le build en cours
```

---

## 7. Pourquoi une branche par fonctionnalité, et pas une branche par personne

C'est la question qui revient toujours. La réponse compte pour ta note.

| Branche personnelle longue | Branche par fonctionnalité |
|---|---|
| Merge une fois, à la fin → **une seule intégration comptée** | 15 merges → 15 fonctionnalités comptées dans `develop` |
| Diverge pendant 8 jours → conflits massifs le dernier jour | Conflits rares et petits |
| PR de 2000 lignes → revue tamponnée sans être lue | PR de 200 lignes → vraie revue, vrais commentaires |
| Graphe Git illisible | Le correcteur voit qui a livré quoi et quand |

L'énoncé note « le nombre de fonctionnalités **effectivement intégrées dans la branche
`develop`** ». Une branche personnelle mergée à la fin, c'est une fonctionnalité.

**Une branche = une fonctionnalité = 1 à 2 jours. Supprimée après le merge.**

---

## 8. Les 6 règles absolues

1. **Jamais de push direct** sur `develop` ni `main`. Toujours par PR.
2. **Jamais de squash merge.** `--merge`, point.
3. **Jamais commiter à la place de quelqu'un d'autre.** En binôme : `Co-authored-by: Nom <email>`.
4. **Minimum 2 commits par jour ouvré.**
5. **Aucun secret dans Git.** Le dépôt est public. `.env` est dans `.gitignore`.
6. **PR de moins de 400 lignes modifiées.**
