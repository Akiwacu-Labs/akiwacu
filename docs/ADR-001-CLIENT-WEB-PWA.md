# ADR-001 — Application web responsive (PWA) plutôt qu'un client natif

**Statut :** acceptée · **Date :** D1 · **Auteur :** @miguelandy875 · **Décision liée :** `DECISIONS.md` D-18


---

## Contexte

Une tontine n'est pas un bureau, c'est une **réunion**. Les membres viennent avec du
liquide, le trésorier enregistre les cotisations une par une, les demandes de prêt
sont débattues, les commissaires votent, l'argent sort. Le support actuel est le
registre papier.

Cinq acteurs, et ils ne sont pas au même endroit :

| Acteur | Ce qu'il fait | Où | Appareil naturel |
|---|---|---|---|
| **Trésorier** | Saisit 20 à 50 cotisations d'affilée, valide les déblocages et remboursements, émet les reçus | À la table de réunion, argent en main | Téléphone ou tablette — la vitesse compte, le réseau est instable |
| **Membre** | Consulte son épargne, dépose une demande de prêt, suit son échéancier, garde ses reçus | N'importe où, sur son propre téléphone | Téléphone |
| **Commissaire** | Vote sur les demandes de prêt (R4 : deux approbations distinctes) | N'importe où, en asynchrone | Téléphone |
| **Gestionnaire** | Crée les tontines, ouvre/gèle/clôture les cycles, gère adhésions, utilisateurs et rôles | Assis, travail de saisie | Ordinateur portable — beaucoup de formulaires |
| **Président** | Tableaux de bord : solde de caisse, totaux, impayés | En revue | Grand écran |

Les deux facteurs de forme sont donc requis par le cas d'usage lui-même, pas par une
préférence technique.

## Options envisagées

| Option | Verdict |
|---|---|
| **Client web responsive (PWA)** | Retenue |
| Application native Android (Kotlin ou Flutter) | Écartée — 2 à 3× le temps par écran, et l'artefact ne passe pas dans la chaîne CI/CD |
| Rendu serveur Thymeleaf intégré à l'API | Écartée — ne démontre pas qu'une API REST est consommable par un client indépendant |
| Deux clients, un mobile et un desktop | Écartée — hors budget sur 8 jours |

## Décision

**React 18 + TypeScript, conçu mobile-first, livré en PWA installable.**

Vite · TypeScript · Tailwind CSS · shadcn/ui · TanStack Query · React Router ·
React Hook Form + Zod · **client API généré depuis la spécification OpenAPI**.

## Justification

1. **Le terrain est mobile, l'administration est desktop.** Le responsive est la seule
   option qui couvre les deux sans construire deux clients.
2. **Le critère de notation est la couverture d'endpoints.** Environ 75 endpoints à
   exposer dans le délai. Un client natif coûte deux à trois fois le temps par écran
   pour zéro point supplémentaire.
3. **Les reçus PDF sont générés côté serveur.** Sur le web, c'est un lien de
   téléchargement. En natif, ce sont des permissions fichiers, une API de stockage et
   une visionneuse — de la friction pure.
4. **Le client web se déploie dans la chaîne CI/CD.** Un conteneur nginx part sur
   `vm-dev-g1` et `vm-prod-g1` comme l'API. Un APK ne passe pas par GitHub Actions
   vers une VM : c'est une part de la note DevOps perdue sèche.

## Conséquences

**Positives.** Un seul artefact déployable. L'outillage ne consomme pas de temps de
développement : Vite évite toute configuration webpack, shadcn/ui fournit les
composants, et le client API généré fait que l'équipe écrit des formulaires plutôt que
de la plomberie HTTP.

**Contraintes qui en découlent.**

- **Interdiction d'écrire un `fetch` à la main.** On régénère le client depuis la spec
  (`pnpm gen:api`). Un appel écrit à la main diverge silencieusement de l'API.
- Le client a son propre `Dockerfile` et sa `nginx.conf`, avec le repli SPA
  `try_files $uri $uri/ /index.html`. **Sans ce repli, recharger la page sur
  `/tontines` renvoie 404** — en pleine démonstration.
- Le mode hors-ligne reste partiel : consultation en cache, pas d'écriture différée.
  C'est un choix de périmètre assumé, à mentionner en perspectives.

## Formulation courte pour le rapport

> Contexte terrain mobile-first, administration desktop → application web responsive
> PWA : un seul artefact déployable, intégré à la chaîne CI/CD au même titre que l'API.

## Question probable à la soutenance

*« Pourquoi pas une application mobile native, alors que vos utilisateurs sont sur le
terrain ? »*

Répondre par le tableau des acteurs : deux des cinq travaillent sur grand écran. Puis
par la chaîne de déploiement : l'énoncé exige un déploiement automatique par GitHub
Actions vers les VM, et un APK n'a pas sa place dans ce flux.
