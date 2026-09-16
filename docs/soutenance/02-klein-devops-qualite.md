# Soutenance — Klein

## Message à défendre

Je transforme un push en application contrôlée : Maven compile et teste, JaCoCo mesure,
SonarQube analyse, Docker construit, Trivy inspecte l'image, GHCR la stocke, puis le
runner auto-hébergé déploie sur les VM. Le même dépôt contient Ansible, Terraform,
Docker Compose, Prometheus et Grafana.

## Ce que je montre

1. `.github/workflows/cicd-develop.yml` : chemin `develop` vers VM-DEV.
2. `.github/workflows/cicd-main.yml` : chemin `main` vers VM-PROD.
3. `api/Dockerfile` : build multi-stage et utilisateur non root.
4. `infra/ansible/playbooks/`, `infra/terraform/` et `docker-compose*.yml`.
5. `monitoring/grafana/dashboard-technique.json` et `dashboard-metier.json`.
6. `/actuator/health`, `/actuator/prometheus`, puis les deux dashboards.

## Déroulé de mes 30 minutes

| Temps | Contenu |
|---|---|
| 0–3 min | Pourquoi industrialiser une API REST : reproductibilité et confiance |
| 3–7 min | JEE/Spring côté exploitation : Actuator, profils dev/prod, configuration externe |
| 7–12 min | Dockerfile multi-stage, Compose, PostgreSQL et réseau de déploiement |
| 12–19 min | Pipeline GitHub Actions : build, tests, JaCoCo, Sonar, Trivy, GHCR |
| 19–23 min | Démonstration du déploiement develop→VM-DEV et main→VM-PROD |
| 23–27 min | Prometheus, Grafana, métriques techniques et métier |
| 27–30 min | Quality gates, limites mesurées et questions |

## Explication DevOps en une minute

« Un push ne doit pas seulement produire du code : il doit produire une preuve.
GitHub Actions compile l'API Spring Boot, exécute les tests, mesure la couverture,
scanne la qualité et l'image Docker, puis publie et déploie seulement si les étapes
précédentes sont acceptées. Actuator expose la santé et les métriques que Prometheus
scrape et que Grafana visualise. »

## Explication du pipeline

Le déploiement dépend des étapes précédentes : un test, un Quality Gate ou un scan
Trivy bloquant doit arrêter la chaîne. Le runner est auto-hébergé parce que les VM sont
dans le réseau de l'université et ne sont pas directement joignables depuis GitHub.
Le runner ouvre une connexion sortante, ce qui respecte cette contrainte réseau.

## Correspondance Énoncé → ma présentation

| L'énoncé demande | Où je le montre (tuto : `02-klein-tuto-30min.md`) |
|---|---|
| CI/CD : intégration + déploiement | §4 workflow `cicd-develop.yml` (VM-DEV) et `cicd-main.yml` (VM-PROD), `needs:`. |
| Conteneurisation | §3 `api/Dockerfile` multi-stage, non-root, HEALTHCHECK + compose + GHCR. |
| Tests + couverture (≥ 80 %) | §4 step `./mvnw test` + JaCoCo ; §6 état honnête (77,5 % mesuré, gate prêt). |
| Sécurité de l'image | §4 Trivy : CVE CRITIQUES bloquent le pipeline ; rapport exporté. |
| SonarQube | §4 étapes conditionnelles `SONAR_ENABLED` / `SONAR_GATE`, explication des drapeaux. |
| Monitoring technique ET métier | §5 Prometheus + 2 dashboards Grafana (`dashboard-technique.json`, `dashboard-metier.json`). |
| Les deux VM déployées + vérification | §4 `deploy-vm-dev` / `deploy-vm-prod` + boucle de vérification `/actuator/health` 200. |
| Individuel : « explique ton code » | §1 vocabulaire (image, pipeline, gate), §7 inventaire fichier par fichier, §9 pièges. |

## Ce qui est réellement présent

- Workflows develop/main avec Maven, couverture, Docker, Trivy et publication GHCR.
- Playbooks Ansible pour l'application, le monitoring et SonarQube.
- Deux dashboards JSON versionnés et configuration Prometheus.
- Service `caisse`, `remboursement` et `dashboard`, avec tests et endpoints.

## Limites à annoncer honnêtement

Le brief exige 80% de couverture et un rapport exporté. La dernière mesure documentée
dans le handoff est 77,5% (865/1116 lignes), donc « proche mais non conforme » tant
qu'une nouvelle mesure ne prouve pas 80%. Les variables documentées indiquent aussi
`SONAR_ENABLED=false` et `SONAR_GATE=false`. Ne pas présenter ces gates comme verts.

Le dépôt local ne contient pas encore `livrables/jacoco/`, `livrables/sonar-rapport.pdf`
ou `livrables/trivy-rapport.json`. Il faut montrer une preuve GitHub/VM existante, ou
dire que l'export est à finaliser.

## Questions probables

**Pourquoi Docker multi-stage ?** Les outils Maven restent dans l'étape de build ;
l'image runtime est plus petite et contient seulement le JRE et l'application.

**Que bloque Trivy ?** Le workflow échoue sur les CVE critiques non corrigées avant
la publication/déploiement de l'image.

**Que se passe-t-il si la VM tombe ?** Le job échoue ; on conserve le déploiement
précédent et on diagnostique le runner/SSH avant de relancer.

## Priorité immédiate

Exporter une preuve récente de JaCoCo, Sonar, Trivy, Grafana et des deux déploiements.
Ne jamais inventer une capture ni un résultat.
