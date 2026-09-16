# Soutenance — Andy

## Message à défendre

J'ai construit le socle partagé : authentification JWT, rôles, isolation par tontine,
gestion des utilisateurs et des membres, gestion des erreurs, configuration OpenAPI,
migrations Flyway et coordination de l'intégration.

Le sujet impose une API REST Spring Boot et huit règles métier. Mon point central est
que la sécurité n'est pas seulement une annotation : le `tontineId` vient du JWT via
`TenantContext`, puis les services et repositories vérifient le tenant. Une erreur
métier est traduite en HTTP 409 par `GlobalExceptionHandler`.

## Ce que je montre

1. `POST /api/auth/login` et le JWT contenant le rôle et la tontine.
2. Une lecture authentifiée de membres/utilisateurs.
3. La même requête avec une autre tontine : aucune donnée n'est retournée.
4. Une tentative de modification d'une opération déjà reçue : HTTP 409.
5. Swagger `/swagger-ui.html`, `/v3/api-docs` et l'arborescence `config/common/auth`.

## Déroulé de mes 30 minutes

| Temps | Contenu |
|---|---|
| 0–3 min | Problème métier, objectifs et architecture client–API–PostgreSQL |
| 3–8 min | Framework JEE : Spring Boot, IoC/injection, contrôleur, service, repository, JPA |
| 8–13 min | REST : ressources, verbes HTTP, DTO, validation, codes 200/201/400/401/403/404/409 |
| 13–20 min | Démonstration login JWT, rôle et isolation R1 entre deux tontines |
| 20–24 min | R8, gestion globale des exceptions et OpenAPI/Swagger |
| 24–27 min | Tests de sécurité et choix d'architecture défendus |
| 27–30 min | Questions, limites et relais vers Klein pour CI/CD |

## Explication REST/JEE en une minute

« Spring Boot fournit le serveur et son conteneur IoC. Le contrôleur reçoit une
requête REST et valide son DTO. Le service applique la règle métier. Le repository
JPA traduit l'accès aux entités PostgreSQL. Cette séparation permet de tester le
métier sans dépendre de HTTP et de retourner des codes REST cohérents. »

## Correspondance Énoncé → ma présentation

| L'énoncé demande | Où je le montre (tuto : `01-andy-tuto-30min.md`) |
|---|---|
| Isolation des données par tontine (**R1**) | Démo 2 : 2 jetons, 2 tontines → `GET /api/membres` distincts. Filtre JWT → `TenantContext` → repositories + filtre Hibernate. |
| Spring Security + JWT | Démo 1 : `POST /api/auth/login`, décodage du jeton (roles + tontineId). `SecurityConfig` STATELESS. |
| Gestion des exceptions / codes HTTP | Tableau `GlobalExceptionHandler` : 400/401/403/404/409. Démo R8 → 409. |
| Contrôleurs REST / DTO / validation | `AuthController`, `MembreController`, `UtilisateurController` + records validés. |
| Tests (entités, services, contrôleurs) | `SecurityConfigTest`, `TenantContextTest`, `TenantFilterAspectTest`, tests utilisateur/membre. |
| Individuel : « explique ton code » | §5 `SecurityConfig`, §6 inventaire fichier par fichier, §8 les 4 pièges. |

## Preuves dans le dépôt

- `api/src/main/java/bi/ac/upg/akiwacu/auth/`
- `api/src/main/java/bi/ac/upg/akiwacu/common/`
- `api/src/main/java/bi/ac/upg/akiwacu/config/`
- `api/src/main/java/bi/ac/upg/akiwacu/utilisateur/`
- `api/src/main/java/bi/ac/upg/akiwacu/membre/`
- `docs/DECISIONS.md`, `docs/MODELE-DE-DONNEES.md`, `CLAUDE.md`
- Tests : `SecurityConfigTest`, `TenantContextTest`, `TenantFilterAspectTest`, tests
  des services et contrôleurs utilisateur/membre.

## Réponse technique courte

**Pourquoi le tenant ne vient-il pas du body ?** Parce que le client ne doit jamais
pouvoir choisir le périmètre d'accès. Le serveur le déduit de l'identité JWT.

**Pourquoi la règle est-elle dans le service ?** Le contrôleur transporte HTTP ; le
service porte le cas d'usage et reste testable même sans serveur web.

**Pourquoi 409 ?** La requête est syntaxiquement valide mais impossible selon l'état
 métier, donc `GlobalExceptionHandler` renvoie un conflit explicite.

## À ne pas affirmer sans preuve live

Le checkout actuel est `docs/planning-phase-c-d`, pas `develop`, et plusieurs fichiers
locaux sont non commités. Je présenterai comme intégré uniquement ce qui est visible
dans GitHub/develop le jour de la soutenance. La correction locale de `MembreService`
et la nouvelle interface client doivent être décrites comme « en cours » si elles ne
sont pas mergées.

## Phrase de conclusion

« Notre choix de sécurité est défensif : authentification au bord de l'API, tenant
issu du JWT, vérification dans les services et filtre Hibernate en seconde ligne. Cela
réduit le risque qu'un endpoint oublié expose la tontine d'un autre groupe. »
