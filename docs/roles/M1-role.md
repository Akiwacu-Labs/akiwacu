# ANDY — Chef de projet · Architecte · Sécurité

**8 jours. Jeudi 6 → jeudi 13 août. Soutenance vendredi 14.**

---

## Ton rôle en une phrase

Tu poses le socle sur lequel les quatre autres construisent, tu gardes la sécurité et
l'architecture, et tu fais en sorte que personne ne soit jamais bloqué plus d'une heure.

## Ton domaine technique

`auth` · `utilisateur` · `membre` — environ 16 endpoints

**Tes règles métier et leurs tests obligatoires :**

| | Règle | Test obligatoire |
|---|---|---|
| **R1** | Isolation totale des données par tontine | `shouldNotAccessDataFromAnotherTontine()` |
| **R5** | Audit : traçabilité du validateur sur chaque opération | `shouldRecordValidatingTreasurer()` (avec Klein) |
| **R8** | Opération avec reçu → non modifiable ni supprimable | `shouldRejectModificationOfReceiptedOperation()` |

Les noms de tests sont **imposés** : ce sont eux qui prouvent la conformité aux règles
de l'énoncé, et tu les montreras à la soutenance.

## Fichiers dont tu es SEUL propriétaire

Personne d'autre ne les touche. C'est ce qui évite 90 % des conflits de merge.

```
config/SecurityConfig.java      common/GlobalExceptionHandler.java
config/OpenApiConfig.java       common/TenantContext.java
src/main/resources/application*.yml
src/main/resources/db/migration/    (Flyway)
pom.xml       CLAUDE.md       .github/
```

---

## Ta première heure, dans cet ordre

1. **[`RUNBOOK-GITHUB-CLI.md`](RUNBOOK-GITHUB-CLI.md)** — organisation, dépôt public, invitations, protections, secrets. ~25 min.
2. Créer le site Jira + Confluence, importer `02-BACKLOG-JIRA.csv`, créer les 3 sprints. ~20 min.
3. Envoyer à chacun son pack + le lien du dépôt.
4. **Sprint planning, 30 minutes.** Objectif du Sprint 1 : *« un push sur develop se déploie tout seul sur VM-DEV »*.
5. Puis tu codes : **la PR socle**.

---

## Ton coup de levier n°1 — à livrer en D1

> **Une seule PR contenant les 13 entités JPA + les migrations Flyway + les repositories vides.**

Tant qu'elle n'est pas mergée, quatre personnes attendent ou divergent sur le schéma.
Après, elles travaillent en parallèle sans se marcher dessus.

Sur 8 jours, c'est la décision qui a le plus d'impact. Fais-la avant de faire quoi que
ce soit d'autre côté code.

Les 13 entités : `Utilisateur` `Membre` `Tontine` `Cycle` `Adhesion` `Cotisation`
`DemandePret` `VoteCommissaire` `Pret` `Remboursement` `TransactionCaisse` `Recu` + `BaseEntity`.

---

## Ton rythme sur 8 jours

| Jour | Ton temps |
|---|---|
| **D1–D2** | 80 % code (socle, sécurité), 20 % pilotage |
| **D3–D5** | 50 % code, 50 % revue et déblocage |
| **D6–D8** | 20 % code, 80 % rapport, pilotage, répétition |

**Ton piège :** tu vas vouloir tout coder. Ne le fais pas. À partir du D4, si tu passes
plus de la moitié de ta journée dans l'IDE, quelqu'un est bloqué sans que tu le saches.
À partir du D4, ton livrable principal, ce sont **les PR mergées par les autres**.

---

## Ce que tu surveilles chaque matin, avant le daily

```bash
gh pr list --search "review-requested:@me"    # ce qui m'attend
gh run list --limit 5                          # la CI est-elle verte ?
gh pr list --state open --json number,title,author,createdAt
```

Quatre signaux d'alarme :
- une PR ouverte depuis plus de 3 h sans revue
- un ticket Jira en « In Progress » depuis plus de 24 h
- quelqu'un qui n'a rien mergé depuis 24 h
- quelqu'un qui dit « bloqué » sur le même sujet deux jours de suite → **tu prends le sujet le jour même**

---

## Ton outillage — plan Pro

**Cowork, c'est ton cockpit.** Jira et Confluence connectés, calendrier, assemblage du
rapport, brief du matin. Tu pilotes depuis là.

**Claude Code, pour deux choses seulement :** le socle du D1 (entités, sécurité,
gestion des erreurs) et la relecture des PR des autres. Résiste à l'envie de coder des
fonctionnalités après le D4.

**Figma : 45 minutes maximum**, et uniquement l'écran de saisie rapide des cotisations —
c'est celui qui porte la démo. Tout le reste utilise shadcn/ui par défaut. À 14 jours,
4 h de design était un bon investissement ; à 8 jours, c'est 5 % du projet pour zéro
point noté.

Mets ceci dans `~/.claude/CLAUDE.md` sur ta machine :

```markdown
Je suis Andy Miguel Habyarimana (@miguelandy875), chef de projet, architecte et
responsable sécurité du projet Akiwacu (plateforme de gestion de tontines,
TP Frameworks JEE + API REST, UPG Gitega, soutenance le 14 août 2026).

Je possède : auth, utilisateur, membre — et tout le socle transverse
(SecurityConfig, GlobalExceptionHandler, OpenApiConfig, TenantContext,
migrations Flyway, pom.xml).
Mes règles métier : R1 (isolation par tontine), R5 (audit), R8 (verrouillage après reçu).

Au début de chaque session, lis : CLAUDE.md, docs/PLANNING-8-JOURS.md,
docs/roles/M1-*.md.

Je suis aussi chef de projet : quand je te demande un arbitrage, tiens compte du
délai de 8 jours. Signale-moi ce qui ne tiendra pas dans le temps imparti.
```

---

## Ce que tu montres à la soutenance

1. L'authentification JWT en direct sur VM-PROD
2. Le filtre multi-tenant **R1** dans le code, et pourquoi `tontineId` vient du JWT et jamais du body
3. Le verrouillage **R8** : tentative de modification d'une cotisation avec reçu → 409
4. L'architecture globale et les deux ADR (client PWA, runner auto-hébergé)
5. Le pipeline de bout en bout

**Questions à préparer :**
- Comment garantis-tu qu'une tontine ne voit jamais les données d'une autre ? Montre le code.
- Que se passe-t-il si deux trésoriers enregistrent la même cotisation en même temps ?
- Pourquoi cette règle est-elle dans le service et pas dans le contrôleur ?
- Pourquoi un dépôt public sans licence ?
- Pourquoi un runner auto-hébergé plutôt qu'un runner GitHub ?

---

## Ton pack

| Fichier | Quand |
|---|---|
| [`INSTALLER-MON-PACK.md`](INSTALLER-MON-PACK.md) | **En premier.** Pack + dépôt côte à côte, Obsidian, Claude Code. |
| [`RUNBOOK-GITHUB-CLI.md`](RUNBOOK-GITHUB-CLI.md) | **Maintenant.** Création du dépôt de A à Z. |
| [`MES-TACHES.md`](MES-TACHES.md) | Chaque matin. Tes tâches D1 → D8. |
| [`MA-SOUTENANCE.md`](MA-SOUTENANCE.md) | D7–D8. Ton script de 5 minutes. |
| [`Akiwacu/docs/PLANNING-8-JOURS.md`](../Akiwacu/docs/PLANNING-8-JOURS.md) | Le plan maître. Tu l'as écrit, tiens-le. |
| [`Akiwacu/docs/GUIDE-JIRA-SCRUM.md`](../Akiwacu/docs/GUIDE-JIRA-SCRUM.md) | Partie C : ce que tu configures. Partie D : tes indicateurs. |
