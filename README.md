# Akiwacu

> Plateforme web de gestion des associations d’épargne et de crédit communautaires
> (tontines) — projet académique Frameworks JEE + API REST, UPG Gitega.

Akiwacu centralise les membres, adhésions, cycles d’épargne, cotisations,
demandes de prêt, votes des commissaires, déblocages, remboursements, caisse et
reçus. L’application repose sur l’isolation stricte des tontines,
l’authentification JWT et des règles métier vérifiables par l’API.

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Démarrage rapide](#démarrage-rapide)
- [API et documentation](#api-et-documentation)
- [Tests et qualité](#tests-et-qualité)
- [Client et OpenAPI](#client-et-openapi)
- [Configuration](#configuration)
- [Docker et déploiement](#docker-et-déploiement)
- [Sécurité et règles métier](#sécurité-et-règles-métier)
- [Contribution](#contribution)

## Fonctionnalités

- Authentification stateless par JWT et contrôle d’accès par rôle.
- Gestion multi-tenant des tontines, utilisateurs et membres.
- Gestion des adhésions et des cycles `OUVERT`, `GELE` et `CLOTURE`.
- Saisie individuelle et par lot des cotisations.
- Demandes de prêt, vote de deux commissaires distincts et résumé de décision.
- Déblocage des prêts et consultation de leur échéancier.
- Remboursements, suivi de caisse et génération de reçus PDF.
- Documentation OpenAPI et Swagger UI.
- Tests unitaires, web, JPA et intégration PostgreSQL avec Testcontainers.
- Client React responsive, mobile-first, avec TanStack Query et types OpenAPI.

Le contrat actuel expose **55 opérations HTTP sur 32 chemins**. Le client est
développé progressivement : une nouvelle opération backend doit être ajoutée au
contrat puis régénérée et intégrée côté client.

## Architecture

```text
akiwacu/
├── api/          Spring Boot 3, Java 21, REST, JPA, JWT et Flyway
├── client/       React 18, TypeScript, Vite, Tailwind et TanStack Query
├── infra/        Ansible et Terraform
├── monitoring/   Prometheus et dashboards Grafana
├── bruno/        Collections de tests API
├── docs/         ADR, règles métier, guides et planification
├── livrables/    Exports et documents de soutenance
├── docker-compose.yml       Services locaux
├── docker-compose.dev.yml   Environnement dev déployé
└── docker-compose.prod.yml  Environnement production
```

Le backend est organisé par domaine : `auth`, `utilisateur`, `membre`, `tontine`,
`adhesion`, `cycle`, `cotisation`, `demandepret`, `vote`, `pret`,
`remboursement`, `caisse`, `dashboard` et `recu`.

## Technologies

| Couche | Technologies |
| --- | --- |
| API | Java 21, Spring Boot 3, Spring MVC, Spring Data JPA, Hibernate |
| Sécurité | Spring Security, JWT, isolation par tontine |
| Base | PostgreSQL 16, Flyway |
| Documentation | springdoc OpenAPI, Swagger UI |
| Tests | JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers, JaCoCo |
| Client | React 18, TypeScript, Vite, Tailwind CSS |
| État/formulaires | TanStack Query, React Hook Form, Zod |
| Observabilité | Actuator, Micrometer, Prometheus, Grafana |
| Livraison | Docker, Compose, GitHub Actions, GHCR, SonarQube, Trivy |

## Prérequis

- Git
- Java 21
- Docker et Docker Compose
- Node.js avec Corepack activé
- pnpm 10+

```bash
java -version
docker --version
docker compose version
node --version
pnpm --version
```

## Démarrage rapide

### 1. Démarrer les dépendances locales

Depuis la racine :

```bash
docker compose up -d
```

Cette commande démarre PostgreSQL sur `localhost:5432`, Prometheus sur `9090`
et Grafana sur `3000`.

### 2. Démarrer l’API

Dans un deuxième terminal :

```bash
cd api
./mvnw spring-boot:run
```

L’API écoute sur `http://localhost:8080`. Flyway applique les migrations au
démarrage et utilise PostgreSQL local par défaut.

### 3. Démarrer le client

Dans un troisième terminal :

```bash
cd client
pnpm install
pnpm dev
```

Le client écoute sur `http://localhost:5173`. Vite redirige `/api/*` vers
`http://localhost:8080`.

### 4. Vérifier les services

```bash
curl http://localhost:8080/actuator/health
```

Pages utiles :

- Client : <http://localhost:5173>
- Swagger UI : <http://localhost:8080/swagger-ui.html>
- Contrat OpenAPI : <http://localhost:8080/v3/api-docs>
- Prometheus : <http://localhost:9090>
- Grafana : <http://localhost:3000>

## API et documentation

Les commandes Maven se lancent depuis `api/` :

```bash
./mvnw -B clean compile
./mvnw -B test
./mvnw -B verify
```

`verify` peut démarrer un conteneur PostgreSQL pour les tests d’intégration ;
Docker doit donc être accessible au processus Maven.

Authentification :

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "utilisateur@example.com",
  "motDePasse": "mot-de-passe"
}
```

Les routes protégées utilisent `Authorization: Bearer <jeton-jwt>`.

Codes HTTP utilisés : `200` lecture, `201` création, `204` suppression, `400`
validation, `401` session absente/expirée, `403` rôle insuffisant, `404`
ressource absente et `409` règle métier violée.

Exporter le contrat :

```bash
curl http://localhost:8080/v3/api-docs > /tmp/akiwacu-openapi.json
```

## Tests et qualité

```bash
# Backend
cd api
./mvnw -B test
./mvnw -B verify

# Client
cd ../client
pnpm lint
pnpm build
```

Le rapport JaCoCo est généré dans `api/target/site/jacoco/`. Les tests couvrent
les services, contrôleurs, repositories, entités et règles métier. Les tests
d’intégration utilisent PostgreSQL avec Testcontainers.

La CI GitHub Actions vérifie notamment la compilation, les tests, la couverture,
les scans de sécurité, les images et, selon l’environnement, SonarQube et le
déploiement.

## Client et OpenAPI

Le client ne doit pas écrire d’appels `fetch` ou Axios à la main. Régénérer les
types depuis l’API démarrée :

```bash
cd client
pnpm install
pnpm gen:api
pnpm lint
pnpm build
```

La commande met à jour `client/src/api/generated/schema.d.ts`, qui ne doit pas
être modifié manuellement.

Principes frontend :

- TanStack Query pour les lectures, mutations et invalidations de cache.
- React Hook Form + Zod pour les formulaires.
- `localStorage` réservé au JWT.
- Montants via `Montant`, en BIF sans décimales.
- Interface française, mobile-first à partir de 360 px.
- Tokens visuels centralisés dans `client/src/index.css`.
- Les règles métier restent vérifiées par le backend.

## Configuration

Les valeurs locales par défaut sont compatibles avec `docker-compose.yml` :

```text
DB_URL=jdbc:postgresql://localhost:5432/akiwacu
DB_USER=akiwacu
DB_PASSWORD=akiwacu
SERVER_PORT=8080
```

Pour un environnement partagé ou de production, fournir les secrets par
l’environnement d’exécution et ne jamais les committer :

```text
SPRING_PROFILES_ACTIVE=dev|prod
DB_URL=...
DB_USER=...
DB_PASSWORD=...
JWT_SECRET=...
```

Le secret JWT de production doit être long, aléatoire et différent de toute
valeur de développement.

## Docker et déploiement

Le Compose local fournit les dépendances de développement. Les fichiers
`docker-compose.dev.yml` et `docker-compose.prod.yml` utilisent les images GHCR
et sont destinés aux environnements déployés.

```bash
docker compose ps
docker compose logs -f db
docker compose down
```

Ne jamais supprimer les volumes PostgreSQL sur un environnement contenant des
données utiles. Éviter en particulier `docker system prune --volumes`.

## Sécurité et règles métier

Les règles métier sont implémentées dans les services, jamais dans les
contrôleurs :

- **R1** — isolation totale ; `tontineId` vient du JWT via `TenantContext`.
- **R2** — toute opération financière concerne un cycle actif.
- **R3** — aucun prêt sur un cycle gelé ou clôturé.
- **R4** — approbation par deux commissaires distincts.
- **R5** — chaque opération financière enregistre le trésorier validateur.
- **R6** — montant demandé limité à trois fois l’épargne du membre.
- **R7** — échéance du prêt au plus tard à la fin du cycle.
- **R8** — une opération associée à un reçu n’est plus modifiable ni supprimable.

Voir [`docs/MATRICE-REGLES-METIER.md`](docs/MATRICE-REGLES-METIER.md) et
[`docs/DECISIONS.md`](docs/DECISIONS.md) pour les détails et tests obligatoires.

## Organisation et documentation

Pour commencer :

1. [`docs/SETUP-DEPUIS-ZERO.md`](docs/SETUP-DEPUIS-ZERO.md) — installation.
2. [`docs/GUIDE-CLIENT-REACT.md`](docs/GUIDE-CLIENT-REACT.md) — frontend.
3. [`docs/MODELE-DE-DONNEES.md`](docs/MODELE-DE-DONNEES.md) — modèle de données.
4. [`docs/DECISIONS.md`](docs/DECISIONS.md) — décisions d’architecture.
5. [`docs/PLANNING-PHASE-C-D.md`](docs/PLANNING-PHASE-C-D.md) — priorités.
6. [`CONTRIBUTING.md`](CONTRIBUTING.md) — contribution et pull requests.
7. [`SECURITY.md`](SECURITY.md) — signalement des vulnérabilités.

## Contribution

1. Créer une branche `feat/<domaine>-<slug>`, `fix/<slug>` ou `docs/<slug>`.
2. Respecter les responsabilités de domaine et `AGENTS.md`/`CLAUDE.md`.
3. Utiliser Conventional Commits, par exemple
   `feat(cotisation): ajouter la saisie par lot`.
4. Ajouter les tests du comportement nominal et des erreurs importantes.
5. Exécuter les contrôles locaux avant de pousser.
6. Ouvrir une pull request avec le résumé, les tests et les dépendances éventuelles.

Les règles détaillées sont dans [`CONTRIBUTING.md`](CONTRIBUTING.md) et
[`CLAUDE.md`](CLAUDE.md).

## Licence et crédits

Projet académique réalisé à l’Université Polytechnique de Gitega. Voir
[`NOTICE.md`](NOTICE.md) pour les informations de copyright et de licence.
