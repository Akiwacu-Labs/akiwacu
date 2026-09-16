# Soutenance — Juste

## Message à défendre

Je possède `tontine`, `adhesion` et `recu`. J'ai transformé la relation d'adhésion en
entité métier, protégé les lectures par tenant et livré le service partagé qui produit
un reçu PDF téléchargeable.

## Ce que je montre

1. Création/lecture d'une tontine et inscription du premier administrateur.
2. Création d'une adhésion et vérification de l'isolation.
3. `GET /api/recus/{id}/pdf` avec `Content-Type: application/pdf`.
4. Le PDF ouvert : numéro, date, tontine, membre, montant, type d'opération et
   trésorier validateur.
5. Le test `RecuGenerationServiceTest` et les contrôleurs concernés.

## Déroulé de mes 30 minutes

| Temps | Contenu |
|---|---|
| 0–4 min | Besoin métier : remplacer les registres papier par des données traçables |
| 4–10 min | MCD/MLD : tontine, membre, adhésion, opérations et relations |
| 10–15 min | JEE/Spring Data JPA : entité, repository, service et transaction |
| 15–21 min | REST : création tontine, adhésion, consultation et téléchargement PDF |
| 21–26 min | Démonstration d'un reçu contenant les sept champs obligatoires |
| 26–28 min | Tests, isolation tenant et intégration avec les autres domaines |
| 28–30 min | Limites, livrables manquants et questions |

## Explication JEE en une minute

« JPA mappe les objets Java vers les tables PostgreSQL. Spring Data fournit les
repositories, Spring gère les dépendances et les transactions, et Spring MVC expose
les services sous forme de ressources REST. Le reçu est un service applicatif partagé,
donc les trois types d'opérations utilisent le même contrat. »

## Explication du modèle

Une adhésion est une entité et non une simple table technique, car elle représente
l'entrée d'un membre dans une tontine avec son propre état, ses dates et ses règles.
Le reçu est généré par un service afin que cotisation, prêt débloqué et remboursement
utilisent le même contrat au lieu de copier trois générateurs différents.

## Correspondance Énoncé → ma présentation

| L'énoncé demande | Où je le montre (tuto : `03-juste-tuto-30min.md`) |
|---|---|
| Base de données (modèle, relations) | §2 MCD/MLD + entités `Adhesion.java`/`Tontine.java` + migration Flyway. |
| Isolation par tontine (**R1**) | §3 `trouverEntiteAccessible` (id de chemin ≠ tenant) + vérifications tenant du service adhésion. |
| Trésorier validateur (**R5**) | §5 champ « Validé par » du PDF issu de `ValidateurCourantService`, jamais du client. |
| Reçu verrouille l'opération (**R8**) | §5 `verifierOperationDisponible` + `setVerrouille(true)` → 409 ensuite. |
| API REST complète | §3-5 : CRUD tontine, adhésion, `GET /api/recus/{id}/pdf`. |
| Tests entités/services/contrôleurs | `RecuGenerationServiceTest`, `TontineControllerTest`, `AdhesionControllerTest`, `RecuControllerTest`. |
| Livrable reçu PDF téléchargeable | §5 démo PDF avec les 7 champs. |
| Individuel : « explique ton code » | §1 vocabulaire (entité, repository, transaction, DTO), §6 inventaire, §8 pièges. |

## Preuves dans le dépôt

- `api/src/main/java/bi/ac/upg/akiwacu/tontine/`
- `api/src/main/java/bi/ac/upg/akiwacu/adhesion/`
- `api/src/main/java/bi/ac/upg/akiwacu/recu/`
- `api/src/test/java/bi/ac/upg/akiwacu/recu/RecuGenerationServiceTest.java`
- Commits `feat(tontine)`, `feat(recu)` et `feat(adhesion)` dans l'historique develop.

## Questions probables

**Comment éviter deux reçus identiques ?** Le numéro et l'opération doivent être
contrôlés par la base et par le service transactionnel ; je montre la contrainte ou,
si elle n'est pas encore prouvée, je l'annonce comme amélioration.

**Pourquoi le trésorier vient-il de la sécurité ?** Le validateur est l'utilisateur
authentifié, pas une valeur libre fournie par le client.

**Pourquoi générer côté serveur ?** Le serveur possède les données fiables et peut
garantir le même format et les mêmes champs pour les trois opérations.

## Alerte livrables

`bruno/` et `livrables/` sont actuellement vides à l'exception de `.gitkeep`. Je ne
dirai pas que la collection Postman, l'OpenAPI exporté ou le rapport technique sont
livrés. Après la soutenance, la priorité est de produire ces exports depuis l'API
réellement démarrée.
