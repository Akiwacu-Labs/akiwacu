# TUTO 30 MIN — Juste — Modèle de données, tontine, adhésion et reçus PDF

> Format : tutoriel complet. Chaque étape = **l'idée** → **l'appel REST** → **le code réel** → **ce que ça prouve**.
> Complément de `03-juste-modele-recus.md` (le plan temps à temps).

---

## 0. Vos 30 minutes, minute par minute

| Temps | Contenu | Support |
|---|---|---|
| 00:00–04:00 | Problème métier : les registres papier des tontines → des données traçables. | schéma du registre |
| 04:00–10:00 | Le modèle : tables TONTINE, ADHESION, RECU et leurs relations (MCD/MLD). | `docs/MODELE-DE-DONNEES.md` |
| 10:00–15:00 | JEE en actes : `@Entity` (JPA), `Repository`, `Service`, `@Transactional`. | code projeté |
| 15:00–21:00 | REST + démo : créer une tontine, une adhésion, télécharger un reçu PDF. | Swagger + terminal |
| 21:00–26:00 | Démo reçu PDF : les 7 champs obligatoires, le verrou R8. | PDF ouvert |
| 26:00–28:00 | Tests, isolation tenant, intégration avec les autres domaines. | test outputs |
| 28:00–30:00 | Limites (livrables bruno/livrables), questions. | — |

---

## 1. Le vocabulaire JEE du domaine

| Terme | Définition simple | Dans le projet |
|---|---|---|
| **Entité JPA** | Une classe `@Entity` dont CHAQUE objet correspond à UNE ligne d'une table. Les attributs deviennent des colonnes gerées par Hibernate. | `Tontine.java`, `Adhesion.java`, `Recu.java` |
| **Table** | La structure relationnelle. Le schéma exact pi − viennent de `Flyway` (migrations) et `docs/MODELE-DE-DONNEES.md`. | `db/migration/` |
| **Repository** | L'interface Spring Data qui fabrique les requêtes SQL. | `TontineRepository`, `RecuRepository` |
| **Transaction** | `@Transactional` : groupe des écritures « tout ou rien ». | `TontineService.creer()` |
| **Record DTO** | L'objet de transport entre HTTP et service, avec validation Bean Validation. | `TontineRequest`, `AdhesionRequest` |
| **PDF serveur** | Le reçu est géneré côté serveur (donnees fiables, format unique), jamais sur le navigateur. | `RecuGenerationService` |
| **Service partagé** | Une seule classe décrit le reçu pour les 3 opérations (cotisation, prêt, remboursement) → même contrat, pas de code dupliqué. | `RecuGenerationService` |

---

## 2. Le modèle — pourquoi l'adhésion est une entité à part entière

### Les 3 tables que vous montrez

```
TONTINE (id, nom, description, date_creation, statut)
    │ 1
    │
    │ n
ADHESION (id, tontine_id, membre_id, statut, date_adhesion, ...)
    │
    │ n
RECU (id, numero, type_operation, operation_id, montant, emis_par_id, contenu_pdf)
```

> **À dire :** « Une adhésion n'est pas une simple colonne technique : c'est l'entrée
> d'un membre dans une tontine, avec son propre statut, ses dates et ses règles.
> En faire une entité permet de la dater, de la valider, de la refuser — et de
> tracer chaque reçu émis pour cette relation. »

### Démonstration du modèle dans le code — `Adhesion.java` (essentiel)

```java
@Entity
@Table(name = "adhesions")
public class Adhesion extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private StatutAdhesion statut;

    private LocalDate dateAdhesion;

    @ManyToOne
    @JoinColumn(name = "membre_id")
    private Membre membre;

    @ManyToOne
    @JoinColumn(name = "tontine_id")
    private Tontine tontine;
    // ...
}
```

> `@ManyToOne` + `@JoinColumn` : la relation « n adhésions vers 1 tontine » est
> matérialisée par une colonne `tontine_id`. Le nom de colonne est celui du schéma Flyway.

### La migration Flyway (le schéma versionné)

`api/src/main/resources/db/migration/V…__schema.sql` (extrait type)

```sql
CREATE TABLE adhesions (
    id            BIGSERIAL PRIMARY KEY,
    statut        VARCHAR(32)  NOT NULL,
    date_adhesion DATE         NOT NULL,
    membre_id     BIGINT       NOT NULL REFERENCES membres (id),
    tontine_id    BIGINT       NOT NULL REFERENCES tontines (id)
);
```

---

## 3. Démo 1 — Créer une tontine (inscription libre-service)

### L'idée
Quand aucun compte n'existe encore, on crée une tontine ET son premier
administrateur en UN SEUL appel. C'est la seule route publique de création,
exception pensée par sécurité (voir `SecurityConfig`).

### L'appel REST
```
POST /api/tontines
Content-Type: application/json

{
  "nom": "Tontine des développeurs",
  "description": "Épargne du groupe D1",
  "administrateur": {
    "email": "juste@tontine-dev.test",
    "motDePasse": "motdepasse",
    "nom": "Habarurema",
    "prenom": "Juste",
    "telephone": "79123456"
  }
}
```

### La réponse réelle
```json
{
  "id": 1,
  "nom": "Tontine des développeurs",
  "description": "Épargne du groupe D1",
  "dateCreation": "2026-09-16",
  "statut": "ACTIVE"
}
```

### Le code réel qui crée (transaction : TOUT OU RIEN)

`api/src/main/java/bi/ac/upg/akiwacu/tontine/TontineService.java` :

```java
@Transactional
public TontineResponse creer(TontineCreationRequest requete) {
    verifierNomDisponible(requete.nom());
    verifierEmailDisponible(requete.administrateur().email());

    var tontine = Tontine.builder()
            .nom(requete.nom())
            .description(requete.description())
            .dateCreation(LocalDate.now())
            .statut(StatutTontine.ACTIVE)
            .build();
    var tontineCreee = tontineRepository.save(tontine);

    var administrateur = requete.administrateur();
    var utilisateur = Utilisateur.builder()
            .tontine(tontineCreee)
            .email(administrateur.email())
            .motDePasse(passwordEncoder.encode(administrateur.motDePasse()))
            .nom(administrateur.nom())
            .prenom(administrateur.prenom())
            .telephone(administrateur.telephone())
            .roles(Set.of(Role.ADMIN))
            .actif(true)
            .build();
    utilisateurRepository.save(utilisateur);
    return versResponse(tontineCreee);
}
```

> **À dire :** « Deux écritures — la tontine ET l'administrateur. Avec
> `@Transactional`, si la deuxième échoue (ex. email déjà pris), la première est
> ROLLBACK : la base ne reste jamais avec une tontine sans administrateur. »

### Le contrôleur REST — 201 + en-tête `Location`

```java
@PostMapping
public ResponseEntity<TontineResponse> creer(@Valid @RequestBody TontineCreationRequest requete) {
    var reponse = tontineService.creer(requete);
    URI emplacement = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(reponse.id()).toUri();
    return ResponseEntity.created(emplacement).body(reponse);
}
```

> **À dire :** « REST : `POST` crée, on répond `201 Created` avec l'en-tête
> `Location` contenant l'URL de la ressource créée. »

### R1 sur la lecture d'une tontine
Un `@PathVariable` ne peut JAMAIS choisir le tenant — il doit correspondre au
`tontineId` signé dans le JWT :

```java
private Tontine trouverEntiteAccessible(Long id) {
    // Un id de chemin ne peut jamais choisir le tenant ; il doit correspondre
    // au tontineId signé dans le JWT de l'appelant.
    if (!TenantContext.getTontineId().equals(id)) {
        throw new RessourceIntrouvableException("Tontine introuvable : " + id);
    }
    return trouverEntite(id);
}
```

> **CAPTURE** : la réponse `201 Created` avec l'en-tête `Location`.

---

## 4. Démo 2 — Créer une adhésion et vérifier l'isolation

### L'appel REST (authentifié — rôle ADMIN ou GESTIONNAIRE)
```
POST /api/adhesions
Authorization: Bearer <jeton>
Content-Type: application/json

{
  "membreId": 1,
  "cycleId": 1,
  "dateAdhesion": "2026-09-16"
}
```

(selon le `AdhesionRequest` réel du projet : `membreId`, `cycleId`, `dateAdhesion`;
le `statut` de la réponse est posé par le service — seul un membre de la tontine du jeton est accepté)

### Ce que le service contrôle (extraits types du domaine `adhesion`)
- le `membreId` appartient à la tontine du jeton (R1),
- le statut est un `StatutAdhesion` valide,
- la date d'adhésion est posée côté serveur.

### Démontrer l'isolation
1. Login tontine A → `GET /api/adhesions` → leurs adhésions.
2. Login tontine B → `GET /api/adhesions` → les adhésions de B, pas celles de A.

---

## 5. Démo 3 — Le reçu PDF (le point fort)

### L'idée
Cotisation, prêt débloqué et remboursement doivent produire le MÊME reçu avec les
MÊMES 7 champs. Un seul `RecuGenerationService` centralise le contrat et le rendu.

### Les 7 champs obligatoires affichés dans le PDF
1. **Numéro** du reçu
2. **Date** de l'opération
3. **Tontine** (nom)
4. **Membre**
5. **Montant** (format français + « BIF »)
6. **Type d'opération** (COTISATION / DEBLOCAGE_PRET / REMBOURSEMENT)
7. **Trésorier validateur** (R5 — vient de la sécurité, jamais du client)

### Le code réel qui génère le PDF

`api/src/main/java/bi/ac/upg/akiwacu/recu/RecuGenerationService.java` :

```java
private byte[] construirePdf(String numero, LocalDate date, String tontine, String membre,
                             BigDecimal montant, TypeOperationRecu type, String tresorier) {
    try (var sortie = new ByteArrayOutputStream()) {
        var document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, sortie);
        document.open();
        var titre = new Font(Font.HELVETICA, 16, Font.BOLD);
        var normal = new Font(Font.HELVETICA, 11);
        document.add(new Paragraph("TONTINE " + tontine.toUpperCase(Locale.ROOT), titre));
        document.add(new Paragraph("Reçu N° " + numero, normal));
        var table = new PdfPTable(2);
        table.setWidthPercentage(100);
        ajouterLigne(table, "Date", date.toString(), normal);
        ajouterLigne(table, "Membre", membre, normal);
        ajouterLigne(table, "Type d'opération", type.name(), normal);
        ajouterLigne(table, "Montant", formater(montant), normal);
        ajouterLigne(table, "Validé par", tresorier, normal);
        document.add(table);
        document.close();
        return sortie.toByteArray();
    } catch (DocumentException | IOException exception) {
        throw new IllegalStateException("Impossible de générer le PDF du reçu", exception);
    }
}
```

### Le service partagé — un contrat unique pour les 3 opérations

```java
@Override
@Transactional
public Recu genererPourCotisation(Long cotisationId) {
    Cotisation operation = cotisationRepository.findById(cotisationId)
            .orElseThrow(() -> new RessourceIntrouvableException("Cotisation introuvable : " + cotisationId));
    var membre = operation.getMembre();
    verifierTenant(membre.getTontine().getId());
    verifierOperationDisponible(TypeOperationRecu.COTISATION, cotisationId, operation.estVerrouillee());
    return generer(TypeOperationRecu.COTISATION, cotisationId, operation.getDateCotisation(), membre,
            membre.getTontine().getNom(), membre.getNom() + " " + membre.getPrenom(),
            operation.getMontant(), operation.getValidePar(), recu -> {
                operation.setRecu(recu);          // l'opération devient verrouillée (R8)
                operation.setVerrouille(true);
                cotisationRepository.save(operation);
            });
}
```

**Pourquoi une `Consumer` ?** Le gabarit `generer(...)` est identique aux 3 places ;
seul le « verrouillage » diffère (`setRecu` + `setVerrouille` sur la bonne entité).
On passe donc le comportement spécifique en paramètre — zéro duplication.

### Le numéro de reçu et l'antidouble

```java
String numero = "RC-%d-%s-%06d".formatted(date.getYear(), codeTontine(tontine),
        recuRepository.prochainNumero());
```

```java
private void verifierOperationDisponible(TypeOperationRecu type, Long operationId, boolean verrouillee) {
    if (verrouillee || recuRepository.findByTypeOperationAndOperationId(type, operationId).isPresent()) {
        throw new OperationVerrouilleeException("Un reçu existe déjà pour cette opération");
    }
}
```

> **À dire :** « On ne peut pas émettre DEUX reçus pour la même opération : on vérifie
> l'existence + le verrou, et l'unicité est garantie par l'opération déjà verrouillée.
> Une erreur métier ici = HTTP 409 (R8), pas de modification possible après reçu. »

### Le téléchargement — endpoint REST

`api/src/main/java/bi/ac/upg/akiwacu/recu/RecuController.java` :

```java
@RestController
@RequestMapping("/api/recus")
@Tag(name = "Reçus")
public class RecuController {

    private final RecuService recuService;

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Télécharger un reçu PDF")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        byte[] contenu = recuService.telecharger(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"recu-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenu);
    }
}
```

```
GET /api/recus/1/pdf
Authorization: Bearer <jeton>
Accept: application/pdf
```

> **CAPTURE** : le PDF ouvert avec les 7 champs, `Content-Type: application/pdf`.

---

## 6. L'inventaire de VOS fichiers

| Fichier | Contenu réel résumé |
|---|---|
| `tontine/Tontine.java` | `@Entity` : nom, description, dateCreation, statut. |
| `tontine/TontineRepository.java` | `JpaRepository` + `existsByNom`. |
| `tontine/TontineService.java` | créer (avec USER), modifier, supprimer, + contrôle tenant sur l'id de chemin. |
| `tontine/TontineController.java` | endpoints REST + `@PreAuthorize("hasRole('ADMIN')")` sur PUT/DELETE. |
| `tontine/dto/TontineRequest`, `TontineResponse`, `TontineCreationRequest` | records + validation. |
| `adhesion/Adhesion.java` | `@Entity` : statut, dateAdhesion, membre, tontine. |
| `adhesion/AdhesionService.java` | créer/lister/trouver avec contrôles R1. |
| `adhesion/AdhesionController.java`, `dto/*` | REST + DTO records. |
| `recu/RecuService.java` | l'interface du contrat partagé (cotisation / pret / remboursement / telecharger). |
| `recu/RecuGenerationService.java` | l'implémentation PDF, numéro `RC-…`, verrou R8. |
| `recu/RecuController.java` | `GET /{id}/pdf` avec `Content-Disposition: inline`. |
| `recu/Recu.java`, `RecuRepository.java`, `TypeOperationRecu.java` | entité + `findByTypeOperationAndOperationId` + enum des 3 types. |

---

## 7. Correspondance ÉNONCÉ → votre démonstration

| L'énoncé demande | Votre preuve |
|---|---|
| « API REST avec API complète » | CRUD tontine + adhésion + téléchargement PDF. |
| « Entités métier (tests exigés §11.1) » | `Tontine.java`, `Adhesion.java`, `Recu.java` + leurs tests d'entité. |
| « Services (tests) » | `TontineService`, `AdhesionService`, `RecuGenerationService` avec `RecuGenerationServiceTest`. |
| « Contrôleurs REST (tests) » | `TontineControllerTest`, `AdhesionControllerTest`, `RecuControllerTest`. |
| « R1 isolation par tontine » | contrôles `TenantContext` dans `trouverEntiteAccessible`, `verifierTenant`. |
| « R5 trésorier validateur » | le champ « Validé par » du PDF vient de `ValidateurCourantService` (le compte authentifié), pas du client. |
| « R8 opération reçue = verrouillée » | `verifierOperationDisponible` + `setVerrouille(true)` → 409 ensuite. |
| « Base de données » | schéma Flyway versionné, relations `@ManyToOne`. |
| « Livrable reçu PDF téléchargeable » | endpoint `GET /api/recus/{id}/pdf`. |

---

## 8. Les pièges et questions probables

- **« Comment éviter deux reçus identiques ? »** → Le numéro unique + la vérification
  `findByTypeOperationAndOperationId` + le verrou. À défendre : la contrainte
  d'unicité en base doit aussi exister ; si non prouvée → l'annoncer comme amélioration.
- **« Pourquoi le trésorier vient-il de la sécurité ? »** → Le validateur est
  l'utilisateur authentifié (`ValidateurCourantService`), jamais une valeur libre du
  client : sinon n'importe qui écrirait n'importe quel nom sur un reçu (R5).
- **« Pourquoi générer côté serveur ? »** → Le serveur possède les données fiables et
  garantit le même format pour les 3 opérations. Pas de dépendance au navigateur.
- **« C'est quoi JPA ? »** → La couche qui transforme un objet Java en ligne SQL et
  inversement. `@Entity` = table, attributs = colonnes, `@ManyToOne` = clé étrangère.

---

> **Prérequis démo** : API démarrée, PostgreSQL actif, navigateur ouvert.
> Pour le PDF : le rendre SANS confidentialité inutile — l'en-tête `Content-Disposition: inline`
> l'ouvre directement dans le navigateur.
>
> **Alertes honnêtes** : `bruno/` et `livrables/` sont encore vides (`.gitkeep`).
> Ne pas dire que la collection Postman ou l'OpenAPI exporté sont « livrés » : dire
> « la priorité après la soutenance est de produire ces exports depuis l'API démarrée ».