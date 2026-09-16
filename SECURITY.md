# POLITIQUE DE SÉCURITÉ

## Analyse automatisée

| Outil | Portée | Effet |
|---|---|---|
| **Trivy** | Images Docker (API et client) | Bloque le pipeline sur CVE `CRITICAL` (dev) et `CRITICAL,HIGH` (prod) |
| **SonarQube** | Code source | Quality Gate bloquant : Security Hotspots à 100 % revus |
| **GitHub secret scanning** | Dépôt entier | Actif par défaut (dépôt public), avec protection au push |
| **Dependabot** | Dépendances Maven et npm | Alertes activées |

## Mesures applicatives

- Authentification par **JWT**, mots de passe hachés avec **BCrypt**
- Autorisation par rôle : `ADMIN` `GESTIONNAIRE` `TRESORIER` `COMMISSAIRE` `MEMBRE`
- **Isolation multi-tenant (R1)** : `tontineId` extrait exclusivement du JWT,
  jamais du corps de la requête ni d'un paramètre non vérifié
- Traçabilité (R5) : chaque opération financière enregistre son validateur
- Immuabilité (R8) : toute opération ayant généré un reçu est verrouillée
- Conteneurs exécutés avec un **utilisateur non root**
- Aucun secret dans le code : variables d'environnement et GitHub Secrets uniquement

## Signaler une vulnérabilité

Ouvrez une issue avec le label `security`, ou contactez directement
`@miguelandy875`. Merci de ne pas divulguer publiquement les détails d'une
vulnérabilité avant qu'elle ne soit corrigée.

## Périmètre

Projet académique, déployé sur une infrastructure on-premise de l'Université
Polytechnique de Gitega, non exposée à Internet. Il ne traite aucune donnée
personnelle réelle : les jeux de données sont fictifs.
