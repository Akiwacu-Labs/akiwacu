## Ce que fait cette PR

<!-- 2-3 phrases. Quel comportement métier est ajouté ou corrigé ? -->

## Ticket Jira

<!--
Écris la clé du ticket, par exemple : AKW-42
PAS de « # » — le dièse désigne une issue GitHub, et notre tableau est Jira.

Mets aussi la clé dans le nom de la branche et dans au moins un commit :
    git checkout -b feat/cotisation-saisie-rapide
    git commit -m "feat(cotisation): saisie rapide multi-membres (AKW-42)"

L'app « GitHub for Jira » lit ces clés et rattache automatiquement branche,
commits, PR et déploiement au ticket. C'est la traçabilité que le correcteur
vérifie en un clic — personne ne relie quoi que ce soit à la main.
-->

AKW-

## Règles métier concernées

<!-- Coche celles qui s'appliquent et dis en une ligne comment elles sont respectées -->

- [ ] R1 — isolation par tontine
- [ ] R2 — cycle actif
- [ ] R3 — pas de prêt sur cycle gelé/clôturé
- [ ] R4 — 2 commissaires distincts
- [ ] R5 — trésorier validateur enregistré
- [ ] R6 — prêt ≤ 3× épargne
- [ ] R7 — échéance ≤ fin de cycle
- [ ] R8 — opération avec reçu non modifiable
- [ ] Aucune

## Definition of Done

- [ ] Entité · repository · service · contrôleur · DTO · mapper
- [ ] Règles métier dans la couche **service**, pas dans le contrôleur
- [ ] Tests : cas nominal **+** cas d'erreur **+** exception
- [ ] Couverture ≥ 80 % sur mon package (capture ou lien JaCoCo ci-dessous)
- [ ] Endpoint annoté OpenAPI et visible dans Swagger UI
- [ ] Écran client qui consomme réellement l'endpoint
- [ ] Requête ajoutée à la collection Bruno
- [ ] CI verte
- [ ] Pas de secret, pas de valeur en dur

## Comment tester

```bash
# commandes exactes pour rejouer le comportement
```

## Captures

<!-- Swagger, écran client, ou rapport de couverture -->

---
