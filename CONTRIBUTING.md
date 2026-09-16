# CONTRIBUER À CE PROJET

Ce document décrit comment notre équipe de cinq travaille sur ce dépôt.
Il est la référence en cas de désaccord sur le processus.

## Branches

| Branche | Rôle |
|---|---|
| `main` | Production. Protégée. Merge depuis `develop` uniquement. |
| `develop` | Intégration. Protégée. Branche par défaut. |
| `feat/<domaine>-<slug>` | Une fonctionnalité. Durée de vie : 1 à 2 jours. |
| `fix/<domaine>-<slug>` | Une correction. |
| `test/`, `chore/`, `docs/` | Tests, outillage, documentation. |

**Une branche = une fonctionnalité.** Pas de branche personnelle longue durée :
elle diverge, produit des conflits massifs et rend l'historique illisible.

## Messages de commit — Conventional Commits

```
<type>(<domaine>): <description à l'infinitif>
```

Types : `feat` `fix` `test` `docs` `chore` `refactor`

```
feat(cotisation): ajouter la saisie rapide multi-membres
fix(pret): corriger le calcul de l'épargne nette pour R6
test(vote): couvrir le refus de double vote d'un commissaire
```

Minimum **2 commits par jour ouvré et par personne**.

## Pull Requests

- Ouverte depuis une branche de fonctionnalité vers `develop`
- Moins de **400 lignes** modifiées
- Le template de PR est rempli, les cases réellement cochées
- **CI verte** et **au moins une revue approuvée** avant merge
- Merge commit — **jamais de squash** : l'historique individuel doit rester visible
- Branche supprimée après le merge

## Revue de code

SLA : **3 heures ouvrées**. Une PR qui dort bloque un coéquipier.

Checklist du relecteur :

- [ ] Règles métier dans la couche service, pas dans le contrôleur
- [ ] Cas d'erreur ET cas nominal testés
- [ ] Pas de secret, pas de valeur en dur
- [ ] Endpoint annoté OpenAPI
- [ ] `tontineId` pris depuis le JWT, jamais depuis le corps de la requête (R1)
- [ ] Montants en `BigDecimal`, jamais `double`
- [ ] Je comprends ce code sans que l'auteur me l'explique

Les revues sont **écrites**, jamais orales. Un commentaire cite une ligne précise
et un problème précis.

## Definition of Done

1. Branche créée depuis `develop`
2. Entité · repository · service · contrôleur · DTO · mapper
3. Règles métier dans le service
4. Tests : nominal + erreur + exception
5. Couverture ≥ 80 % sur le package concerné
6. Endpoint annoté OpenAPI et visible dans Swagger
7. Écran client qui consomme l'endpoint
8. Requête ajoutée à la collection Bruno
9. CI verte + revue approuvée, mergé, **vérifié en direct sur VM-DEV**

## Assistance par IA

L'usage d'assistants IA est autorisé pour produire une première version du code
et des tests. Chaque auteur reste **entièrement responsable** de l'exactitude de
son code et de sa capacité à l'expliquer.

Règle d'équipe : **l'IA écrit le premier jet, l'humain possède l'explication.**
Toute PR contenant du code assisté par IA le mentionne explicitement.

## Qualité

- Couverture minimale : **80 %** (seuil JaCoCo bloquant dans la CI)
- Quality Gate SonarQube sur le code neuf : bloquant
- Analyse Trivy des images Docker : bloquante sur les CVE critiques
