# JUSTE — Tontines, Adhésions, Reçus PDF

**8 jours. Jeudi 6 → jeudi 13 août. Soutenance vendredi 14.**

---

## Ton domaine

`tontine` · `adhesion` · `recu` — environ 15 endpoints

## Ta pièce maîtresse

> **Le service de génération de reçus PDF.**

Trois personnes en dépendent :
- **Benitha** — reçu de cotisation
- **Gloria** — reçu de prêt débloqué
- **Klein** — reçu de remboursement

⚠ **Date limite : D4, dimanche 9 août.**
Si le service prend du retard, le signaler au daily du **D3** — pas en fin de D4.
Trois personnes attendent, et sur 8 jours il n'y a aucune marge de rattrapage.

---

## Ce que chaque reçu doit contenir (énoncé §9, non négociable)

| Champ | D'où il vient |
|---|---|
| Numéro du reçu | séquence, unique par tontine et par année |
| Date | date de l'opération |
| Tontine | nom de la tontine |
| Membre | nom complet du membre |
| Montant | en BIF, formaté avec séparateurs de milliers |
| Type d'opération | COTISATION · PRET_DEBLOQUE · REMBOURSEMENT |
| **Trésorier ayant validé** | depuis le `SecurityContext` (règle R5) |

**Bibliothèque : OpenPDF** (fork libre d'iText, licence LGPL — pas de souci en académique).

```xml
<dependency>
  <groupId>com.github.librepdf</groupId>
  <artifactId>openpdf</artifactId>
  <version>2.0.3</version>
</dependency>
```

**Fais-le moche mais fonctionnel d'abord.** Une page, un en-tête, un tableau, les 7 champs.
Tu embelliras au D6 si tu as le temps. Un reçu soigné livré en D5 ne sert à rien ;
un reçu fonctionnel livré en D4 débloque trois personnes.

---

## Ton binôme

**Andy**, en rapproché toute la première moitié du projet. Tu n'as pas à te débrouiller
seule. **Si tu bloques plus de 30 minutes, tu l'appelles.** C'est prévu dans le plan.

---

## Ta première heure (D1)

1. Configure ton poste — [`Akiwacu/docs/GUIDE-SETUP-POSTE.md`](../Akiwacu/docs/GUIDE-SETUP-POSTE.md)
2. Clone le dépôt, `docker compose up -d`, vérifie que l'appli démarre
3. **Lis l'énoncé du TP en entier** (le PDF). Tu es responsable du chapitre « Conception
   de la base » du rapport : commence le brouillon du MCD en D1.
4. Premier commit, poussé

---

## Tes 8 jours

| Jour | Ce que tu livres |
|---|---|
| **D1** | Poste prêt · appli locale · énoncé lu · brouillon du MCD · 1er commit |
| **D2** | Package `tontine` · `GET /api/tontines` répond |
| **D3** | CRUD tontine + adhésion complet, avec tests · **démarrer le service de reçus** |
| **D4** | ⚠ **SERVICE DE REÇUS PDF LIVRÉ** — trois personnes en dépendent |
| **D5** | Écrans client : tontines, adhésions, téléchargement de reçu |
| **D6** | Couverture ≥ 80 % sur tes packages |
| **D7** | Chapitre « Conception de la base » : MCD, MLD, diagramme de classes |
| **D8** | Répétition de ta démo |

Le CRUD tontine est la partie la moins complexe de ton périmètre. Prends de l'avance
dessus pour sécuriser le service PDF, qui est la dépendance de trois personnes.

---

## Ton outillage

`/nouveau-domaine tontine` puis `/tests TontineService` — la structure complète selon
nos conventions, en une commande.

`/explique` **avant chaque daily**. Tu dois pouvoir défendre chaque ligne.

Mets ceci dans `~/.claude/CLAUDE.md` sur ta machine :

```markdown
Je suis Juste Daxa Ayikunde (@daxa257), développeuse sur le projet Akiwacu
(plateforme de gestion de tontines, TP Frameworks JEE + API REST, UPG Gitega,
soutenance le 14 août 2026).

Je possède les domaines : tontine, adhesion, recu (~15 endpoints).

Ma pièce maîtresse est le service partagé de génération de reçus PDF (OpenPDF),
utilisé par trois autres membres de l'équipe : cotisation (Benitha), prêt débloqué
(Gloria), remboursement (Klein). Chaque reçu doit porter 7 champs obligatoires :
numéro, date, tontine, membre, montant, type d'opération, trésorier validateur.
Date limite : D4, dimanche 9 août.

Je suis aussi responsable du chapitre "Conception de la base de données" du
rapport : MCD, MLD, diagramme de classes.

Au début de chaque session, lis : CLAUDE.md, docs/PLANNING-8-JOURS.md,
docs/roles/M3-*.md.

IMPORTANT : je dois pouvoir expliquer chaque ligne à l'oral devant mon enseignant.
Code lisible plutôt que malin, commentaires en français, et quand tu génères un
test explique-moi en 3 lignes ce qu'il vérifie et quel bug il attraperait.
```

---

## La règle sur l'IA

> L'IA écrit le premier jet. **Toi, tu possèdes l'explication.**

Tu relis ligne par ligne et tu ajoutes 2-3 lignes de commentaire **avec tes propres
mots**. Si tu ne peux pas l'expliquer au daily du lendemain, tu le réécris.

---

## Tes dépendances

| Tu dépends de | Pour quoi | Quand |
|---|---|---|
| **Andy** | entités JPA, `TenantContext` (R1), exceptions | D1 |
| **Andy** | `SecurityContext` pour récupérer le trésorier validateur (R5) | D2 |

| Qui dépend de toi | Pour quoi | Quand |
|---|---|---|
| **Benitha** | reçu de cotisation | **D4** |
| **Gloria** | reçu de prêt débloqué | **D4** |
| **Klein** | reçu de remboursement | **D4** |

Tu es sur le chemin critique de trois personnes au D4. C'est la seule journée du projet
où ton retard coûterait plus cher que celui de n'importe qui d'autre.

---

## Ce que tu montres à la soutenance

1. La création d'une tontine, l'adhésion d'un membre
2. **La génération d'un reçu PDF en direct** — tu enregistres une cotisation, le PDF
   s'ouvre avec les 7 champs. C'est visuel, concret, et tout le monde comprend
   immédiatement ce que ça apporte par rapport au registre papier.
3. Ton MCD, et pourquoi tu as modélisé l'adhésion comme une entité à part entière plutôt
   qu'une simple relation

**Questions à préparer :**
- Comment garantis-tu l'unicité du numéro de reçu ? Et si deux reçus sont générés en même temps ?
- Pourquoi l'adhésion est-elle une entité et pas juste une table de jointure ?
- Que se passe-t-il si on tente de générer deux fois le reçu de la même cotisation ?
- Comment ton service est-il appelé par les trois autres domaines ?
- Pourquoi cette règle est-elle dans le service et pas dans le contrôleur ?

---

## Ton pack

| Fichier | Quand |
|---|---|
| [`Akiwacu/docs/CARTE-DES-MACHINES.md`](../Akiwacu/docs/CARTE-DES-MACHINES.md) | **Lis-la une fois.** Où lancer quelle commande. |
| [`Akiwacu/docs/DECISIONS.md`](../Akiwacu/docs/DECISIONS.md) | Avant la soutenance. Ce qui a été décidé et pourquoi — les réponses aux questions du jury. |
| [`Akiwacu/docs/MATRICE-REGLES-METIER.md`](../Akiwacu/docs/MATRICE-REGLES-METIER.md) | Avant d'implémenter une règle. Propriétaires, noms de tests imposés, pièges. |
| [`INSTALLER-MON-PACK.md`](INSTALLER-MON-PACK.md) | **En premier.** Pack + dépôt côte à côte, Obsidian, Claude Code. |
| **[`SPEC-RECUS.md`](SPEC-RECUS.md)** | **D1.** Le contrat du service PDF. |
| [`MES-TACHES.md`](MES-TACHES.md) | Chaque matin. |
| [`MA-SOUTENANCE.md`](MA-SOUTENANCE.md) | D7–D8. |
| [`Akiwacu/docs/GUIDE-SETUP-POSTE.md`](../Akiwacu/docs/GUIDE-SETUP-POSTE.md) | D1, avant tout. |
| [`Akiwacu/docs/GUIDE-GIT-GITHUB.md`](../Akiwacu/docs/GUIDE-GIT-GITHUB.md) | La boucle de travail, à connaître par cœur. |
| [`Akiwacu/docs/GUIDE-JIRA-SCRUM.md`](../Akiwacu/docs/GUIDE-JIRA-SCRUM.md) | Partie A. |
