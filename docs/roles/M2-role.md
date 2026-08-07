# KLEIN — DevOps · QA Lead

**8 jours. Jeudi 6 → jeudi 13 août. Soutenance vendredi 14.**

---

## Ton rôle en une phrase

Tu construis la chaîne qui transforme un `git push` en une application qui tourne en
production — et tu es le garant de la qualité du code de toute l'équipe.

## Ce que tu portes

**Infrastructure :** les 4 VM Proxmox · le runner GitHub auto-hébergé · Docker ·
Ansible · Terraform (au D7)
**Chaîne CI/CD :** les 2 workflows GitHub Actions · GHCR · SonarQube + Quality Gate ·
Trivy · JaCoCo et le seuil bloquant
**Monitoring :** Actuator · Micrometer · Prometheus · Grafana (technique **et** métier)
**Domaines backend :** `transaction-caisse` · `remboursement` · `dashboard` — ~14 endpoints
**Règle métier :** **R5** — chaque opération financière enregistre le trésorier validateur

> **8 des 13 livrables de l'énoncé passent par toi.** C'est le deuxième poste le plus
> exigeant du projet après celui d'Andy, et c'est celui qui a le plus d'impact sur la
> note collective.

---

## Ton séquencement — ne l'inverse jamais

| Jour | Focus |
|---|---|
| **D1** | 4 VM créées à la main · Docker · **runner auto-hébergé enregistré** |
| **D2** | Dockerfile · docker-compose · **CI verte (build + test)** · Ansible de base |
| **D3** | SonarQube · JaCoCo 50 % · Trivy · GHCR · **déploiement automatique VM-DEV** |
| **D4** | Tes domaines : remboursement, caisse, R5 · seuil 60 % |
| **D5** | Prometheus · Grafana · métriques métier Micrometer · écrans dashboard |
| **D6** | **Seuil 80 %** · Quality Gate vert · **`main` → VM-PROD** |
| **D7** | Terraform (codifie l'existant) · captures Sonar/Trivy/Grafana **avec interprétation** |
| **D8** | Vérification des livrables · snapshot VM-PROD · répétition |

**Ne commence pas tes domaines métier avant que la CI soit verte.**
Si le pipeline n'est pas debout au soir du **D2**, dis-le fort au daily : toute
D3 s'ouvre par une mobilisation collective sur le pipeline. C'est la procédure prévue.

---

## Ta première heure

1. Ouvre **[`GUIDE-INFRA-PAS-A-PAS.md`](GUIDE-INFRA-PAS-A-PAS.md)** et remplis la fiche d'adresses §0
2. Fais les 4 VM (§1). ~2 h.
3. Demande le token de runner à Andy et installe-le. ~30 min.
4. **Snapshot des 4 VM.** 30 secondes.

---

## Ton risque n°1 — à traiter en D1

L'énoncé exige un déploiement **par GitHub Actions via SSH**. Les runners hébergés par
GitHub sont dans le cloud, vos VM sont derrière le NAT de l'université. Cette connexion
SSH **ne peut pas fonctionner**.

Le runner auto-hébergé résout le problème : connexion sortante vers GitHub, aucun port
entrant à ouvrir, et les jobs s'exécutent à l'intérieur du LAN.

C'est le point exact où les autres groupes se bloqueront au jour 5. Installe-le
**en D1**, et garde l'explication : elle vaut deux paragraphes dans ton chapitre
du rapport et c'est une des meilleures questions que l'enseignant puisse te poser.

---

## Ton coup de levier — le ratchet de couverture

Tu pilotes le seuil JaCoCo, et tu ne le baisses **jamais** :

| Jour | Seuil bloquant |
|---|---|
| D3 | 50 % |
| D4 | 60 % |
| D5 | 70 % |
| **D6** | **80 %** ← exigence de l'énoncé |

La panique de couverture en fin de projet est le mode d'échec le plus courant sur ce
type de TP. Le ratchet l'empêche mécaniquement : personne ne peut merger du code qui
fait régresser la couverture.

Deuxième levier, tout aussi important : **distribuer SonarLint en mode connecté à toute
l'équipe au daily du D3.** Dix minutes. Sans ça, les trois développeurs découvriront
200 issues Sonar au D6.

---

## Ton outillage

**Claude Code** est ton outil principal : workflows GitHub Actions, débogage de pipeline
(colle le log qui échoue, tu gagnes des heures), playbooks Ansible, HCL Terraform,
requêtes PromQL pour Grafana.

**Claude dans Chrome** pour la phase captures du D7 : Grafana, SonarQube, Trivy.

Mets ceci dans `~/.claude/CLAUDE.md` sur ta machine :

```markdown
Je suis Klein de Guy Mugisha (@Gkcoding-prog), DevOps et QA Lead du projet Akiwacu
(plateforme de gestion de tontines, TP Frameworks JEE + API REST, UPG Gitega,
soutenance le 14 août 2026).

Je possède : toute l'infrastructure (4 VM Proxmox, runner GitHub auto-hébergé,
Docker, Ansible, Terraform), la chaîne CI/CD (2 workflows, GHCR, SonarQube,
Trivy, JaCoCo), le monitoring (Prometheus, Grafana, Micrometer), et les domaines
backend transaction-caisse, remboursement et dashboard.
Ma règle métier : R5 — chaque opération financière enregistre le trésorier validateur.

Contexte réseau important : nos VM sont derrière le NAT de l'université, sans IP
publique. On utilise un runner GitHub auto-hébergé sur VM-CI. Ne me propose jamais
une solution qui suppose que GitHub peut joindre nos machines depuis Internet.

Au début de chaque session, lis : CLAUDE.md, docs/PLANNING-8-JOURS.md,
docs/roles/M2-*.md.

Nous avons 8 jours. Quand tu me proposes une approche, privilégie ce qui marche
vite et se défend à l'oral plutôt que ce qui est le plus élégant.
```

---

## Tes métriques métier — ne les oublie pas

L'énoncé §10 exige que Grafana montre **des métriques techniques ET des indicateurs
métier**. Les techniques sortent gratuitement d'Actuator. Les métier, tu dois les
instrumenter à la main avec Micrometer :

`akiwacu_membres_total` · `akiwacu_cotisations_montant_total` ·
`akiwacu_prets_montant_total` · `akiwacu_prets_refuses_total` (par motif : R3, R6, R7 —
très parlant en démo) · `akiwacu_remboursements_montant_total` · `akiwacu_solde_caisse`

Tague tout par `tontine` pour pouvoir filtrer.
**Livrable = le JSON exporté des dashboards**, pas la capture d'écran.

---

## Ce que tu montres à la soutenance

1. Un `git push` qui déclenche le pipeline complet **en direct**
2. Le Quality Gate SonarQube et ce qu'il bloque
3. Le rapport Trivy avec **3 CVE commentées** : corrigée / acceptée, et pourquoi
4. Les 2 dashboards Grafana, technique et métier
5. `terraform plan` qui affiche « No changes » — et tu expliques la réconciliation d'état
6. **Pourquoi un runner auto-hébergé** — c'est ta meilleure réponse du projet

**Questions à préparer :**
- Pourquoi le déploiement n'a-t-il pas lieu si Trivy trouve un CVE critique ? Montre.
- Que se passe-t-il si le Quality Gate échoue sur une PR ?
- Pourquoi le Quality Gate porte sur le code neuf et pas le code global ?
- Quelle est la différence entre ton image Docker de build et celle d'exécution ?
- Si VM-PROD tombe pendant un déploiement, que se passe-t-il ?

---

## Ton pack

| Fichier | Quand |
|---|---|
| [`INSTALLER-MON-PACK.md`](INSTALLER-MON-PACK.md) | **En premier.** Pack + dépôt côte à côte, Obsidian, Claude Code. |
| **[`GUIDE-INFRA-PAS-A-PAS.md`](GUIDE-INFRA-PAS-A-PAS.md)** | **Maintenant.** VM, runner, Ansible, Terraform, de A à Z. |
| [`MES-TACHES.md`](MES-TACHES.md) | Chaque matin. D1 → D8. |
| [`MA-SOUTENANCE.md`](MA-SOUTENANCE.md) | D7–D8. |
| [`Akiwacu/docs/GUIDE-GIT-GITHUB.md`](../Akiwacu/docs/GUIDE-GIT-GITHUB.md) | La boucle de travail quotidienne. |
| [`Akiwacu/docs/GUIDE-JIRA-SCRUM.md`](../Akiwacu/docs/GUIDE-JIRA-SCRUM.md) | Partie A. |
