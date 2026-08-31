# GUIDE CLIENT REACT — de la direction visuelle aux écrans

Onze écrans `frontend` sont attendus côté client. Ce guide dit qui fait quoi,
dans quel ordre, et avec quels prompts — mis à jour au fil de l'avancement réel,
pas au calendrier D1–D8 d'origine (abandonné, voir `docs/DECISIONS.md`). L'état
« Fait / À faire » ci-dessous reflète le dépôt et Jira tels qu'ils sont
aujourd'hui, pas une prévision.

Pas de personne dédiée au client : chaque propriétaire de domaine construit les
écrans qui consomment ses propres endpoints, sur son propre ticket Jira, dans son
propre GitHub issue de suivi (#43 à #47). C'est le même découpage que le backend,
pour la même raison : l'évaluation est individuelle, et personne n'écrit l'écran
de vote commissaire plus vite que Gloria.

---

## 1. Qui fait quoi

| Qui | Écrans | Ticket Jira | Statut | Issue de suivi |
|---|---|---|---|---|
| **Andy** | socle (init, routage, thème) | AKW-247 | ✅ Fait — PR #13 | — |
| **Andy** | client API généré (`pnpm gen:api`) | AKW-248 | À faire | [#43](https://github.com/Akiwacu-Labs/akiwacu/issues/43) |
| **Andy** | login et guards par rôle | AKW-281 | À faire | [#43](https://github.com/Akiwacu-Labs/akiwacu/issues/43) |
| **Andy** | écrans utilisateurs et membres | AKW-282 | À faire | [#43](https://github.com/Akiwacu-Labs/akiwacu/issues/43) |
| **Benitha** | **saisie rapide des cotisations — « Le Compteur », écran de référence** | AKW-285 | À faire | [#45](https://github.com/Akiwacu-Labs/akiwacu/issues/45) |
| **Benitha** | écrans de gestion des cycles | AKW-286 | À faire | [#45](https://github.com/Akiwacu-Labs/akiwacu/issues/45) |
| **Gloria** | demande de prêt et vote commissaire | AKW-287 | À faire | [#46](https://github.com/Akiwacu-Labs/akiwacu/issues/46) |
| **Gloria** | détail du prêt et échéancier | AKW-288 | À faire | [#46](https://github.com/Akiwacu-Labs/akiwacu/issues/46) |
| **Juste** | tontine et adhésion | AKW-283 | À faire | [#44](https://github.com/Akiwacu-Labs/akiwacu/issues/44) |
| **Juste** | téléchargement et aperçu des reçus | AKW-284 | À faire | [#44](https://github.com/Akiwacu-Labs/akiwacu/issues/44) |
| **Klein** | tableau de bord et caisse | AKW-291 | À faire | [#47](https://github.com/Akiwacu-Labs/akiwacu/issues/47) |

Le socle (AKW-247) est mergé depuis le 20 août : layout, thème Tailwind, routeur,
composant `Montant`. Ce qui reste dessus, ce n'est pas de l'initialisation — c'est
le client API généré (jamais lancé) et l'authentification, tous les deux encore à
faire. Voir la section 4 pour le détail.

## 2. Une PR socle, puis dix contributions d'écrans

Le client suit le même principe que la PR socle backend : un socle commun avant
les contributions parallèles. C'est déjà arrivé — voir AKW-247 ci-dessus.

**Ensuite, chaque propriétaire livre ses écrans.** Trois raisons de ne pas tout
garder dans une seule contribution :

1. La Definition of Done exige, pour chaque story, un **écran client qui consomme
   réellement l'endpoint**. Une story sans écran n'est pas terminée — donc chacun a
   de toute façon un écran à livrer.
2. Chacun connaît ses propres endpoints, ses champs, ses codes d'erreur. Personne
   n'écrira l'écran de vote commissaire plus vite que Gloria.
3. Dix écrans sur une seule contribution créeraient un chemin critique pour tout
   le projet. Répartis entre cinq personnes, cela représente un ou deux écrans
   chacun.

Le découpage par écran suit exactement le découpage par package qu'ils ont déjà :
aucun conflit de fichiers, aucune règle nouvelle à apprendre.

**Un seul vrai ordre de dépendance** : le socle (fait) → le client API généré et
l'authentification (Andy, section 4) → l'écran de référence, la saisie de
cotisations (Benitha, c'est le motif que les autres suivent) → les huit écrans
restants, chacun sur son propre ticket, en parallèle. Rien n'oblige à attendre
que Juste ait fini pour que Klein commence — seul l'écran de référence de Benitha
est un vrai prérequis pour les autres.

---

## 3. La direction visuelle — déjà tranchée, ne pas en choisir une autre

**Décidée le 20 août 2026 (D-34). Ne relance pas ce choix.** L'écran phare est un
trésorier qui saisit **40 cotisations d'affilée, à une main, debout, en réunion**,
sur un Android d'entrée de gamme, avec un réseau instable — voir ADR-001. Ce n'est
pas un tableau de bord d'analyste. C'est un **registre**.

### Trois directions explorées, une retenue

Trois maquettes réelles et comparables (écran de saisie + tableau de bord
président), explorées le 20 août 2026 —
[canvas de la direction visuelle](https://claude.ai/code/artifact/2e81e057-b8b6-406d-8f72-aec38a588282).

| | Direction | L'idée |
|---|---|---|
| A | Le Registre | La liste est le document — rangées réglées, montants tabulaires, saisie en feuille au bas de l'écran |
| B | Le Guichet | Une carte par membre ; toucher une carte l'ouvre sur place pour saisir |
| **C** | **Le Compteur** — **retenue** | Un membre à la fois, plein écran, total qui grimpe en direct en bandeau fixe |

**Direction C.** Elle colle le mieux au cas d'usage de l'ADR-001 : un trésorier
qui compte à voix haute pendant que les cotisations rentrent, pas quelqu'un qui
parcourt une liste. Le bandeau de total fixe en haut donne à toute la salle le
même repère visuel que le décompte oral. Détail complet : `docs/DECISIONS.md` D-34.

### Jetons de design — Direction C, centralisés dans `index.css` / le thème Tailwind

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

Ces valeurs viennent des maquettes du canvas et sont déjà posées dans le socle
(AKW-247). Les reprendre telles quelles ; ne pas en réinventer de nouvelles.

### Contraintes non négociables, sur tous les écrans

- **Français** partout dans l'interface — c'est la langue du projet.
- **Montants en BIF, sans décimale**, séparateur d'espace : `1 250 000 BIF`. Le franc
  burundais n'a pas de sous-unité utilisée ; afficher `1250000.00` est faux.
- **Chiffres tabulaires** (`font-variant-numeric: tabular-nums`) dans toute colonne de
  montants, sinon les totaux ne s'alignent pas.
- **Cibles tactiles ≥ 48 px** sur tout ce qui se tape en réunion.
- **Mobile d'abord**, `md:` ensuite pour l'administration au portable.
- **Aucun `fetch` écrit à la main** — tout passe par le client généré depuis OpenAPI
  (AKW-248, voir section 4).

---

## 4. Les prompts, dans l'ordre

Chaque prompt indique qui le lance. Ouvrir Claude Code **à la racine du dépôt**,
pas dans `client/`. Chaque étape suppose la précédente terminée et mergée.

### P0 — la direction visuelle · déjà fait, pour mémoire

La direction C a été choisie le 20 août (section 3, D-34). Ce prompt n'est plus à
relancer — gardé ici pour comprendre comment la décision a été prise, et au cas où
elle devrait être rouverte.

> Lis `docs/ADR-001-CLIENT-WEB-PWA.md`, `docs/MODELE-DE-DONNEES.md` et
> `docs/GUIDE-CLIENT-REACT.md`. N'écris aucun code pour l'instant. Propose une
> direction visuelle pour un client React destiné à un trésorier qui saisit 40
> cotisations d'affilée, à une main, en réunion, sur un Android d'entrée de gamme.
> Donne une palette, une échelle typographique, une échelle d'espacement, et les
> tailles de cibles tactiles — chaque choix justifié par le cas d'usage. Termine
> par les trois écrans les plus risqués et pourquoi.

### P1 — le socle client · déjà fait (AKW-247, PR #13)

Layout mobile-first, routeur, thème Tailwind avec les jetons de la section 3,
composant `Montant`. Rien à relancer ; à connaître avant P2.

### P2 — le client API généré · Andy, AKW-248

> Ajoute un script `pnpm gen:api` qui génère un client TypeScript typé depuis
> `http://localhost:8080/v3/api-docs`, avec les hooks TanStack Query.
>
> Le code généré va dans `client/src/api/generated/` et n'est **jamais** modifié à
> la main. Documente la commande dans `client/README.md`.
>
> Fournir ensuite un exemple d'appel typé permettant de vérifier que les types du
> backend arrivent bien jusqu'au composant.

### P3 — authentification et garde de routes · Andy, dans AKW-281

> Implémente l'écran de connexion et la protection des routes par rôle
> (`TRESORIER`, `MEMBRE`, `COMMISSAIRE`, `GESTIONNAIRE`, `PRESIDENT`).
>
> Le jeton JWT est stocké et rattaché à chaque requête. Une 401 renvoie à la
> connexion. Le `tontineId` vient **du jeton**, jamais d'un champ de formulaire —
> voir R1.
>
> Écrans d'erreur en français, jamais de message technique brut.

### P4 — l'écran de référence · Benitha, AKW-285

C'est le plus exigeant des dix, et il fixe le motif que les autres réutilisent.
Ne commence qu'une fois P2 et P3 mergés — sinon il n'y a ni client API généré ni
authentification pour le faire fonctionner.

> Implémente l'écran de saisie rapide des cotisations. Contexte réel : le trésorier
> a 40 membres à saisir en réunion, à une main, debout.
>
> Exigences : liste des membres du cycle en cours, montant saisi en un geste, total
> mis à jour en direct, confirmation immédiate, et **aucune perte de saisie si le
> réseau tombe** — mise en file et renvoi.
>
> Traite explicitement les quatre états : chargement, liste vide, erreur réseau,
> échec de validation métier (HTTP 409). Démontrer chacun de ces états.

### P5 — les huit autres écrans · Juste, Gloria, Klein, et Andy (AKW-282), chacun sur son propre ticket

Ne commence qu'une fois P4 mergé — c'est le motif de référence à suivre.

> Lis `docs/GUIDE-CLIENT-REACT.md` et regarde
> `client/src/features/cotisation/` — c'est le motif de référence, suis-le.
>
> Implémenter les écrans du ticket **AKW-\<numéro, voir la table section 1\>**.
>
> Contraintes : uniquement les fichiers du domaine concerné, sous
> `client/src/features/<domaine>/` ; appels d'API par le client généré, jamais de
> `fetch` ; les quatre états obligatoires (chargement, vide, erreur réseau, erreur
> métier) ; aucune couleur ni taille en dur.

### P6 — les états, une passe transversale · qui termine en dernier

> Passe en revue tous les écrans et vérifie que chacun traite : chargement, vide,
> erreur réseau, erreur métier, et absence de droits.
>
> Lister les écrans non conformes avant de corriger quoi que ce soit.

### P7 — PWA et mise en service · Andy, non attribué à un ticket pour l'instant

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
