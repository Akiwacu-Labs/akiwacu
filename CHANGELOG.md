# Journal des modifications

Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/).
Ce projet suit un versionnement sémantique.

> **À mettre à jour à chaque merge dans `develop`.** Une ligne suffit.
> C'est la responsabilité de l'auteur de la PR, et c'est vérifié en revue.

## [Non publié]

### Ajouté
- Socle du projet : 13 entités JPA, migrations Flyway, `BaseEntity` avec audit
- Sécurité : authentification JWT, rôles, `GlobalExceptionHandler`, configuration OpenAPI
- Chaîne CI/CD : GitHub Actions, runner auto-hébergé, GHCR, SonarQube, Trivy, JaCoCo

### Modifié

### Corrigé

---

## Modèle pour les prochaines entrées

```
## [0.2.0] — 2026-08-09

### Ajouté
- Service de génération de reçus PDF avec les 7 champs obligatoires (#42)
- Règle R4 : approbation par deux commissaires distincts (#47)

### Corrigé
- Le calcul de l'épargne nette ignorait les prêts en cours (R6) (#51)
```
