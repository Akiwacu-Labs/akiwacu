# LIVRABLES — checklist de dépôt

Énoncé §12. À vérifier intégralement au **D8 au matin**, avant l'export final.
Un livrable manquant est une perte sèche, indépendante de la qualité du code.

---

## Les 14 livrables

| # | Livrable | Propriétaire | Où il se trouve | ☐ |
|---|---|---|---|---|
| 1 | Lien du dépôt GitHub | Andy | — | ☐ |
| 2 | Code source complet de l'API REST | tous | `api/` | ☐ |
| 3 | Code source complet de l'application cliente | tous | `client/` | ☐ |
| 4 | **Script SQL de création de la base** | Andy | `livrables/schema.sql` | ☐ |
| 5 | Fichiers Docker et Docker Compose | Klein | `api/Dockerfile` `client/Dockerfile` `docker-compose*.yml` | ☐ |
| 6 | Workflows GitHub Actions | Klein | `.github/workflows/` | ☐ |
| 7 | Tableaux de bord Grafana (JSON exporté) | Klein | `monitoring/grafana/` | ☐ |
| 8 | Fichiers de configuration Prometheus | Klein | `monitoring/prometheus.yml` | ☐ |
| 9 | **Collection de requêtes REST** | Andy | `bruno/` + `livrables/postman-collection.json` | ☐ |
| 10 | Documentation Swagger / OpenAPI | tous | `/swagger-ui` + `livrables/openapi.json` | ☐ |
| 11 | Rapport de couverture JaCoCo | Klein | `livrables/jacoco/` | ☐ |
| 12 | Rapport SonarQube | Klein | `livrables/sonar-rapport.pdf` | ☐ |
| 13 | Rapport Trivy | Klein | `livrables/trivy-rapport.json` | ☐ |
| 14 | Rapport technique 15-25 pages | tous, Andy rédacteur en chef | `livrables/rapport-technique.pdf` | ☐ |

### Les deux livrables qui se ratent le plus

**№ 4 — le script SQL.** Flyway crée la base par migrations successives ; l'énoncé
demande un script de création consolidé. Ce n'est pas la même chose. Il s'obtient
après un déploiement réussi :

```bash
# sur vm-prod-g1, une fois la base à jour
docker exec -t <conteneur-postgres> pg_dump -U akiwacu --schema-only akiwacu \
  > livrables/schema.sql
```

**№ 9 — la collection Postman.** Nous travaillons avec **Bruno**, parce que les
fichiers `.bru` sont du texte et passent en revue de code. L'énoncé §12.9 nomme
explicitement Postman. Un export au format Postman est donc produit au D7, en plus du
dossier `bruno/`.

---

## Répartition du rapport technique

15 à 25 pages. Chacun rédige son chapitre **sur Confluence**, en parallèle. Andy
assemble et exporte en PDF au D8.

| Chapitre | Auteur | Pages |
|---|---|---|
| 1. Contexte, objectifs, analyse des besoins | Andy | 3 |
| 2. Conception de la base — MCD, MLD, diagramme de classes | Juste | 4 |
| 3. Architecture logicielle et sécurité, **+ ADR-001 et ADR-002** | Andy | 4 |
| 4. Règles métier et leur implémentation | Gloria | 3 |
| 5. Captures de l'application cliente | Benitha | 3 |
| 6. Captures Swagger / OpenAPI | Gloria | 1 |
| 7. Tests, couverture, résultats **avec interprétation** | Klein | 3 |
| 8. SonarQube et Trivy **avec interprétation** | Klein | 2 |
| 9. Pipeline CI/CD et déploiements vm-dev-g1 / vm-prod-g1 | Klein | 3 |
| 10. Captures Grafana | Klein | 2 |
| 11. Perspectives d'amélioration | Andy | 1 |

> L'énoncé insiste : les résultats doivent être « **accompagnés d'une interprétation**
> des principaux résultats ». Ne collez pas de captures brutes. Commentez au moins
> **3 issues Sonar et 3 CVE Trivy** : ce que vous avez corrigé, ce que vous avez
> accepté, et pourquoi. C'est un point facile que la plupart des groupes rateront.

Les ADR-001 et ADR-002 se recopient presque tels quels dans le chapitre 3 — ils sont
déjà rédigés dans `docs/`.

---

## Procédure de dépôt — D8

1. **10 h** — chacun confirme que son chapitre est terminé sur Confluence
2. **11 h** — Andy exporte le rapport en PDF et relit d'un bout à l'autre
3. **12 h** — export des artefacts vers `livrables/` : `schema.sql`, JaCoCo, Sonar,
   Trivy, OpenAPI, collection Postman, JSON Grafana
4. **14 h** — répétition générale, 90 minutes
5. **17 h** — snapshot Proxmox de `vm-prod-g1`, dernier push, dépôt

Le snapshot est l'assurance rollback du jour J. Il prend une minute et évite d'arriver
devant le jury avec une production cassée par un dernier correctif.
