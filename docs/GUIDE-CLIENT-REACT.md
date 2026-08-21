# GUIDE CLIENT REACT — de la direction visuelle aux écrans

Le client **est** planifié. Onze stories `frontend`, 54 points, déjà réparties dans
`02-BACKLOG-JIRA.csv` et datées D5. Rien n'est à réinventer ; ce guide dit dans quel
ordre les faire et avec quels prompts.

---

## 1. Ce que le backlog prévoit déjà

| Qui | Stories `frontend` | Pts |
|---|---|---|
| **Andy** | initialiser React/Vite/TS/Tailwind/shadcn · générer le client API · écrans login et guards · écrans utilisateurs et membres | **18** |
| **Benitha** | **saisie rapide des cotisations (écran phare, 8 pts)** · écrans de gestion des cycles | **13** |
| **Gloria** | demande de prêt et vote commissaire · détail du prêt et échéancier | **10** |
| **Juste** | tontine et adhésion · téléchargement et aperçu des reçus | **8** |
| **Klein** | tableau de bord et caisse | **5** |

## 2. Une seule personne, ou cinq ?

**Les deux, dans cet ordre.** C'est exactement le schéma de la PR socle.

**Toi seul sur le socle client** — initialisation, thème, routage, authentification,
client API généré. Tant qu'il n'existe pas, personne ne peut écrire un écran sans
inventer sa propre façon d'appeler l'API, et on se retrouve avec cinq conventions.
Compte une demi-journée. C'est bloquant, donc c'est solo et c'est prioritaire.

**Ensuite chacun ses écrans.** Trois raisons de ne pas tout garder :

1. La Definition of Done exige, pour chaque story, un **écran client qui consomme
   réellement l'endpoint**. Une story sans écran n'est pas terminée — donc chacun a
   de toute façon un écran à livrer.
2. Chacun connaît ses propres endpoints, ses champs, ses codes d'erreur. Personne
   n'écrira l'écran de vote commissaire plus vite que Gloria.
3. Onze écrans sur une personne, c'est le chemin critique du projet entier qui passe
   par toi. Sur cinq, c'est deux écrans chacun.

Le découpage par écran suit exactement le découpage par package qu'ils ont déjà :
aucun conflit de fichiers, aucune règle nouvelle à apprendre.

---

## 3. La direction visuelle — à trancher AVANT la première ligne de code

Ne demande pas « fais-moi une belle interface ». Décide d'abord, en te fondant sur
ADR-001 : l'écran phare est un trésorier qui saisit **40 cotisations d'affilée, à une
main, debout, en réunion**, sur un Android d'entrée de gamme, avec un réseau instable.

Ce n'est pas un tableau de bord d'analyste. C'est un **registre**.

### Trois directions explorées, une choisie

Pas trois descriptions en l'air : trois maquettes réelles et comparables (écran de
saisie + tableau de bord président), explorées ensemble le 20 août 2026 —
[canvas de la direction visuelle](https://claude.ai/code/artifact/2e81e057-b8b6-406d-8f72-aec38a588282).

| | Direction | L'idée |
|---|---|---|
| A | Le Registre | La liste est le document — rangées réglées, montants tabulaires, saisie en feuille au bas de l'écran |
| B | Le Guichet | Une carte par membre ; toucher une carte l'ouvre sur place pour saisir |
| **C** | **Le Compteur** — **retenue** | Un membre à la fois, plein écran, total qui grimpe en direct en bandeau fixe |

**Direction C retenue.** Elle colle le mieux au cas d'usage de l'ADR-001 : un
trésorier qui compte à voix haute pendant que les cotisations rentrent, pas
quelqu'un qui parcourt une liste. Le bandeau de total fixe en haut donne à tout le
monde dans la salle le même repère visuel que le décompte oral. Voir
`docs/DECISIONS.md` D-34.

### Jeton de design — Direction C, à centraliser dans `index.css` / le thème Tailwind

- **Polices** (Google Fonts) : `Space Grotesk` (titres, montants, boutons) ·
  `IBM Plex Sans` (corps de texte)
- **Bandeau d'en-tête / fond sombre** : `oklch(0.2 0.015 70)` texte
  `oklch(0.96 0.01 80)`
- **Accent (montants, boutons d'action)** : `oklch(0.75 0.15 85)` — ambre
- **Fond clair (corps de page)** : `oklch(0.97 0.005 90)` texte
  `oklch(0.2 0.01 90)`
- **Chiffres** : `font-variant-numeric: tabular-nums` partout où un montant
  s'affiche
- **Rayon des boutons/cartes** : 8–10 px · **cibles tactiles** : ≥ 48 px

Ces valeurs viennent des maquettes du canvas — les copier telles quelles en
variables CSS/thème Tailwind (P1), ne pas en réinventer de nouvelles.

### Les contraintes non négociables, quelle que soit la direction

- **Français** partout dans l'interface — c'est la langue du projet.
- **Montants en BIF, sans décimale**, séparateur d'espace : `1 250 000 BIF`. Le franc
  burundais n'a pas de sous-unité utilisée ; afficher `1250000.00` est faux.
- **Chiffres tabulaires** (`font-variant-numeric: tabular-nums`) dans toute colonne de
  montants, sinon les totaux ne s'alignent pas.
- **Cibles tactiles ≥ 48 px** sur tout ce qui se tape en réunion.
- **Mobile d'abord**, `md:` ensuite pour l'administration au portable.
- **Aucun `fetch` écrit à la main** — tout passe par le client généré depuis OpenAPI.
  C'est une story du backlog, et c'est ce qui garantit que le client suit l'API.

---

## 4. Les prompts, dans l'ordre

Ouvre Claude Code **à la racine du dépôt**, pas dans `client/`. Chaque prompt part du
principe que le précédent est terminé et mergé.

### P0 — cadrer, sans écrire de code

> Lis `docs/ADR-001-CLIENT-WEB-PWA.md`, `docs/MODELE-DE-DONNEES.md` et
> `docs/GUIDE-CLIENT-REACT.md`.
>
> Ne écris aucun code pour l'instant. Propose-moi une **direction visuelle** pour un
> client React destiné à un trésorier qui saisit 40 cotisations d'affilée, à une main,
> en réunion, sur un Android d'entrée de gamme.
>
> Donne-moi : une palette (une couleur d'accent, une échelle de gris, deux couleurs
> sémantiques), une échelle typographique, une échelle d'espacement, et les tailles de
> cibles tactiles. Justifie chaque choix par le cas d'usage, pas par l'esthétique.
>
> Termine par les trois écrans que tu considères comme les plus risqués et pourquoi.

*Tu lis, tu ajustes, tu valides. Ensuite seulement, P1.*

### P1 — le socle client

> Initialise le client dans `client/` : Vite + React 18 + TypeScript + Tailwind +
> shadcn/ui, mobile-first.
>
> Applique la direction visuelle validée en la **centralisant** : variables CSS dans
> `index.css` et thème Tailwind. Aucune couleur ni taille en dur dans un composant.
>
> Mets en place : le routage React Router, un layout mobile avec navigation basse et
> un layout desktop avec barre latérale, et un composant `Montant` qui formate en BIF
> sans décimale avec chiffres tabulaires.
>
> Contraintes : `client/Dockerfile` et `client/nginx.conf` existent déjà, ne les
> réécris pas. Pas de `localStorage` pour autre chose que le jeton.
>
> Propose-moi un plan en 5 lignes. Je valide avant que tu écrives du code.

### P2 — le client API généré

> Ajoute un script `pnpm gen:api` qui génère un client TypeScript typé depuis
> `http://localhost:8080/v3/api-docs`, avec les hooks TanStack Query.
>
> Le code généré va dans `client/src/api/generated/` et n'est **jamais** modifié à la
> main. Documente la commande dans `client/README.md`.
>
> Montre-moi ensuite un exemple d'appel typé, pour que je vérifie que les types du
> backend arrivent bien jusqu'au composant.

### P3 — authentification et garde de routes

> Implémente l'écran de connexion et la protection des routes par rôle
> (`TRESORIER`, `MEMBRE`, `COMMISSAIRE`, `GESTIONNAIRE`, `PRESIDENT`).
>
> Le jeton JWT est stocké et rattaché à chaque requête. Une 401 renvoie à la connexion.
> Le `tontineId` vient **du jeton**, jamais d'un champ de formulaire — voir R1.
>
> Écrans d'erreur en français, jamais de message technique brut.

### P4 — l'écran de référence

Fais **la saisie rapide des cotisations** avant les autres, même si elle appartient à
Benitha : c'est le plus dur, et il fixe le motif que les dix autres copieront.

> Implémente l'écran de saisie rapide des cotisations. Contexte réel : le trésorier a
> 40 membres à saisir en réunion, à une main, debout.
>
> Exigences : liste des membres du cycle en cours, montant saisi en un geste, total
> mis à jour en direct, confirmation immédiate, et **aucune perte de saisie si le
> réseau tombe** — mise en file et renvoi.
>
> Traite explicitement les quatre états : chargement, liste vide, erreur réseau,
> échec de validation métier (HTTP 409). Montre-moi chacun.

### P5 — les dix autres écrans, un prompt par personne

> Lis `docs/GUIDE-CLIENT-REACT.md` et regarde
> `client/src/features/cotisation/` — c'est le motif de référence, suis-le.
>
> Implémente \<mes écrans\> pour le ticket **AKW-\<numéro\>**.
>
> Contraintes : uniquement mes fichiers sous `client/src/features/<mon-domaine>/` ;
> appels d'API par le client généré, jamais de `fetch` ; les quatre états obligatoires ;
> aucune couleur ni taille en dur.

### P6 — les états, une passe transversale

> Passe en revue tous les écrans et vérifie que chacun traite : chargement, vide,
> erreur réseau, erreur métier, et absence de droits.
>
> Liste-moi les écrans non conformes avant de corriger quoi que ce soit.

### P7 — PWA et mise en service

> Ajoute le manifeste PWA et un service worker : coquille applicative en cache,
> installable, écran hors-ligne explicite.
>
> Vérifie que `docker build ./client` passe et que nginx sert bien le repli SPA.

---

## 5. Activer la chaîne

Le job client est derrière un interrupteur, à ne lever qu'une fois `client/` réellement
buildable — sinon le pipeline échoue sur `pnpm install` :

```bash
gh variable set CLIENT_ENABLED --body true
```

---

## 6. Ce qui se dit à la soutenance

> Le client est responsive et non natif parce que le terrain est mobile et
> l'administration est au bureau — ADR-001. La direction visuelle (« Le Compteur »)
> découle du même cas d'usage : un trésorier qui compte à voix haute en réunion, pas
> quelqu'un qui parcourt une liste — d'où un membre à la fois, plein écran, et un
> total qui grimpe en direct dans un bandeau fixe. Trois directions ont été maquettées
> et comparées avant de trancher (D-34), pas choisies au goût. Le client TypeScript
> est **généré depuis la spécification OpenAPI** : aucun appel écrit à la main, et un
> changement de contrat côté API casse la compilation du client plutôt que de casser
> l'application en démonstration.
