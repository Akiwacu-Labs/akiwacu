# GUIDE JIRA & SCRUM — pas à pas

> Objectif : que personne ne se demande jamais « qu'est-ce que je fais maintenant ? ».
> Le tableau Jira répond à cette question à toute heure du jour.

---

## Partie A — Ce que tu dois savoir en tant que membre

### A.1 Se connecter la première fois

1. Tu reçois un mail d'invitation Atlassian. Clique, crée ton compte (mets ton **vrai nom**, c'est ce que le correcteur verra).
2. Va sur `https://<notre-site>.atlassian.net/jira/software/projects/AKW/boards/1`
3. Mets le tableau en favori. Tu l'ouvres chaque matin avant le daily.

### A.2 Comprendre le tableau

Cinq colonnes, dans cet ordre :

| Colonne | Ce que ça veut dire | Limite |
|---|---|---|
| **Backlog** | Pas encore engagé dans un sprint | — |
| **To Do** | Engagé dans le sprint en cours, personne dessus | — |
| **In Progress** | Quelqu'un code dessus **maintenant** | **max 2 par personne** |
| **In Review** | PR ouverte, en attente de revue et de CI | max 3 par personne |
| **Done** | Mergé dans `develop` **et vérifié sur vm-dev-g1** | — |

> **La limite de 2 en « In Progress » est la règle la plus importante du tableau.**
> Trois tickets ouverts en même temps veut dire zéro ticket fini. Termine avant de commencer.

### A.3 La boucle quotidienne — 6 gestes

**1. Prendre un ticket**
Ouvre le tableau → colonne **To Do** → choisis un ticket de **ton domaine** →
clic sur `Assignee` → mets-toi → glisse-le en **In Progress**.

> Ne prends jamais un ticket du domaine de quelqu'un d'autre sans le dire au daily.

**2. Lire les critères d'acceptation**
Ils sont dans la description. S'ils ne sont pas clairs, **commente le ticket** et
mentionne Andy avec `@` — ne devine pas.

**3. Créer ta branche avec la clé du ticket**
```bash
git checkout develop && git pull origin develop
git checkout -b feat/cotisation-saisie-rapide
```

**4. Lier le ticket à ta PR**
Dans la description de la PR, écris la clé Jira :
```
Closes AKW-42
```
Mentionne aussi la clé dans au moins un commit :
```bash
git commit -m "feat(cotisation): ajouter la saisie rapide multi-membres (AKW-42)"
```
Jira relie alors automatiquement la branche, les commits et la PR au ticket. Le
correcteur voit la traçabilité complète : ticket → branche → commits → PR → merge.

**5. Passer en In Review**
Dès que la PR est ouverte, glisse le ticket en **In Review** et laisse un commentaire :
`PR #17 ouverte, en attente de revue.`

**6. Passer en Done**
Seulement quand : PR mergée **ET** endpoint vérifié en direct sur vm-dev-g1.
Commente ce que tu as vérifié : `Mergé. Testé sur vm-dev-g1 : POST /api/cotisations/batch renvoie 201.`

### A.4 Si tu es bloqué

1. Glisse le ticket **In Progress** → ajoute le label `blocked`
2. Commente : ce que tu as essayé, l'erreur exacte, ce dont tu as besoin
3. Mentionne la personne concernée avec `@`
4. **Écris aussi dans le groupe.** Un commentaire Jira que personne ne lit ne débloque personne.

> Seuil : **30 minutes.** Au-delà, tu écris. Sur 8 jours, une demi-journée perdue en
> silence, c'est 6 % du projet.

---

## Partie B — Les rituels Scrum, concrètement

### B.1 Sprint planning — 30 minutes, D1 / D4 / D7

**Avant la réunion (Andy) :** le backlog du sprint est prêt, priorisé, avec des critères d'acceptation.

**Pendant :**
1. Andy annonce **l'objectif du sprint** en une phrase (5 min)
   *Sprint 1 : « à la fin, un push sur develop se déploie tout seul sur vm-dev-g1 »*
2. On parcourt les tickets. Chacun prend les siens et les glisse dans le sprint (15 min)
3. Estimation rapide (10 min) — voir B.2
4. Chacun dit à voix haute **son engagement** : *« je livre X, Y et Z d'ici D3 »*

**Règle de discipline :** au-delà de **2 minutes de débat sur un ticket**, on l'étiquette
`à-affiner` et on passe. On y revient à l'affinage.

### B.2 Estimation — suite de Fibonacci

| Points | Ça veut dire | Repère |
|---|---|---|
| **1** | Trivial | Ajouter un champ, corriger un libellé |
| **2** | Simple, connu | Un endpoint GET avec son test |
| **3** | Normal | Un CRUD complet avec ses tests |
| **5** | Complexe | Une règle métier avec ses cas limites |
| **8** | Gros | Le service de génération de reçus PDF |
| **13** | **Trop gros** | À découper obligatoirement |

**Comment on estime :** chacun annonce un chiffre en même temps. Si tout le monde est
d'accord, on note et on avance. Si l'écart est grand (2 contre 8), **la personne la
plus basse et la plus haute expliquent en 30 secondes chacune**, puis on revote une
seule fois. Pas de troisième tour.

> **Ce qu'on estime, c'est la complexité, pas les heures.** Un ticket à 5 points n'est
> pas « 5 heures ». C'est « nettement plus dur qu'un 3 ».

### B.3 Daily stand-up — 15 minutes, tous les jours, heure fixe

Debout. Minuté. Tableau Jira affiché à l'écran.

Chacun, dans l'ordre du tableau, répond à trois questions :

1. **Qu'est-ce que j'ai livré hier ?** (livré = mergé, pas « j'ai travaillé sur »)
2. **Qu'est-ce que je livre ce jour ?**
3. **Qu'est-ce qui me bloque ?**

**Interdit pendant le daily :** résoudre un problème technique. On note « Andy + Gloria
après le daily » et on continue. Sinon le daily dure 45 minutes et devient inutile.

**Ce qu'Andy surveille pendant qu'on parle :**
- quelqu'un qui dit « toujours sur le même ticket » deux jours de suite
- un ticket en **In Progress** depuis plus de 24 h
- quelqu'un avec plus de 2 tickets en cours
- une PR qui attend une revue depuis plus de 3 h

### B.4 Affinage du backlog — 15 minutes, D2 et D5

On regarde les tickets du sprint suivant. On découpe tout ce qui dépasse 8 points.
On écrit les critères d'acceptation manquants. C'est tout.

### B.5 Revue de sprint / démo — 30 minutes, D3 et D6

**Chacun démontre son domaine en direct sur vm-dev-g1. Pas sur son portable.**

Format, 5 minutes par personne :
1. Le ticket que j'ai livré
2. La démonstration en direct (Swagger ou écran client)
3. Ce qui ne marche pas encore et pourquoi

> C'est la répétition générale de ta soutenance. Prends-la au sérieux les deux fois :
> tu auras déjà présenté deux fois quand viendra le vrai jour.

### B.6 Rétrospective — 15 minutes, juste après la démo

Trois colonnes, trois post-its chacun maximum :

| 🛑 Stop | ✅ Continue | 🚀 Start |
|---|---|---|
| Ce qui nous ralentit | Ce qui marche | Ce qu'on essaie au prochain sprint |

On vote sur **une seule action** à mettre en place. Une. Pas cinq. Andy en fait un
ticket Jira avec un responsable, sinon ça n'arrive jamais.

---

## Partie C — Ce qu'Andy configure (une seule fois, D1)

### C.1 Créer le site et le projet

1. `https://www.atlassian.com/software/jira/free` → créer un site
   → nom du site : `akiwacu-labs` (ou autre)
2. Type de projet : **Scrum**, template **Software development**
3. Nom : `Akiwacu — Plateforme Tontines`, clé : **`AKW`**
4. Inviter les 5 membres + `@cincotech` (Jira Free autorise **jusqu'à 10 utilisateurs** — 6 rentre largement)

### C.2 Le principe : tout arrive au backlog, rien n'arrive dans un sprint

C'est la pratique Scrum standard et c'est aussi ce que Jira sait faire de manière fiable.

| Ce qui s'importe bien par CSV | Ce qui ne s'importe pas |
|---|---|
| Type, résumé, description, assigné, points, labels, priorité | Les epics et le lien parent-enfant *(projet team-managed)* |
| | Les sprints — un nom de sprint dans le CSV crée un sprint fantôme, sans nom ni dates |

Donc : **le CSV ne contient ni colonne `Sprint` ni colonne d'epic.** Les 93 stories
tombent toutes dans le backlog, dans l'ordre d'exécution. Les sprints se remplissent
en 10 minutes au sprint planning, en glissant depuis le haut du backlog. C'est
exactement le geste que le sprint planning est censé produire.

Le regroupement thématique est porté par les **labels** — `socle` `backend` `frontend`
`regle-metier` `devops` `test` `doc`, plus un label de jour `D1`…`D8`. Un filtre de
tableau par label donne la même lecture qu'un epic, sans le champ qui ne s'importe pas.

### C.2 bis Si un import bancal est déjà en place — nettoyer d'abord

Symptômes : des sprints en double, sans nom ni dates, dans le désordre.

**1. Supprimer les sprints fantômes**
`Backlog` → sur chaque sprint vide ou non daté `···` → `Supprimer le sprint`.
Les tickets qu'il contient retombent au backlog, ils ne sont pas perdus.

**2. Supprimer les tickets importés**
`Backlog` ou vue `Work items` → filtrer sur `Created` = aujourd'hui →
tout sélectionner → `Actions groupées` → `Supprimer`.

**3. Vider les epics résiduels**
Ils apparaissent dans le panneau `Epic` à gauche du backlog. Supprimer un epic ne
supprime pas ses enfants.

Le projet doit se retrouver avec un backlog vide et zéro sprint avant de réimporter.

### C.2 ter Importer

`Paramètres du projet` → `Importer` → CSV → charger `02-BACKLOG-JIRA.csv`.

| Colonne CSV | Champ Jira | Inclure |
|---|---|---|
| `Issue Type` | Work Type | ✅ |
| `Summary` | Summary | ✅ |
| `Description` | Description | ✅ |
| `Assignee` | Assignee | ✅ |
| `Story Points` | Story point estimate | ✅ |
| `Labels` | Labels | ✅ |
| `Priority` | Priority | ✅ |

À l'étape **« Map values »**, associer chaque prénom du CSV — `Andy` `Klein` `Juste`
`Benitha` `Gloria` — au compte Atlassian correspondant. Une fois par personne.

**Résultat attendu :** 93 stories au backlog, dans l'ordre d'exécution, zéro sprint.

### C.2 quater Créer les 3 sprints à la main

`Backlog` → bouton `Créer un sprint`, trois fois. Puis sur chacun `···` →
`Modifier le sprint` :

| Nom | Début | Fin | Objectif du sprint |
|---|---|---|---|
| `Sprint 1 — Socle et pipeline` | jeu. 6 août | sam. 8 août | Un push sur `develop` se déploie tout seul sur vm-dev-g1 |
| `Sprint 2 — Règles métier et client` | dim. 9 août | mar. 11 août | Couverture ≥ 80 %, Quality Gate vert, vm-prod-g1 en ligne |
| `Sprint 3 — Gel et rapport` | mer. 12 août | jeu. 13 août | 13 livrables déposés, démos répétées |

**Les dates ne s'importent jamais par CSV.** Sans dates, pas de burndown — donc pas
d'indicateur à montrer à la soutenance.

Au sprint planning du D1, glisser dans Sprint 1 tout ce qui porte les labels `D1`,
`D2`, `D3`. Idem au D4 et au D7. Puis **démarrer le Sprint 1** : un sprint non démarré
n'alimente ni le tableau ni le burndown.

### C.2 quinquies Le backlog reste-t-il utile sur 8 jours ?

Oui, et c'est le seul artefact Scrum qui existe en permanence — les sprints, eux, vont
et viennent. Trois raisons concrètes :

- Le sprint planning **tire depuis le backlog**. Sans backlog, il n'y a rien à tirer et
  la cérémonie devient une réunion d'improvisation.
- Ce qui n'entre pas dans Sprint 1 doit rester **visible** plutôt que disparaître. Un
  périmètre différé et assumé se défend ; un périmètre oublié se remarque.
- À la soutenance, un backlog ordonné démontre la priorisation. Un tableau où tout est
  déjà rangé dans des sprints ne démontre rien : on ne voit pas les arbitrages.

Ce qui **n'est pas** utile sur 8 jours : un backlog de fonctionnalités « plus tard »
qu'on sait ne jamais faire. Nos 93 stories sont toutes destinées à être livrées d'ici
le D8. Le backlog est une file d'attente, pas un cimetière.

### C.4 Connecter GitHub à Jira

`Applications` → `Rechercher des applications` → **GitHub for Jira** → installer →
autoriser l'organisation.

Une fois branché, chaque ticket affiche ses branches, commits, PR et déploiements.
**C'est ce qui rend ta traçabilité visible au correcteur en un clic.**

### C.5 Créer Confluence et le lier

Même site Atlassian → activer Confluence (gratuit, mêmes 10 utilisateurs).
Créer l'espace `Akiwacu` avec ces pages :

```
Akiwacu
├── Rapport technique          ← les 15-25 pages, rédigées à 5 en parallèle
│   ├── 1. Contexte et objectifs            (Andy)
│   ├── 2. Analyse des besoins              (Andy)
│   ├── 3. Conception de la base            (Juste)
│   ├── 4. Architecture logicielle + ADR    (Andy)
│   ├── 5. Règles métier                    (Gloria)
│   ├── 6. Application cliente              (Benitha)
│   ├── 7. Tests et couverture              (Klein)
│   ├── 8. SonarQube et Trivy               (Klein)
│   ├── 9. CI/CD et déploiements            (Klein)
│   └── 10. Perspectives                    (Andy)
├── Journal des décisions (ADR)
├── Comptes rendus de daily
└── Rétrospectives
```

> **Pourquoi Confluence et pas le dépôt Git pour le rapport :** cinq personnes qui
> écrivent de la prose en parallèle dans Git, ce sont des conflits de merge sur des
> paragraphes. Confluence gère l'édition simultanée et se lie nativement aux tickets Jira.
> Le code et les docs techniques restent dans Git ; la prose collaborative va dans Confluence.

---

## Partie D — Les indicateurs que suit Andy

### D.1 Burndown

`Rapports` → `Burndown Chart`. À regarder chaque matin avant le daily.
Si la courbe réelle est au-dessus de l'idéale deux jours de suite → on retire du périmètre.
**On ne rattrape pas en travaillant plus. On rattrape en enlevant.**

### D.2 Vélocité — mesurée, mais pas utilisée pour prédire

On mesure la vélocité de chaque sprint parce que c'est la pratique professionnelle.

**Mais on ne s'en sert pas pour planifier le sprint suivant**, et il faut savoir dire
pourquoi : la vélocité devient prédictive après trois à cinq sprints stables, avec une
équipe rodée. Nous avons trois sprints très courts et une équipe qui n'a jamais
travaillé ensemble. Nos chiffres seront du bruit, pas un signal.

> C'est exactement ce qu'il faut dire à la soutenance. Présenter un burndown confiant
> sur trois micro-sprints, c'est afficher le processus. Expliquer pourquoi on ne s'y
> fie pas, c'est le comprendre.

### D.3 Cycle time — notre vrai indicateur

Combien de temps un ticket reste entre « In Progress » et « Done ».

- **< 24 h** → sain
- **24–48 h** → à surveiller, le ticket était sans doute trop gros
- **> 48 h** → problème, et ce n'est presque jamais le code

Cet indicateur ne dépend d'aucune calibration d'estimation. Il fonctionne dès le
premier jour. C'est pour ça qu'on le préfère.

### D.4 Contrôle individuel — D3, D6, D8

Andy vérifie, pour chacun :
- GitHub → `Insights` → `Contributors` : commits, lignes ajoutées
- Jira → filtre `assignee = X AND status = Done`
- PR relues : `gh pr list --search "reviewed-by:@handle"`

> Un membre invisible dans l'historique est un membre non noté. Ce n'est pas de la
> surveillance, c'est de la protection : mieux vaut le voir au D3 qu'à la soutenance.

---

## Annexe — Modèle de ticket

```
Titre    : feat(cotisation) — saisie rapide multi-membres
Type     : Story
Assignee : Benitha Gahimbare
Points   : 5
Sprint   : Sprint 2 — Règles & Client
Labels   : backend, regle-metier

Description
-----------
En tant que trésorier, je veux enregistrer plusieurs cotisations en une seule
opération, afin de saisir rapidement une réunion de 40 membres.

Critères d'acceptation
----------------------
- [ ] POST /api/cotisations/batch accepte une liste de {membreId, montant}
- [ ] R2 : rejette avec 409 si le cycle n'est pas actif
- [ ] R5 : enregistre le trésorier validateur sur chaque cotisation
- [ ] Un reçu PDF est généré par cotisation
- [ ] Tests : nominal + cycle gelé + membre inexistant
- [ ] Endpoint annoté OpenAPI
- [ ] Écran client qui le consomme
```
