# TUTO 30 MIN — Andy — Architecture, sécurité et fondations REST

> Format : tutoriel complet, comme si vous l'expliquiez au jury qui découvre le projet.
> Chaque étape = **l'idée** → **l'appel REST** → **le code réel** → **ce que ça prouve**.
> Complément de `01-andy-architecture-securite.md` (le plan temps à temps).

---

## 0. Vos 30 minutes, minute par minute

| Temps | Contenu | Support |
|---|---|---|
| 00:00–03:00 | Problème métier : une tontine = un groupe de personnes qui épargnent ensemble. L'argent d'un groupe ne doit JAMAIS être visible d'un autre groupe. | Schéma 3 colonnes : tontine A, B, C |
| 03:00–08:00 | **JEE en 3 concepts** : IoC, JPA, DTO (voir §1). On montre `AuthController` + `AuthService` + `UtilisateurRepository`. | Swagger ouvert |
| 08:00–13:00 | REST : verbes, codes HTTP, `201/400/401/403/404/409`. Tableau des codes selon `GlobalExceptionHandler`. | tableau projeté |
| 13:00–20:00 | **Démo 1 — login JWT** : `POST /api/auth/login`, décodage du jeton (roles + tontineId). | Swagger / Postman |
| 20:00–24:00 | **Démo 2 — R1 isolation** : `GET /api/membres` avec 2 jetons de tontines différentes. | terminal curl |
| 24:00–27:00 | **Démo 3 — R8 verrou** : cotisation reçue → 409 au `PUT`. | Swagger |
| 27:00–30:00 | OpenAPI, tests de sécurité, questions, relais vers Klein (CI/CD). | Swagger + tests |

---

## 1. Le vocabulaire JEE que TOUT le jury doit repartir en connaissant

| Terme | Définition simple | Où dans le projet |
|---|---|---|
| **JEE / Jakarta EE** | La plateforme Java d'applications d'entreprise : servlets, JPA, validation, injection. Spring Boot en est l'implémentation moderne la plus utilisée. | le pom.xml |
| **IoC (Inversion de Contrôle)** | Ce n'est pas votre code qui fabrique ses dépendances : c'est le conteneur Spring. Vous annoncez « j'ai besoin d'un `AuthService` » et Spring le fournit tout construit. | injection par constructeur partout |
| **Bean** | Un objet géré par le conteneur Spring (`@Service`, `@Controller`, `@Repository`, `@Component`). | chaque classe `@Service` |
| **JPA** | L'API Java pour mapper des objets Java sur des tables de base de données relationnelles. Une entité `@Entity` = une table. | `Membre.java`, `Utilisateur.java` |
| **Hibernate** | L'implémentation de JPA utilisée par Spring Boot. | dépendance `spring-boot-starter-data-jpa` |
| **Repository** | L'interface Spring Data qui fabrique les requêtes SQL à partir du nom de la méthode : `findByEmail(...)` devient une requête SQL. | `UtilisateurRepository` |
| **DTO (Data Transfer Object)** | Un objet de transport entre client et serveur. Il exprime UNIQUEMENT ce qui doit traverser le réseau — jamais l'entité brute, jamais un champ technique. | `dto/LoginRequest.java`, `MembreRequest.java` |
| **Record** | Syntaxe Java 21 pour un DTO immuable : champs + constructeur + getters en une ligne. | `LoginRequest`, `LoginResponse` |
| **Transactional** | `@Transactional` groupe plusieurs opérations en une seule unité : tout ou rien. Si une règle échoue, la base n'a pas de demi-écriture. | `AuthService.login()` |
| **Bean Validation** | Annotations `@NotBlank`, `@Email`, `@Positive` sur le DTO. Le serveur rejette les données invalides AVANT tout traitement. | `LoginRequest` |

### Exemple réel — un DTO avec validation (le contenu du fichier)

`api/src/main/java/bi/ac/upg/akiwacu/auth/dto/LoginRequest.java`

```java
public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse) {
}
```

### Exemple réel — l'injection par constructeur (IoC en action)

`api/src/main/java/bi/ac/upg/akiwacu/auth/AuthService.java` (extraits)

```java
@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
```

> À dire : « Trois dépendances, zéro `new`. Spring les construit et les injecte.
> C'est l'IoC : je déclare mes besoins, le conteneur les satisfait.
> L'avantage : je peux remplacer un bean par un faux en test sans toucher au service. »

---

## 2. Démo 1 — Connexion et JWT

### L'idée
Le serveur vérifie email + mot de passe, puis remet au client un **JWT** signé.
Ce jeton porte les autorisations : `roles` et surtout `tontineId` (la base de R1).

### L'appel REST
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "andy@tontine-a.test",
  "motDePasse": "motdepasse"
}
```

### La réponse réelle (structure)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.….",
  "expiration": "2026-06-01T12:30:00Z",
  "utilisateurId": 1,
  "nom": "Habyarimana",
  "prenom": "Andy",
  "tontineId": 1,
  "roles": ["ADMIN"]
}
```

### Le code réel qui répond

`api/src/main/java/bi/ac/upg/akiwacu/auth/AuthController.java` — la couche HTTP :

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authentifier un utilisateur",
               description = "Vérifie l'email et le mot de passe, renvoie un JWT portant tontineId et roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentification réussie"),
        @ApiResponse(responseCode = "400", description = "Email ou mot de passe manquant/invalide"),
        @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest requete) {
        return ResponseEntity.ok(authService.login(requete));
    }
}
```

`api/src/main/java/bi/ac/upg/akiwacu/auth/AuthService.java` — la couche métier :

```java
@Transactional(readOnly = true)
public LoginResponse login(LoginRequest requete) {
    Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.email())
            .orElseThrow(() -> new IdentifiantsInvalidesException(MESSAGE_IDENTIFIANTS_INVALIDES));

    if (!utilisateur.isActif()) {
        throw new IdentifiantsInvalidesException(MESSAGE_IDENTIFIANTS_INVALIDES);
    }

    if (!passwordEncoder.matches(requete.motDePasse(), utilisateur.getMotDePasse())) {
        throw new IdentifiantsInvalidesException(MESSAGE_IDENTIFIANTS_INVALIDES);
    }

    String jeton = jwtService.genererToken(utilisateur);
    return new LoginResponse(jeton, jwtService.expirationDe(jeton),
            utilisateur.getId(), utilisateur.getNom(), utilisateur.getPrenom(),
            utilisateur.getTontine().getId(), utilisateur.getRoles());
}
```

`api/src/main/java/bi/ac/upg/akiwacu/auth/JwtService.java` — la fabrication du jeton :

```java
public String genererToken(Utilisateur utilisateur) {
    Instant maintenant = Instant.now();
    Instant expiration = maintenant.plus(expirationMinutes, ChronoUnit.MINUTES);

    List<String> roles = utilisateur.getRoles().stream()
            .map(Enum::name)
            .toList();

    return Jwts.builder()
            .subject(utilisateur.getEmail())
            .claim("utilisateurId", utilisateur.getId())
            .claim("tontineId", utilisateur.getTontine().getId())
            .claim("roles", roles)
            .issuedAt(Date.from(maintenant))
            .expiration(Date.from(expiration))
            .signWith(cle)
            .compact();
}
```

### Ce que ça prouve
- **Architecture en couches** : le contrôleur ne fait QU'HTTP, le service fait le métier (3 vérifications), le `JwtService` signe.
- **Une seule cause d'échec** : les 3 échecs lèvent la MÊME exception avec le MÊME message → un attaquant ne sait jamais si l'email ou le mot de passe est faux.
- **BCrypt** : le mot de passe stocké est haché (`BCryptPasswordEncoder`), on ne compare jamais en clair.

> **CAPTURE à prendre** : `Swagger → POST /api/auth/login` avec la réponse JSON complète et le jeton visible.

---

## 3. Démo 2 — R1, l'isolation par tontine (le point qui fait gagner des points)

### L'idée
Chaque requête authentifiée porte un jeton contenant `tontineId`. Le serveur ne prend
jamais en compte ce que le client envoie dans le corps ou dans l'URL : il lit le jeton.

### Le flux complet (à dessiner au tableau)
```
Requête HTTP avec "Authorization: Bearer <jeton>"
        │
        ▼
JwtAuthenticationFilter  →  lit le jeton
        │                    extrait tontineId   (R1 : vient du JWT, jamais du body)
        ▼
TenantContext.setTontineId(tontineId)   (ThreadLocal, un par thread de requête)
        │
        ▼
Service  →  TenantContext.getTontineId()
        ▼
Repository  →  appelle findByIdAndTontineId(id, tontineId)   (filtre manuel)
   + filtre Hibernate "tontineFilter" auto-activé par Aspect  (seconde ligne)
```

### Le code réel — le filtre qui pose le tenant

`api/src/main/java/bi/ac/upg/akiwacu/auth/JwtAuthenticationFilter.java` :

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXE_BEARER = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest requete,
                                     HttpServletResponse reponse,
                                     FilterChain chaine) throws ServletException, IOException {
        try {
            String token = extraireToken(requete);
            if (token != null) {
                authentifier(token);
            }
            chaine.doFilter(requete, reponse);
        } catch (JwtException ex) {
            // Jeton invalide/expiré : on n'authentifie pas, la requête continue.
            // Sur une route protégée, SecurityConfig renverra 401.
            chaine.doFilter(requete, reponse);
        } finally {
            // Tomcat réutilise ses threads : sans ce clear(), le tontineId
            // fuiterait vers la requête suivante traitée par ce thread.
            TenantContext.clear();
        }
    }

    private void authentifier(String token) {
        String email = jwtService.extraireEmail(token);
        Long tontineId = jwtService.extraireTontineId(token);
        List<Role> roles = jwtService.extraireRoles(token);
        // ... autorités ROLE_* dans le SecurityContext ...
        TenantContext.setTontineId(tontineId);   // ← la base de R1
    }
}
```

### Le code réel — le contexte par thread

`api/src/main/java/bi/ac/upg/akiwacu/common/TenantContext.java` :

```java
public final class TenantContext {

    private static final ThreadLocal<Long> TONTINE_ID = new ThreadLocal<>();

    public static void setTontineId(Long tontineId) {
        TONTINE_ID.set(tontineId);
    }

    public static Long getTontineId() {
        Long tontineId = TONTINE_ID.get();
        if (tontineId == null) {
            throw new IllegalStateException(
                    "TenantContext.getTontineId() appelé hors d'une requête authentifiée");
        }
        return tontineId;
    }

    public static void clear() {
        TONTINE_ID.remove();
    }

    public static boolean estDefini() {
        return TONTINE_ID.get() != null;
    }
}
```

> **ThreadLocal** : chaque requête HTTP est traitée par un thread de Tomcat. Le
> `ThreadLocal` fait que chaque thread a SA propre valeur — c'est une mémoire
> par requête, pas globale. Le `clear()` dans le `finally` empêche la fuite
> entre deux requêtes qui passent par le même thread.

### La démonstration à faire (R1 prouvée en 2 appels)

1. Login avec le compte de la tontine A → jeton A.
2. `GET /api/membres` avec `Authorization: Bearer <jetonA>` → membres de A.
3. Login avec le compte de la tontine B → jeton B.
4. `GET /api/membres` avec `Authorization: Bearer <jetonB>` → membres de B (≠ A).

```
curl -H "Authorization: Bearer $JETON_A" http://localhost:8080/api/membres
curl -H "Authorization: Bearer $JETON_B" http://localhost:8080/api/membres
```

### La sécurité en profondeur (2 lignes de défense)
1. **Primaire** — chaque repository filtre par tenant (méthodes nommées comme
   `findByIdAndMembreTontineId(id, tontineId)`).
2. **Seconde ligne** — l'`Aspect` active le filtre Hibernate `tontineFilter`
   avant tout repository, donc une requête qui oublierait le filtre est quand
   même protégée.

`api/src/main/java/bi/ac/upg/akiwacu/common/TenantFilterAspect.java` :

```java
@Aspect
@Component
public class TenantFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void activerFiltreTontine() {
        if (!TenantContext.estDefini()) {
            return;               // avant authentification (ex. login), on ne filtre pas
        }
        entityManager.unwrap(Session.class)
                .enableFilter("tontineFilter")
                .setParameter("tontineId", TenantContext.getTontineId());
    }
}
```

> **CAPTURE** : les deux réponses `GET /api/membres` (tontine A vs B) côte à côte.

---

## 4. Démo 3 — R8 : une opération reçue ne se modifie pas (HTTP 409)

### L'idée
Les règles métier R1–R8 sont **dans les services**, et une règle violée renvoie
**HTTP 409 Conflict** — pas 400 (la requête est bien formée), pas 500 (ce n'est pas
une erreur technique) : l'état métier rend l'opération impossible.

### La chaîne réelle des codes HTTP (à projeter)

`api/src/main/java/bi/ac/upg/akiwacu/common/GlobalExceptionHandler.java` :

| Exception levée par le service | Code HTTP renvoyé | Exemple |
|---|---|---|
| `MethodArgumentNotValidException` | **400** | champ obligatoire manquant (validation Bean) |
| `IdentifiantsInvalidesException` | **401** | email/mot de passe incorrect |
| `AuthenticationException` | **401** | pas de jeton sur une route protégée |
| `AccessDeniedException` | **403** | rôle insuffisant |
| `RessourceIntrouvableException` | **404** | id inexistant — ou appartenant à un autre tenant |
| `RegleMetierException` / `OperationVerrouilleeException` | **409** | règle R2, R3, R4, R6, R7, R8 violée |

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RegleMetierException.class, OperationVerrouilleeException.class})
    public ResponseEntity<ErreurResponse> gererConflitMetier(RuntimeException ex,
                                                                HttpServletRequest requete) {
        return construire(HttpStatus.CONFLICT, ex.getMessage(), requete);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> gererRessourceIntrouvable(RessourceIntrouvableException ex,
                                                                       HttpServletRequest requete) {
        return construire(HttpStatus.NOT_FOUND, ex.getMessage(), requete);
    }
    // ...
}
```

### Démonstration courte R8 (coordonnée avec Benitha pour l'étape cotisation)
1. Benitha enregistre une cotisation → un reçu PDF est généré → l'opération est **verrouillée**.
2. `PUT /api/cotisations/{id}` sur cette cotisation → **409** `"R8 : une cotisation ayant un reçu ne peut pas être modifiée"`.

Le verrou est posé dans `RecuGenerationService` (cf. tuto Juste) : l'opération
reçoit un `recu` et passe `verrouille = true`. Le service `CotisationService.modifier`
le détecte et lève `RegleMetierException`, traduite ici en 409.

---

## 5. La configuration Spring Security (le fichier le plus important)

`api/src/main/java/bi/ac/upg/akiwacu/config/SecurityConfig.java` :

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/actuator/health/**",
                    "/actuator/prometheus",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/tontines").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**À dire mot pour mot :**
> « STATELESS : le serveur ne garde aucune session. Chaque requête porte son jeton.
> Seuls le login, Swagger et les endpoints de santé/métriques sont publics — plus
> `POST /api/tontines`, qui est l'inscription libre-service d'une nouvelle tontine
> (avant qu'aucun jeton n'existe). Tout le reste exige un JWT valide. Et le rôle,
> lui, s'affine route par route avec `@PreAuthorize` dans chaque contrôleur. »

Exemple de contrôle de rôle : `TontineController.modifier`

```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public TontineResponse modifier(@PathVariable Long id, @Valid @RequestBody TontineRequest requete) {
    return tontineService.modifier(id, requete);
}
```

---

## 6. L'inventaire de VOS fichiers (pour répondre « c'est quoi ce fichier ? »)

| Fichier | Contenu réel résumé |
|---|---|
| `auth/AuthController.java` | 1 endpoint : `POST /login`. Délègue tout à `AuthService`. |
| `auth/AuthService.java` | Vérifie email actif + mot de passe (BCrypt), émet le jeton. Message d'erreur volontairement identique. |
| `auth/JwtService.java` | Fabrique le jeton HS256 avec claims `sub`, `utilisateurId`, `tontineId`, `roles` ; parse et vérifie. |
| `auth/JwtAuthenticationFilter.java` | Lit `Authorization: Bearer`, pose `Authentication` ET `TenantContext`, `clear()` en `finally`. |
| `auth/dto/LoginRequest.java` | Record `email` + `motDePasse` avec Bean Validation. |
| `auth/dto/LoginResponse.java` | Record `token`, `expiration`, infos utilisateur, `tontineId`, `roles`. |
| `config/SecurityConfig.java` | Chaîne de filtres Security, STATELESS, routes publiques, `PasswordEncoder` BCrypt. |
| `config/OpenApiConfig.java` | Bean OpenAPI : info, security scheme Bearer pour Swagger. |
| `common/TenantContext.java` | `ThreadLocal<Long>` portant le `tontineId` de la requête courante (R1). |
| `common/TenantFilterAspect.java` | Activer le filtre Hibernate `tontineFilter` avant chaque repository (2e ligne R1). |
| `common/GlobalExceptionHandler.java` | `@RestControllerAdvice` : traduit toutes les exceptions métier en codes HTTP. |
| `common/package-info.java` | Déclare `@FilterDef(name = "tontineFilter", ...)` — la définition unique du filtre Hibernate. |
| `common/ValidateurCourantService.java` | Résout le trésorier authentifié depuis le SecurityContext (utilisé par R5). |

---

## 7. Correspondance ÉNONCÉ → votre démonstration

| L'énoncé demande (règle/livrable) | Votre preuve |
|---|---|
| §33 « isolation des données par tontine » (**R1**) | Jeton → `tontineId` claim → `TenantContext` → repository filtrés + filtre Hibernate. Test `shouldNotAccessDataFromAnotherTontine()`. |
| Spring Security + JWT | `SecurityConfig` (STATELESS) + `JwtAuthenticationFilter` + `JwtService`. Démo login. |
| Gestion des exceptions | `GlobalExceptionHandler` : tableau des codes 400→409, format `ErreurResponse` cohérent. |
| Spring Security (routes authentifiées) | `POST /api/tontines` seule route libre + login ; tout le reste 401/403. |
| Contrôleur REST (couche) | `AuthController`, `MembreController`, `UtilisateurController` : HTTP uniquement, zéro logique métier. |
| DTO et validation | `LoginRequest`, `MembreRequest` : records + Bean Validation → 400 automatique. |
| Tests | `SecurityConfigTest`, `TenantContextTest`, `TenantFilterAspectTest`, tests service/controller utilisateur+membre. |

---

## 8. Les 4 pièges que doit connaître le jury (et vous)

1. **Pourquoi `tontineId` ne vient-il pas du body ?** Parce qu'un client malveillant
   choisirait alors le périmètre qu'il veut voir. Le serveur déduit le périmètre de
   l'identité signée, pas des données non signées.
2. **Pourquoi 404 et pas 403 quand on touche à une ressource d'un autre tenant ?**
   Un 403 dirait « je sais que ça existe, mais tu n'y as pas droit ». Le 404 fait
   comme si la ressource n'existait pas — on ne fuite aucune information (R1).
3. **Pourquoi 3 exceptions → même message au login ?** Éviter de laisser deviner si
   c'est l'email ou le mot de passe qui est faux (protection contre l'énumération).
4. **Pourquoi le filtre Hibernate est "seconde ligne" ?** Les requêtes filtrées
   manuellement restent la défense principale, lisible et testable. Le filtre est
   là pour qu'une future entité multi-tenant soit protégée même si son repository
   oublie le filtre.

---

## 9. Questions probables + amorces de réponse

- **« C'est quoi un ThreadLocal ? »** → Une case mémoire par thread ; chaque requête
  HTTP a son propre thread donc sa propre valeur. Voir `TenantContext`.
- **« Pourquoi `@Transactional(readOnly = true)` sur login ? »** → Les lectures de
  login n'écrivent rien ; le mode readOnly optimise et documente l'intention.
- **« Comment expire un jeton ? »** → Claim `exp` posé par `JwtService` ;
  `extraireClaims` le vérifie ; un jeton expiré est traité comme absent → 401.
- **« Pourquoi ne pas utiliser les sessions Servlet ? »** → On a un client web + mails +
  curieux ; un jeton signé est auto-contenu, scale horizontalement sans session partagée.

---

> **Prérequis démo** : API démarrée (`cd api && MAVEN_OPTS='-Djdk.attach.allowAttachSelf=true' ./mvnw spring-boot:run`),
> PostgreSQL actif (`docker compose up -d db`), deux comptes déjà créés (une tontine A, une tontine B).