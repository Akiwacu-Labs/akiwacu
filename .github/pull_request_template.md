## Ce que fait cette PR

<!-- 2-3 phrases. Quel comportement métier est ajouté ou corrigé ? -->

## Story / Épic

Closes #

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
