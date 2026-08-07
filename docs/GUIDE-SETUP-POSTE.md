# CONFIGURATION DU POSTE — à faire le D1, avant tout le reste

> Tu ne dois avoir aucune question à la fin de ce document. Si une commande échoue,
> **copie-colle l'erreur complète dans le groupe.** Ne bricole pas, ne saute pas d'étape.
> Un poste mal configuré au D2, c'est une journée perdue sur huit.

Durée : 45 à 60 minutes. À faire en entier en D1.

---

## Sous Windows : installe WSL d'abord

Ouvre PowerShell **en administrateur** :

```powershell
wsl --install -d Ubuntu-24.04
```

Redémarre. Ouvre « Ubuntu » depuis le menu Démarrer, crée ton utilisateur.
**Tout le reste de ce guide se fait dans le terminal Ubuntu, pas dans PowerShell.**

> Ton dossier Windows est accessible depuis WSL à `/mnt/c/Users/<TonNom>/`.
> Mais travaille **dans le système de fichiers Linux** (`~/`), c'est cinq fois plus rapide.

---

## 1. Paquets de base

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl wget git unzip zip build-essential ca-certificates gnupg
```

---

## 2. Java 21 via SDKMAN

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.5-tem
java -version      # doit afficher 21.x
```

> Maven n'est pas à installer : on utilise le wrapper `./mvnw` fourni dans le dépôt.

---

## 3. Docker

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker
docker run hello-world      # doit afficher "Hello from Docker!"
docker compose version
```

Sous Windows, tu peux aussi installer Docker Desktop et activer l'intégration WSL2
(`Settings` → `Resources` → `WSL Integration` → activer Ubuntu-24.04).

---

## 4. Node 20 et pnpm (pour la partie React)

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.bashrc
nvm install 20 && nvm use 20
corepack enable && corepack prepare pnpm@latest --activate
node -v && pnpm -v
```

---

## 5. Git et GitHub CLI

```bash
git config --global user.name "Ton Prénom Nom"
git config --global user.email "ton.email@exemple.com"   # LE MÊME que sur GitHub
git config --global init.defaultBranch main
git config --global pull.rebase true

sudo mkdir -p -m 755 /etc/apt/keyrings
wget -qO- https://cli.github.com/packages/githubcli-archive-keyring.gpg \
  | sudo tee /etc/apt/keyrings/githubcli-archive-keyring.gpg > /dev/null
sudo chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
  | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update && sudo apt install gh -y

gh auth login     # GitHub.com → HTTPS → Yes → Login with a web browser
gh auth status
```

> ⚠ **L'email doit être celui de ton compte GitHub.** Sinon tes commits ne te sont pas
> attribués — et l'évaluation est individuelle. Vérifie sur github.com → Settings → Emails.

---

## 6. IntelliJ IDEA

Télécharge sur https://www.jetbrains.com/idea/download/

> **Prends Ultimate, il est gratuit pour toi** via le GitHub Student Developer Pack
> (https://education.github.com/pack). Le support Spring y est nettement meilleur que
> dans la Community. Ça vaut les dix minutes de vérification de statut étudiant.

**Extensions à installer — obligatoires :**

| Extension | Pourquoi |
|---|---|
| **SonarQube for IDE** (ex-SonarLint) | ⭐ La plus importante. En mode connecté, tu vois les problèmes Sonar **dans l'éditeur**, avant le commit. Sans elle, tu découvriras 200 issues au D6 et le Quality Gate bloquera toute l'équipe. |
| **Lombok** | Sinon rien ne compile dans l'IDE |
| **Spring Boot Assistant** | Autocomplétion de `application.yml` |
| **Conventional Commit** | T'aide à respecter le format des messages |

Klein te donnera l'URL et le token SonarQube au daily du D3 pour le mode connecté.

---

## 7. VS Code (pour la partie React)

```bash
# sous Windows : télécharge sur code.visualstudio.com puis installe l'extension "WSL"
code --version
```
Extensions : **ESLint** · **Prettier** · **Tailwind CSS IntelliSense** · **WSL**

---

## 8. Outils annexes

- **Bruno** — https://www.usebruno.com/downloads (client API ; les collections sont des
  fichiers `.bru` versionnés dans le dépôt, sous `bruno/`)
- **DBeaver** — https://dbeaver.io/download/ (client PostgreSQL)

---

## 9. Claude Code

```bash
npm install -g @anthropic-ai/claude-code
claude --version
```

Puis crée ton fichier personnel :

```bash
mkdir -p ~/.claude
nano ~/.claude/CLAUDE.md
```

Colle-y le bloc « `~/.claude/CLAUDE.md` » qui se trouve dans **ton** [`LIS-MOI-DABORD.md`](../LIS-MOI-DABORD.md).
C'est ce qui fait que Claude Code sait qui tu es et ce que tu possèdes, sans que tu aies
à le lui réexpliquer à chaque session.

---

## 10. Cloner le projet et vérifier que tout marche

```bash
cd ~
gh repo clone akiwacu-labs/akiwacu
cd akiwacu
git checkout develop

docker compose up -d          # PostgreSQL en local
./mvnw clean verify           # compile + tests + couverture
./mvnw spring-boot:run        # démarre l'API
```

Dans un autre terminal :
```bash
curl http://localhost:8080/actuator/health      # {"status":"UP"}
```

Puis ouvre dans ton navigateur :
- **http://localhost:8080/swagger-ui.html** — la documentation de l'API
- **http://localhost:8080/actuator/prometheus** — les métriques

---

## ✅ Ta checklist de fin de D1

- [ ] `java -version` affiche 21
- [ ] `docker run hello-world` fonctionne
- [ ] `node -v` affiche v20
- [ ] `gh auth status` me montre connecté
- [ ] `git config --global user.email` = mon email GitHub
- [ ] IntelliJ ouvre le projet sans erreur
- [ ] SonarQube for IDE est installé
- [ ] `./mvnw clean verify` passe au vert
- [ ] Swagger UI s'ouvre en local
- [ ] `~/.claude/CLAUDE.md` contient mon bloc personnel
- [ ] **J'ai poussé au moins un commit sur une branche**

Le dernier point est le vrai test. Tant que tu n'as pas poussé, tu n'es pas prêt.

---

## Si ça ne marche pas

| Symptôme | Solution |
|---|---|
| `docker: permission denied` | `sudo usermod -aG docker $USER` puis **ferme et rouvre** le terminal |
| `./mvnw: Permission denied` | `chmod +x mvnw` |
| `port 5432 already in use` | Un PostgreSQL tourne déjà : `sudo systemctl stop postgresql` |
| `java: command not found` après redémarrage | `source ~/.sdkman/bin/sdkman-init.sh`, et ajoute-le à `~/.bashrc` |
| Tests qui échouent au premier `verify` | Normal si le socle n'est pas encore mergé. Demande à Andy où il en est. |
| WSL très lent | Tu travailles sûrement dans `/mnt/c/`. Déplace le projet dans `~/`. |

**Toute autre erreur : copie-colle le message complet dans le groupe.**
Ne perds pas une heure seule là-dessus — au D1, ce n'est pas ton temps le plus utile.
