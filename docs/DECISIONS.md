# DÉCISIONS — journal du projet Akiwacu

Ce fichier enregistre ce qui a été décidé, ce qui avait été envisagé avant, et ce que
la décision impose. Il sert à trois choses : répondre aux questions de la soutenance
sans improviser, éviter de rejouer un arbitrage déjà tranché, et permettre de refaire
le projet depuis zéro sans repasser par les mêmes erreurs.

**Lecture obligatoire avant de contredire une décision.** Si tu veux en changer une,
ouvre la discussion au daily et modifie l'entrée — ne la contourne pas en silence.

Ordre : chronologique. `⟲` signale une décision **révisée en cours de route** — c'est
là que se trouvent les pièges d'une reprise à zéro.

---

## Périmètre et organisation

### D-01 — Découpage par domaine métier, pas par couche

**Décidé :** chaque membre possède un tronçon vertical complet — entité, repository,
service, contrôleur, DTO, tests, écran client.

**Envisagé d'abord :** un découpage par couche (un backend, un frontend, un DevOps).

**Motif :** l'énoncé §13 note individuellement. Un découpage par couche rend la
contribution de chacun illisible dans l'historique Git et empêche de présenter
5 minutes de bout en bout à la soutenance.

**Conséquence :** personne ne touche au domaine d'un autre sans l'annoncer au daily.
Les interfaces partagées (`RecuService`, `CycleGuardService`) sont publiées en avance
sous forme de stubs pour que les dépendants compilent.

---

### D-02 ⟲ — Huit jours, pas quatorze

**Décidé :** D1 jeudi 6 août → D8 jeudi 13 août. Soutenance vendredi 14.

**Révisé depuis :** un planning initial sur 14 jours, avec 4 sprints.

**Motif :** la date de soutenance a été annoncée après la rédaction du premier plan.

**Conséquence :** 3 sprints de 3/3/2 jours. Week-end travaillé. Aucun jour tampon.
Le design Figma passe de 4 heures à 45 minutes et se limite à l'écran de saisie
rapide des cotisations. Le playbook 14 jours est archivé, pas supprimé —
`_ARCHIVE/00-PM-PLAYBOOK-14J.md`.

---

### D-03 — Monorepo `api/` + `client/`

**Décidé :** un seul dépôt, `api/` pour Spring Boot, `client/` pour React.

**Envisagé d'abord :** deux dépôts séparés.

**Motif :** une PR qui change un endpoint et l'écran qui le consomme reste atomique.
Une seule chaîne CI à maintenir sur 8 jours.

**Conséquence, souvent oubliée :** `pom.xml` n'est **pas** à la racine. Tout job
Maven de la CI porte `defaults.run.working-directory: api`. Le contexte de build
Docker est `./api` ou `./client`, jamais `.`. Les chemins JaCoCo sont
`api/target/site/jacoco/`.

**Piège écarté :** les filtres `paths:` sur les workflows. Un job filtré qui ne
s'exécute pas ne rapporte jamais son statut, et une branche protégée l'attend
indéfiniment. Résultat : PR immergeable. On ne filtre pas.

---

### D-04 — Dépôt public, sous une organisation

**Décidé :** organisation GitHub, dépôt **public**.

**Envisagé d'abord :** un dépôt personnel privé.

**Motif :** deux contraintes se croisent. La protection de branche n'est gratuite que
sur un dépôt public. Et un dépôt personnel ne permet pas d'accorder un accès en
lecture-revue à l'enseignant sans lui donner l'écriture.

**Conséquence :** aucun secret dans le code, jamais — tout passe par
`gh secret set`. Le code est visible des autres groupes ; c'est assumé, le rendu est
noté sur l'historique individuel et la démo, pas sur l'originalité du code.

---

### D-05 — Pas de licence, mais un `NOTICE.md`

**Décidé :** aucun fichier `LICENSE`. Un `NOTICE.md` qui indique « travail
académique, tous droits réservés, UPG Gitega 2026 ».

**Motif :** sans licence explicite, le droit d'auteur par défaut s'applique — le code
est visible mais pas réutilisable. C'est exactement l'effet recherché.

---

### D-06 — L'enseignant en **Triage**

**Décidé :** `@cincotech` reçoit le rôle Triage sur le dépôt.

**Motif :** Triage donne la lecture, la revue, les commentaires et la gestion des
issues, sans aucun droit de push.

**Conséquence :** un utilisateur Triage est **silencieusement ignoré** par
`CODEOWNERS`. Il ne doit donc apparaître dans aucun chemin de ce fichier.

---

## Git et qualité

### D-07 — Branches de fonctionnalité, jamais de branche personnelle

**Décidé :** `feat/<domaine>-<slug>`, créée depuis `develop`, supprimée après merge.

**Envisagé d'abord :** une branche longue par personne.

**Motif :** une branche personnelle vivant 8 jours produit un merge final ingérable
et masque qui a écrit quoi.

---

### D-08 — Pas de squash merge

**Décidé :** merge classique. Le squash est désactivé dans les réglages du dépôt.

**Motif :** le squash écrase les commits individuels sous un seul commit de merge.
L'onglet `Insights → Contributors`, qui est la preuve de contribution individuelle,
devient faux.

---

### D-09 ⟲ — `CODEOWNERS` : deux propriétaires par chemin

**Décidé :** chaque chemin liste **deux** handles.

**Révisé depuis :** un seul propriétaire par domaine, avec des identifiants
génériques (`@m1-andy`) jamais remplacés.

**Motif :** GitHub interdit d'approuver sa propre PR. Un chemin à propriétaire unique
rend toute PR de cette personne immergeable — la revue requise ne peut être fournie
par personne d'autre.

**Conséquence :** vérifier les handles réels avant le premier push. Un `CODEOWNERS`
avec un handle inexistant échoue en silence.

---

### D-10 — JaCoCo à cliquet : 50 → 60 → 80

**Décidé :** seuil bloquant relevé aux D3, D4 et D6. Il ne redescend jamais.

**Envisagé d'abord :** imposer 80 % dès le premier jour.

**Motif :** 80 % au D1 bloque toute PR avant qu'il n'existe du code à tester. À
l'inverse, un seuil posé seulement à la fin transforme le D6 en rattrapage collectif.

---

### D-11 — Quality Gate SonarQube sur le **code neuf** uniquement

**Décidé :** la porte de qualité s'applique aux lignes ajoutées ou modifiées.

**Motif :** une porte sur l'ensemble du code échoue en permanence sur un projet jeune
et finit par être désactivée. Une porte sur le code neuf reste crédible et tenable.

**Conséquence :** SonarLint en mode connecté est distribué à toute l'équipe dès le
D3. Sans lui, les problèmes se découvrent au D6, tous ensemble.

---

### D-12 ⟲ — Les entités sont testées et comptées dans la couverture

**Décidé :** `Cycle.peutAccueillirOperation()`, `Pret.soldeRestant()`,
`Pret.estEnRetard()`, `DemandePret.quorumAtteint()`, `Cotisation.estVerrouillee()`
ont chacun leur test.

**Révisé depuis :** exclure les entités du calcul JaCoCo, comme on le fait souvent
pour des POJO.

**Motif :** l'énoncé §11.1 cite explicitement « les entités métier » parmi ce qui doit
être testé. Nos entités portent du comportement, pas seulement des accesseurs.

---

## Infrastructure

### D-13 — Runner GitHub Actions auto-hébergé

**Décidé :** un runner installé sur `vm-devops-g1`.

**Envisagé d'abord :** les runners hébergés par GitHub.

**Motif :** les VM sont sur le LAN de l'université, derrière du NAT. Un runner
GitHub ne peut pas ouvrir de connexion SSH vers `192.168.0.50`. Aucun réglage ne
contourne cela.

**Conséquence :** c'est la dépendance la plus critique du projet. Si le runner tombe,
rien ne se déploie. Il est installé au D1, avant tout job de déploiement. Le token
d'enregistrement expire en 1 heure — Andy le génère à la demande de Klein.

**Cette décision fait l'objet d'un ADR dans le rapport.**

---

### D-14 ⟲ — Trois VM, pas quatre

**Décidé :** `vm-dev-g1` `.50` · `vm-prod-g1` `.51` · `vm-devops-g1` `.52`.
IDs Proxmox 301 / 302 / 303.

**Révisé depuis :** quatre VM — dev, prod, ci, monitoring — pour 20 Go de RAM et
200 Go de disque.

**Motif :** le serveur Proxmox est partagé avec trois autres groupes de la promotion.
La contrainte n'est pas la RAM mais le **disque** : un fichier qcow2 occupe sa place
même VM éteinte. Le CI et le monitoring tiennent ensemble sur une seule machine.

**Conséquence :** 11 Go de RAM, environ 50 Go de disque. Vérifié : les 3 VM tournent
simultanément dans 12,6 Go sur les 15,56 Go disponibles — la démo de soutenance passe.
Toute référence à `vm-ci` ou `vm-mon` dans un fichier est un résidu à corriger.

---

### D-15 — Clones liés depuis un template cloud-init

**Décidé :** un template Debian cloud-init, puis trois clones **liés**.

**Envisagé d'abord :** des clones complets.

**Motif :** un clone lié ne copie pas le disque de base. Sur un serveur partagé où le
disque est la ressource rare, c'est la seule option viable.

**Conséquence :** le template ne doit jamais être supprimé ni démarré. Et une
modification de cloud-init exige `qm stop` puis `qm start` — un `reboot` ne réapplique
pas la configuration.

---

### D-16 — Terraform écrit après coup, puis `terraform import`

**Décidé :** les VM sont créées à la main dans Proxmox au D1. Les fichiers Terraform
sont écrits au D7 et réconciliés avec `terraform import`.

**Envisagé d'abord :** provisionner les VM par Terraform dès le départ.

**Motif :** l'énoncé demande la preuve d'une infrastructure codifiée, pas la preuve
qu'elle a servi à créer les machines. Apprendre le provider `bpg/proxmox` au D1
aurait retardé le jalon bloquant du runner.

**Conséquence :** la preuve attendue est un `terraform plan` affichant
**`No changes`** — capture d'écran à mettre dans le rapport. Si le plan propose de
détruire quelque chose, l'import est incomplet : ne pas appliquer.

---

### D-17 — Debian 13 « trixie » / Proxmox VE 9

**Constaté :** le serveur tourne en PVE 9.1.1, pas en PVE 8.

**Conséquence :** les dépôts APT utilisent le format **deb822** (`.sources`), pas
l'ancien `.list`. Les procédures écrites pour PVE 8 échouent. Ne jamais rediriger la
sortie de ces commandes vers `/dev/null` — c'est ce qui masque l'échec.

---

### D-29 — Type de processeur Proxmox : `host`, pas `kvm64`

**Décidé :** les 3 VM tournent avec `--cpu host`. `qm set <id> --cpu host`, puis
`qm stop` et `qm start`.

**Constaté avant :** Proxmox crée les VM avec `kvm64` par défaut — un modèle de
processeur volontairement minimal, conçu pour que les VM puissent migrer vers
n'importe quel hôte. La VM voyait `QEMU Virtual CPU version 2.5+`.

**Symptôme :** la CI échouait sur `exit code 134`. Le rapport JVM donnait
`SIGSEGV` dans `J 1087 c1 java.lang.String.trim()` — du code **compilé par le JIT**,
pendant le démarrage de Maven, avant le moindre test. Ni mémoire, ni disque, ni
JaCoCo : `df -h` affichait 5,5 Go libres et `free -h` 4,6 Go.

**Motif :** le compilateur C1 génère du code machine optimisé d'après les
instructions qu'il détecte au démarrage. Sur un modèle de processeur aussi réduit,
les intrinsèques vectorisées du JDK produisent du code que le CPU émulé n'exécute
pas correctement.

**Vérification :** après bascule, `lscpu` affiche le vrai processeur
(Intel Core i7-8700, avec `avx`, `avx2`, `bmi2`) et trois `./mvnw -B test`
consécutifs passent.

**Conséquence :** `host` est de toute façon la recommandation sur un nœud unique —
3 à 10 % plus rapide, et la migration à chaud n'est pas dans notre périmètre.
`infra/terraform/main.tf` déclare déjà `cpu { type = "host" }` : le
`terraform plan` du D7 affichera `No changes` au lieu d'un écart à expliquer.

---

### D-30 — Base Trivy amorcée hors CI, analyse limitée aux paquets système

**Décidé :** la base de vulnérabilités Trivy est téléchargée **une fois** sur
`vm-devops-g1`, hors pipeline. Les jobs tournent avec `--skip-db-update` et
`--pkg-types os`. Procédure et rafraîchissement : `docs/RUNBOOK-TRIVY.md`.

**Constaté :** le job Trivy échouait sur
`failed to download vulnerability DB … context deadline exceeded`. Les journaux
donnent la mesure exacte : **106,79 Mio à 62 Ko/s**, soit environ **29 minutes**.
Le délai d'attente par défaut de Trivy est de 5 minutes. Le téléchargement n'était
pas lent par accident : il n'avait aucune chance d'aboutir.

C'est la **troisième** manifestation de la même contrainte physique, après
`actions/setup-java` et `docker/login-action` (voir ADR-002). Le réglage BBR a fait
passer la liaison de 16 à 62 Ko/s ; il n'en fera pas une liaison rapide.

**Motif :** la règle appliquée partout ailleurs sur ce runner — *une machine
persistante s'approvisionne une fois, elle ne se réapprovisionne pas à chaque job*.
Le JDK, Node et les actions suivent déjà ce principe. La base Trivy n'y échappe pas.
Un `--timeout 40m` aurait « marché » en ajoutant 40 minutes à chaque exécution
dont la base a plus de 24 h, ce qui n'est pas un pipeline.

**Limitation assumée — `--pkg-types os`.** Trivy analyse les paquets du système de
base (Alpine), pas les dépendances Java de `app.jar`. L'analyse des `.jar` exige la
`trivy-java-db`, environ 700 Mo, soit **plus de trois heures** sur cette liaison.
La couverture applicative reste donc partielle, et c'est dit tel quel dans le
rapport plutôt que masqué : la CVE d'une dépendance Maven ne serait pas détectée
par cette étape. `mvn dependency:tree` et le Quality Gate SonarQube couvrent
partiellement l'angle mort.

**Vérification :** `ls -lh /home/runner/.trivy-cache/trivy/db/trivy.db` avant de
relancer le pipeline. Le workflow échoue immédiatement, avec un message explicite,
si la base est absente — plutôt qu'au bout de sept minutes d'expiration.

---

### D-31 — Runner persistant, pas éphémère · l'isolation passe par un contrôle d'accès

**Décidé :** le runner reste **persistant**. Pas de `--ephemeral`, pas de runner
conteneurisé recréé à chaque job. L'isolation est obtenue par une règle d'accès :
*Settings → Actions → General → Fork pull request workflows from outside
collaborators → **Require approval for all outside collaborators***.

**Alternative examinée :** un runner éphémère se détruit après chaque job. C'est la
recommandation courante, et elle supprime effectivement toute pollution d'état entre
deux exécutions.

**Motif du rejet — la mesure, pas la préférence.** Tout ce qui rend notre pipeline
possible en 3 min 51 s vit sur une machine qui ne disparaît pas :

| Sur le runner persistant | Coût de réacquisition à chaque job |
|---|---|
| Base de vulnérabilités Trivy (1,2 Go) | ~29 min (D-30) |
| JDK 21 et Node 20 pré-installés | ~180 Mo |
| Cache de couches Docker, images de base | plusieurs minutes |
| `akiwacu-artifacts/` | perdu — les livrables du D7 |

À 60 Ko/s, l'éphémère ne rend pas le pipeline plus lent : il le rend impossible.
La recommandation suppose une liaison où reprovisionner est gratuit. Sur la nôtre,
ADR-002 et D-30 établissent le contraire, chiffres à l'appui.

**Ce que le rejet ne dit pas.** Le risque désigné est réel, mais ce n'est pas la
pollution d'état entre nos cinq jobs — c'est le dépôt **public** couplé à un runner
sur le réseau de l'université : un inconnu ouvre une PR depuis un fork et son code
s'exécute sur `vm-devops-g1`. C'est le scénario contre lequel GitHub met
explicitement en garde.

**Traité par le contrôle d'accès**, qui répond exactement à cette menace, pour un
réglage et zéro minute d'indisponibilité. Nos cinq membres partagent déjà la
machine : les isoler les uns des autres ne protège de rien.

**À noter :** le runner purge `_actions/` au début de chaque job — constaté au D2.
Une partie de l'isolation promise par l'éphémère est donc déjà acquise.

**Réexamen :** si le projet obtient une liaison décente, l'éphémère redevient le bon
choix. La décision dépend d'une mesure de débit, pas d'une doctrine.

---

## Applicatif## Applicatif

### D-18 — Client React + TypeScript, séparé de l'API

**Décidé :** Vite, React 18, TypeScript, Tailwind, shadcn/ui, TanStack Query.
Le client API est **généré** depuis la spécification OpenAPI.

**Envisagé d'abord :** un rendu serveur Thymeleaf, plus rapide à produire.

**Motif :** les tontines se tiennent en réunion physique, la saisie se fait au
téléphone. Le mobile-first et le mode hors-ligne partiel ne sont pas décoratifs ici.
Et une API consommée par un client séparé prouve que l'API est réellement REST.

**Conséquence :** interdiction d'écrire un `fetch` à la main — on régénère.
Le client a son propre `Dockerfile` et une `nginx.conf` avec un repli SPA
`try_files $uri $uri/ /index.html`. Sans ce repli, un rechargement de page sur
`/tontines` renvoie 404 en pleine démonstration.

**Cette décision fait l'objet d'un ADR dans le rapport.**

---

### D-19 — Bruno plutôt que Postman

**Décidé :** collection Bruno, fichiers `.bru` versionnés dans `bruno/`.

**Motif :** les fichiers `.bru` sont du texte : ils passent en revue de code et se
diffent. Une collection Postman est un JSON exporté à la main, souvent périmé.

**Conséquence :** l'énoncé §12.9 nomme explicitement Postman. Un export au format
Postman est produit au D7 pour satisfaire la lettre du livrable.

---

### D-20 — `tontineId` vient du JWT, jamais du corps de la requête

**Décidé :** `TenantContext` lit l'identifiant de tontine depuis les claims du jeton.

**Motif :** un `tontineId` transmis dans le corps est un contrôle d'accès délégué au
client. N'importe qui peut le modifier.

**Conséquence :** c'est la règle R1, et c'est la question la plus probable de la
soutenance. Test imposé : `shouldNotAccessDataFromAnotherTontine()`.

---

### D-25 — Intérêts : taux forfaitaire **mensuel**

**Décidé par Andy, chef de projet :**

```
montantDu = montantAccorde + (montantAccorde × tauxInteret / 100 × dureeMois)
```

`HALF_UP` à 2 décimales. `tauxInteret` vaut 0 par défaut.
*100 000 BIF à 10 % sur 3 mois → 130 000 BIF.*

**Envisagé :** un forfait appliqué une seule fois, indépendant de la durée — plus
simple, mais un prêt d'un mois et un prêt de six mois coûteraient la même chose.

**Motif :** c'est la pratique des associations d'épargne et de crédit. Un taux
mensuel simple sur des prêts courts, dont les intérêts alimentent le fonds
redistribué à la clôture du cycle. Le forfait unique ne correspond à rien de réel.

**Conséquence :** `dureeMois` est **recopié sur `Pret`** et non lu depuis
`DemandePret` — le calcul reste possible sans charger la demande, et l'entité est
testable sans base de données (voir D-12). `soldeRestant()` et `estEnRetard()` en
découlent.

---

### D-26 — Numérotation des reçus par séquence PostgreSQL

**Décidé :** séquence `seq_numero_recu`, format `REC-<année>-<6 chiffres>`.

**Envisagé d'abord :** un compteur calculé en Java.

**Motif :** deux trésoriers émettant un reçu à la même seconde obtiendraient le même
numéro. L'énoncé exige une numérotation séquentielle vérifiable.

**Conséquence :** le nom `seq_numero_recu` fait partie du contrat entre la migration
et le `RecuService` de Juste. Il ne se renomme pas.

---

### D-27 — « L'épargne du membre » = les cotisations du cycle en cours

**Décidé par Andy, chef de projet.** R6 plafonne un prêt à 3 × l'épargne. L'énoncé ne
définit jamais l'épargne.

```sql
SELECT COALESCE(SUM(montant), 0)
FROM cotisations
WHERE membre_id = :membreId AND cycle_id = :cycleIdCourant;
```

**Envisagé :** le cumul sur tous les cycles. Écarté — un cycle se clôture par une
redistribution, chacun récupère sa mise. Compter les cycles passés reviendrait à
prêter contre une épargne déjà rendue.

**Envisagé aussi :** épargne du cycle moins l'encours des prêts actifs, pour empêcher
d'empiler deux prêts au plafond. Écarté du calcul lui-même : les associations traitent
ce cas par une règle distincte — *un seul prêt actif à la fois* — plus lisible qu'un
plafond qui bouge.

**Motif :** cohérent avec R2 et R3, qui bornent toute opération au cycle. Une seule
requête, un seul test.

**Conséquence :** c'est la réponse à donner en soutenance, et elle sera demandée.

---

### D-28 — Plages de numéros de migration réservées par personne

**Décidé :** chaque membre dispose d'une plage `V<n>` qui lui est propre —
Andy `V3-V9`, Juste `V10-V19`, Benitha `V20-V29`, Gloria `V30-V39`, Klein `V40-V49`.

**Motif :** quatre personnes travaillant en parallèle créeraient chacune un `V3__…sql`.
Flyway refuse alors de démarrer, et il faut renuméroter à la main dans quatre branches
déjà poussées. Les plages suppriment le problème sans aucune coordination.

**Conséquence :** une migration **déjà mergée ne se modifie jamais** — Flyway conserve
une empreinte de chaque fichier appliqué. On ajoute une nouvelle migration.

---

## Pilotage## Pilotage

### D-21 — Scrum complet, trois sprints

**Décidé :** planning, daily, affinage, revue, rétrospective. Aucune cérémonie sautée.

**Motif :** l'énoncé note la gestion de projet. Et la pratique des rituels est un
objectif personnel du chef de projet.

**Conséquence :** la **vélocité est mesurée mais ne sert pas à planifier** — trois
sprints courts avec une équipe non rodée produisent du bruit. L'indicateur de
pilotage est le **cycle time** : moins de 24 h sain, plus de 48 h problème.
Savoir expliquer cette distinction vaut mieux qu'afficher un burndown confiant.

---

### D-22 ⟲ — Import Jira : tout au backlog, aucun sprint dans le CSV

**Décidé :** le CSV contient `Issue Type · Summary · Description · Assignee ·
Story Points · Labels · Priority`. 93 stories, dans l'ordre d'exécution. Les sprints
sont créés et datés à la main ; le regroupement thématique passe par les labels.

**Révisé depuis :** un CSV de 18 epics et 110 stories portant des colonnes
`Epic Name`, `Parent Summary` et `Sprint`.

**Motif :** dans un projet Jira **team-managed**, le lien parent-enfant ne s'importe
pas et une valeur de sprint crée un sprint fantôme — sans nom, sans dates, en double.
C'est le résultat observé au premier import.

**Conséquence :** les dates de sprint ne s'importent jamais par CSV, et sans dates il
n'y a pas de burndown. Un import bancal se nettoie avant d'en refaire un : supprimer
les sprints fantômes, supprimer les tickets, vider les epics résiduels.

---

### D-23 — Confluence pour la prose, Git pour ce qui suit le code

**Décidé :** le rapport de 15-25 pages s'écrit sur Confluence. `CLAUDE.md`, les ADR,
les fiches de rôle et les runbooks vivent dans le dépôt.

**Motif :** cinq personnes écrivant de la prose en parallèle dans Git produisent des
conflits de merge sur des paragraphes, les deux derniers jours du projet.

---

### D-24 — Une PR socle bloquante au D1

**Décidé :** les 13 entités JPA et les migrations Flyway arrivent dans une seule PR,
mergée le premier jour.

**Motif :** tant que le schéma n'est pas figé, quatre personnes attendent ou divergent.

**Conséquence :** c'est le premier livrable d'Andy, avant toute tâche de pilotage
autre que la création du dépôt.

---

## Ce qui a été délibérément écarté

| Écarté | Motif |
|---|---|
| Kubernetes | Docker Compose suffit pour deux environnements. L'énoncé ne le demande pas. |
| Tests end-to-end Playwright | Le temps va à la couverture unitaire, qui est notée. |
| Cache Redis | Aucun problème de charge à résoudre sur ce périmètre. |
| Microservices | 13 entités fortement liées. Un monolithe modulaire est le bon choix et se défend. |
| Filtres `paths:` sur les workflows | Bloque définitivement les PR sur branche protégée. Voir D-03. |
| Squash merge | Détruit la preuve de contribution individuelle. Voir D-08. |
| Maquettes Figma complètes | 45 minutes sur un seul écran. Le reste utilise shadcn/ui tel quel. |
