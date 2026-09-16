# `docs/` — la documentation du projet Akiwacu

Tout ce dossier est versionné dans le dépôt. Toute modification passe par une PR,
comme le code.

---

## Par situation

| Tu veux… | Ouvre |
|---|---|
| savoir sur **quelle machine** taper une commande | [`CARTE-DES-MACHINES.md`](CARTE-DES-MACHINES.md) |
| savoir **quoi faire aujourd'hui** | [`PLANNING-8-JOURS.md`](PLANNING-8-JOURS.md) puis ton `roles/M<n>-role.md` |
| **configurer ton poste** | [`GUIDE-SETUP-POSTE.md`](GUIDE-SETUP-POSTE.md) |
| ouvrir une branche, un commit, une PR | [`GUIDE-GIT-GITHUB.md`](GUIDE-GIT-GITHUB.md) |
| **démarrer ta première PR** — règles de travail en parallèle | [`PREMIERE-PR.md`](PREMIERE-PR.md) |
| prendre un ticket, tenir un daily | [`GUIDE-JIRA-SCRUM.md`](GUIDE-JIRA-SCRUM.md) |
| écrire une **entité**, une migration, une relation | [`MODELE-DE-DONNEES.md`](MODELE-DE-DONNEES.md) |
| implémenter une **règle métier** | [`MATRICE-REGLES-METIER.md`](MATRICE-REGLES-METIER.md) |
| comprendre **pourquoi** une chose a été décidée ainsi | [`DECISIONS.md`](DECISIONS.md) |
| **remonter le projet depuis zéro** | [`SETUP-DEPUIS-ZERO.md`](SETUP-DEPUIS-ZERO.md) |
| l'étape Trivy échoue, ou la base est à rafraîchir | [`RUNBOOK-TRIVY.md`](RUNBOOK-TRIVY.md) |
| savoir **quel secret existe, où et qui le pose** | [`INVENTAIRE-SECRETS.md`](INVENTAIRE-SECRETS.md) |
| **construire le client React** — direction visuelle, écrans, états | [`GUIDE-CLIENT-REACT.md`](GUIDE-CLIENT-REACT.md) |
| rédiger ton chapitre du rapport | [`LIVRABLES.md`](LIVRABLES.md) |
| préparer une réponse de soutenance | [`DECISIONS.md`](DECISIONS.md) + les deux ADR |

## Les deux ADR

Ce sont les décisions d'architecture qui se défendent devant le jury. Elles se
recopient presque telles quelles dans le chapitre 3 du rapport.

- [`ADR-001-CLIENT-WEB-PWA.md`](ADR-001-CLIENT-WEB-PWA.md) — pourquoi un client web
  responsive plutôt qu'une application native
- [`ADR-002-RUNNER-AUTO-HEBERGE.md`](ADR-002-RUNNER-AUTO-HEBERGE.md) — pourquoi un
  runner GitHub Actions sur nos propres machines

## Les fiches de rôle

`roles/M1-role.md` … `roles/M5-role.md` — une par personne. C'est le fichier que ton
agent Claude Code lit au démarrage de chaque session.

| | Nom | Domaine |
|---|---|---|
| M1 | Andy | `auth` `utilisateur` `membre` — R1 R5 R8 |
| M2 | Klein | `caisse` `remboursement` `dashboard` — R5 · DevOps et QA |
| M3 | Juste | `tontine` `adhesion` `recu` |
| M4 | Benitha | `cycle` `cotisation` — R2 R3 |
| M5 | Gloria | `demandepret` `vote` `pret` — R4 R6 R7 |

---

## Les trois documents à lire une fois, en entier

1. **`CARTE-DES-MACHINES.md`** — il répond à « je tape ça où ? ». Cinq minutes, et
   elles font gagner une heure.
2. **`PLANNING-8-JOURS.md`** — les jalons bloquants et la Definition of Done.
3. **Ta fiche de rôle** — ton périmètre, tes règles, tes noms de tests imposés.

Le reste se consulte au besoin.

## Ce qui ne vit pas ici

| Quoi | Où |
|---|---|
| Le rapport technique, les comptes rendus, les rétrospectives | **Confluence** — cinq personnes écrivant de la prose dans Git produisent des conflits sur des paragraphes |
| Les tickets, sprints, burndown | **Jira**, projet `AKW` |
| Les conventions de code et de test | `CLAUDE.md` à la racine du dépôt |
| Les artefacts à déposer | `livrables/` |
