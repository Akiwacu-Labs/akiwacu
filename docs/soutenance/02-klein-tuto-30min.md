# TUTO 30 MIN — Klein — DevOps, CI/CD, qualité et monitoring

> Format : tutoriel complet. Chaque étape = **l'idée** → **le fichier réel** → **le passage clé** → **ce que ça prouve**.
> Complément de `02-klein-devops-qualite.md` (le plan temps à temps).

---

## 0. Vos 30 minutes, minute par minute

| Temps | Contenu | Support |
|---|---|---|
| 00:00–03:00 | Problème : un push doit devenir une application déployée et prouvée, pas juste du code. | schéma pipeline |
| 03:00–07:00 | Actuator + profils : l'app se décrit elle-même (santé, métriques). | terminal `/actuator/health` |
| 07:00–12:00 | Docker multi-stage + Compose : construire une image légère, lancer API+Postgres. | Dockerfile projeté |
| 12:00–19:00 | GitHub Actions : jobs build → tests → couverture → Trivy → GHCR → déploiement. | workflow projeté |
| 19:00–23:00 | Déploiement VM-DEV + VM-PROD : SSH, compose, vérification `health`. | workflow + terminal |
| 23:00–27:00 | Prometheus + Grafana : métriques techniques ET métier. | dashboards |
| 27:00–30:00 | Quality gates (JaCoCo 80 %, SonarQube, Trivy), limites honnêtes, questions. | table de seuils |

---

## 1. Le vocabulaire DevOps que le jury doit repartir en connaissant

| Terme | Définition simple | Résultat concret |
|---|---|---|
| **CI/CD** | Intégration continue : chaque push compile et teste. Déploiement continu : si tout est bon, l'app part seule sur la VM. | 2 workflows par branche |
| **GitHub Actions** | L'outil de CI/CD de GitHub : des `workflows` (fichiers YAML) décrivent des jobs. Chaque `runs-on` nomme une machine d'exécution. | `.github/workflows/cicd-develop.yml` |
| **Runner auto-hébergé** | La machine qui exécute les jobs. La nôtre est une VM dans le réseau de l'université, pas un serveur cloud de GitHub. | `runs-on: [self-hosted, linux, onprem]` |
| **Image Docker** | Un fichier d'usine : OS minimal + JRE + votre jar. Identique partout où elle tourne. | `api/Dockerfile` |
| **Multi-stage** | Un Dockerfile en 2 étages : Maven construit le jar, puis on jette Maven et on ne garde que le JRE + le jar → image plus petite et moins vulnérable. | `api/Dockerfile` |
| **GHCR** | GitHub Container Registry : l'entrepôt d'images de GitHub. | `ghcr.io/<org>/api` |
| **Trivy** | Scanner de vulnérabilités : compare l'image à une base de CVE connues. | `trivy image ...` |
| **Actuator** | Module Spring Boot qui expose la santé et les métriques de l'app. | `/actuator/health`, `/actuator/prometheus` |
| **Prometheus** | Collecteur de métriques (scrape) avec son propre langage de requêtes. | `prometheus.yml` |
| **Grafana** | Tableau de bord qui dessine les métriques de Prometheus. | `monitoring/grafana/*.json` |
| **JaCoCo** | Outil qui mesure la couverture des tests (lignes exécutées / lignes codées). | rapport `target/site/jacoco/` |
| **SonarQube** | Analyse statique : dette, bugs, duplications, couverture par Quality Gate. | `sonar:sonar` |
| **Quality Gate** | Un seuil bloquant : s'il n'est pas atteint, le pipeline s'arrête. | `COVERAGE_GATE`, `SONAR_GATE` |

---

## 2. Montrer qu'une API Spring Boot est « opérationnelle »

### /actuator/health — l'app dit si elle vit

```
curl http://VM_DEV:8080/actuator/health
```

Réponse réelle :
```json
{"status":"UP"}
```

> **À noter** : branchez aussi la base — quand PostgreSQL tombe, l'actuator passe
> `DOWN` et le `HEALTHCHECK` du Dockerfile fait redémarrer/marquer le conteneur.

### Les endpoints exposés (dans `application.yml`)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

| Endpoint | À quoi il sert |
|---|---|
| `/actuator/health` | l'app est vivante (utilisé par Docker HEALTHCHECK et le pipeline) |
| `/actuator/prometheus` | les métriques dans le format que Prometheus scrape |

**À dire :** « Actuator transforme l'application en objet observable : le pipeline et
Prometheus se branchent sur des endpoints standards, sans écrire de code métier. »

---

## 3. Docker multi-stage — le Dockerfile réel

`api/Dockerfile` :

```dockerfile
# Build multi-stage : image finale légère et pauvre en CVE (bon pour Trivy)
# ── Étape 1 : build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ── Étape 2 : runtime
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl && \
    addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=45s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
```

**Les 4 arguments à défendre :**
1. **2 étages** → `maven:...` ne survit pas dans l'image finale. Résultat : image ~moins de 200 Mo au lieu de 800+.
2. **Utilisateur non-root** (`USER app`) → si l'application est compromise, l'attaquant n'a pas root.
3. **HEALTHCHECK** → Docker sait si l'app est en vie et peut la signaler / la redémarrer.
4. **`-XX:MaxRAMPercentage=75`** → la JVM respecte les limites mémoire du conteneur.

Build et scan en local (démo possible sans CI) :
```bash
docker build -t akiwacu-api:demo ./api
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD/livrables:/out" \
  aquasec/trivy:latest image --format json --output /out/trivy-rapport.json akiwacu-api:demo
```

---

## 4. Le pipeline — le workflow réel

`.github/workflows/cicd-develop.yml` — la séquence imposée par l'énoncé (§11.2) :

```
push sur develop
      │
      ▼
build-test-quality        ← compile, test, JaCoCo, (SonarQube), (Quality Gate)
      │
      ▼
docker-build-scan-push    ← docker build, Trivy (CVE CRITICAL bloquant), push GHCR
      │
      ▼
client-build-push         ← client React (drapeau CLIENT_ENABLED)
      │
      ▼
deploy-vm-dev             ← scp compose + ssh → docker compose up -d + check /health
```

Extrait réel du premier job :

```yaml
jobs:
  build-test-quality:
    name: Build, tests, couverture et qualité
    runs-on: [self-hosted, linux, onprem]
    defaults:
      run:
        working-directory: api        # MONOREPO : le pom.xml est dans api/

    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0              # requis par SonarQube pour la blame

      - name: Compilation complète du projet Maven
        run: ./mvnw -B clean compile

      - name: Exécution de l'ensemble des tests unitaires
        run: ./mvnw -B test

      - name: Génération du rapport de couverture (JaCoCo)
        run: ./mvnw -B jacoco:report

      - name: Vérification du seuil de couverture
        if: vars.COVERAGE_GATE == 'true'
        run: ./mvnw -B jacoco:check
```

Extrait du scan Trivy (CVE critiques = pipeline rouge) :

```yaml
      - name: Analyse de sécurité de l'image (Trivy)
        run: |
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v "${TRIVY_CACHE}":/root/.cache \
            aquasec/trivy:latest image \
              --skip-db-update \
              --pkg-types os \
              --timeout 10m \
              --severity CRITICAL \
              --ignore-unfixed \
              --exit-code 1 \         # sortie != 0 si une CVE critique est trouvée
              --format table \
              ${IMAGE}:develop
```

Extrait du déploiement SSH :

```yaml
      - name: Déploiement de l'application
        run: |
          ssh -i ~/.ssh/gha_deploy ${{ secrets.SSH_USER_DEV }}@${{ secrets.SSH_HOST_DEV }} << 'EOF'
            set -e
            cd ~/akiwacu
            echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
            export POSTGRES_PASSWORD='${{ secrets.POSTGRES_PASSWORD_DEV }}'
            export JWT_SECRET='${{ secrets.JWT_SECRET_DEV }}'
            export IMAGE_TAG='develop'
            docker compose pull
            docker compose up -d --remove-orphans
            docker system prune -f
          EOF
```

Puis la vérification de démarrage (boucle de 24×5s) :

```yaml
      - name: Vérification du bon démarrage de l'application
        run: |
          echo "Attente du démarrage (max 120 s)…"
          for i in $(seq 1 24); do
            STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
              http://${{ secrets.SSH_HOST_DEV }}:8080/actuator/health || echo "000")
            if [ "$STATUS" = "200" ]; then
              echo "✅ Application démarrée et saine sur vm-dev-g1"
              exit 0
            fi
            echo "  tentative $i/24 — statut $STATUS"
            sleep 5
          done
          exit 1
```

**Les arguments clés à défendre :**
- **Le déploiement n'arrive qu'après TOUTES les étapes vertes** : `needs:` + vérification explicite des résultats de chaque dépendance.
- **Le runner est auto-hébergé** car les VM sont dans le réseau de l'université : le runner OUVRIT une connexion sortante vers GitHub (pas l'inverse), ce qui respecte le réseau.
- **Les secrets ne sont jamais dans le code** : `${{ secrets.* }}` injectés au moment du run, JWT secret et mot de passe Postgres via `export`.

---

## 5. Monitoring — Prometheus et Grafana

### Prometheus scrape l'API

`monitoring/prometheus.yml` :

```yaml
scrape_configs:
  - job_name: 'akiwacu-api-dev'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['192.168.0.50:8080']
        labels: { environnement: 'dev' }

  - job_name: 'akiwacu-api-prod'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['192.168.0.51:8080']
        labels: { environnement: 'prod' }

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['192.168.0.50:9100', '192.168.0.51:9100']
```

### Deux dashboards (énoncé §10 : techniques ET métier)

`monitoring/grafana/dashboard-technique.json` — CPU, mémoire JVM, heap, threads,
requêtes HTTP par statut (`http_server_requests_seconds_count`), latence p95,
disponibilité (`process_uptime_seconds`), GC — sorties d'Actuator gratuitement.

`monitoring/grafana/dashboard-metier.json` — les indicateurs à instrumenter à la
main avec Micrometer (`monitoring/METRIQUES-METIER.md`) :

| Métrique métier | Signification |
|---|---|
| `akiwacu_membres_total` | nb de membres actifs |
| `akiwacu_cotisations_montant_total` | total des cotisations du cycle |
| `akiwacu_prets_montant_total` / `akiwacu_prets_accordes_total` | encours / prêts débloqués |
| `akiwacu_prets_refuses_total` | par motif (R3, R6, R7) — très parlant en démo |
| `akiwacu_remboursements_montant_total` | total remboursé |
| `akiwacu_solde_caisse` | solde de caisse |
| `akiwacu_cycles_actifs` | cycle en OUVERT |

Exemple d'instrumentation Micrometer dans un service (extrait de `METRIQUES-METIER.md`) :

```java
meterRegistry.counter("akiwacu.cotisations.enregistrees",
        "tontine", tenantContext.getTontineNom()).increment();
meterRegistry.summary("akiwacu.cotisations.montant",
        "tontine", tenantContext.getTontineNom())
        .record(saved.getMontant().doubleValue());
```

---

## 6. Les seuils de qualité (et leur état ACTUEL — à dire honnêtement)

| Gate | Seuil exigé | État constaté au handoff | Commentaire |
|---|---|---|---|
| JaCoCo ligne | 80 % | 77,5 % (865/1116 lignes) | « proche mais non conforme » tant qu'une nouvelle mesure ne montre pas 80 |
| SonarQube | Quality Gate par défaut (couverture 80 % sur code neuf) | `SONAR_ENABLED=false`, `SONAR_GATE=false` | ne présentez PAS les gates comme verts |
| Trivy | aucune CVE CRITICALE | actif sur `develop` | le pipeline devient rouge si CVE critique |

> **PHRASE À DIRE :** « Le Quality Gate JaCoCo est prêt dans le pipeline, mais il est
> activé par la variable `COVERAGE_GATE`. On ne l'active que quand la mesure réelle
> atteint 80 % — pas avant, sinon toutes les PR s'arrêteraient. Notre dernière mesure
> est 77,5 %, donc on est proche mais pas encore vert : je ne le présenterai pas comme
> conforme. »

---

## 7. L'inventaire de VOS fichiers

| Fichier | Contenu réel résumé |
|---|---|
| `.github/workflows/cicd-develop.yml` | develop → VM portant la tontine dev : build, test, jacoco, (sonar), trivy, GHCR, deploy, health. |
| `.github/workflows/cicd-main.yml` | main → VM de production, même chaîne. |
| `api/Dockerfile` | multi-stage Maven→JRE-alpine, non-root, HEALTHCHECK. |
| `api/docker-compose.yml` / `docker-compose.dev.yml` | services `db` (PostgreSQL), `api`, dépendances et env. |
| `infra/ansible/inventory/hosts.yml` | inventory des VM (dev/prod). |
| `infra/ansible/playbooks/00-base.yml` | paquets de base, utilisateurs, réglages réseau/CDN. |
| `infra/ansible/playbooks/10-app.yml` | déploiement de l'application sur la VM. |
| `infra/ansible/playbooks/20-monitoring.yml` | Prometheus + Grafana + node-exporter. |
| `infra/ansible/playbooks/30-sonarqube.yml` | SonarQube + PostgreSQL dédié. |
| `monitoring/prometheus.yml` | scrape des endpoints dev/prod, node-exporter. |
| `monitoring/grafana/dashboard-technique.json` | dashboard technique (CPU, JVM, HTTP, uptime). |
| `monitoring/grafana/dashboard-metier.json` | dashboard métier (membres, cotisations, prêts, solde). |
| `infra/terraform/main.tf`, `variables.tf` | infrastructure des VM si provisionnée par Terraform. |

---

## 8. Correspondance ÉNONCÉ → votre démonstration

| L'énoncé demande | Votre preuve |
|---|---|
| « CI/CD : intégration continue + déploiement » | Workflows `cicd-develop.yml` (VM-DEV) et `cicd-main.yml` (VM-PROD), branche par branche. |
| « Tests unitaires obligatoires (services…) » | step `./mvnw -B test` + rapport JaCoCo archivé sur la VM (`akiwacu-artifacts/`). |
| « Couverture ≥ 80 % » | `jacoco:check` prêt derrière `COVERAGE_GATE` ; énoncer la mesure réelle (77,5 % au dernier handoff). |
| « Conteneurisation » | `api/Dockerfile` multi-stage + Compose + images publiées sur GHCR. |
| « Sécurité de l'image » | Trivy — CVE critiques bloquent le pipeline ; rapport exporté `trivy-report.json`. |
| « SonarQube » | étape conditionnelle `sonar:sonar` + `sonarqube-quality-gate-action` derrière `SONAR_GATE`. |
| « Monitoring (mesures techniques et métier) » | `/actuator/prometheus` → Prometheus → 2 dashboards Grafana versionnés en JSON. |
| « Les deux VM déployées » | jobs `deploy-vm-dev` / `deploy-vm-prod` avec vérification `/actuator/health` en 200. |

---

## 9. Les pièges et questions probables

- **« Pourquoi le runner est-il auto-hébergé ? »** → Les VM sont dans le réseau de
  l'université, non joignables de l'extérieur. Le runner/vm ouvre une connexion
  SORTANTE vers GitHub, ce qui évite d'ouvrir un port entrant.
- **« Pourquoi Trivy en conteneur avec `--skip-db-update` ? »** → Sur la liaison
  (16 Ko/s mesurés), retélécharger la base de 107 Mo fait échouer Trivy. La base est
  amorcée une fois sur la VM, rafraîchie hors CI. Limitation assumée : `--pkg-types os`
  n'analyse que les paquets système, pas les dépendances Java (trivy-java-db ~700 Mo). Voir `docs/DECISIONS.md` D-30.
- **« Que se passe-t-il si une VM tombe ? »** → Le job échoue, le déploiement
  précédent reste en place ; on diagnostique runner/SSH puis on relance.
- **« Que bloque l'absence de Sonar ? »** → Rien au pipeline tant que `SONAR_ENABLED`
  est false. C'est une étape prête mais désactivée pour ne pas bloquer l'équipe tant
  que la couverture n'est pas réelle.
- **« Et les captures ? »** → À prendre réellement le jour J : workflow vert,
  dashboards Grafana avec données, rapport JaCoCo index.html, rapport Trivy.
  NE JAMAIS afficher une capture inventée.

---

> **Prérequis démo** : VM dev + prod réglées (runner en ligne, compose en place),
> ou en local : `docker compose up -d` puis `curl http://localhost:8080/actuator/health`.