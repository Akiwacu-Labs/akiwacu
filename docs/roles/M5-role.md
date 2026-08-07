# GLORIA — Prêts, Demandes, Votes

**8 jours. Jeudi 6 → jeudi 13 août. Soutenance vendredi 14.**

---

## Ton domaine

`demandepret` · `vote` · `pret` — environ 16 endpoints

## Tes règles métier — les plus difficiles du projet

| | Règle | Test obligatoire |
|---|---|---|
| **R4** | Un prêt doit être approuvé par au moins **deux commissaires distincts** | `shouldRejectApprovalFromSameCommissionerTwice()` |
| **R6** | Le montant d'un prêt ne peut dépasser **trois fois l'épargne** du membre | `shouldRejectLoanExceedingThreeTimesSavings()` |
| **R7** | L'échéance d'un prêt ne peut dépasser **la fin du cycle** | `shouldRejectDueDateAfterCycleEnd()` |

---

## Lis ça avant tout le reste

On t'a confié le domaine le plus exigeant du projet **volontairement**, et avec un filet.

Trois des huit règles métier sont chez toi. C'est là qu'est la vraie difficulté du sujet,
et c'est donc là que l'enseignant posera le plus de questions. Le workflow de prêt est
aussi la démonstration la plus complète du projet.

Le filet, concrètement :

> **Andy a écrit les spécifications en Gherkin et les squelettes de tests.**
> Ils sont dans [`SPECS-GHERKIN.md`](SPECS-GHERKIN.md) et [`TESTS-A-COMPLETER.md`](TESTS-A-COMPLETER.md) de ton pack.
> Toi, tu implémentes le corps des règles et des tests.

Tu apprends sur du vrai code métier, encadré. Tu n'es jamais devant une page blanche.

**Andy est ton binôme sur les règles. Si tu bloques 30 minutes, tu l'appelles.**
C'est prévu dans le plan, ce n'est pas un aveu de faiblesse.

---

## Les pièges — anticipe-les maintenant

### R4 — deux commissaires *distincts*

- Deux votes du **même** commissaire ne comptent pas pour deux. Mets une **contrainte
  unique en base** sur `(demande_id, commissaire_id)`, pas seulement un test en Java.
  Un contrôle applicatif seul se contourne en cas d'accès concurrent.
- Un vote négatif ne s'annule pas et ne se remplace pas silencieusement.
- Le décompte porte sur les commissaires **de la tontine concernée** (croisement avec R1).
  Un commissaire d'une autre tontine ne doit jamais pouvoir voter.

### R6 — définis « l'épargne » et écris-le

« Trois fois l'épargne du membre » : trois fois **quoi**, exactement ?

- la somme de ses cotisations sur le **cycle actif** ?
- sur tout son historique ?
- nette des prêts en cours ?

**L'enseignant te posera exactement cette question.** Choisis, justifie, et écris ta
définition dans le `README.md` de ton package. Une réponse ferme et assumée vaut
beaucoup mieux qu'une réponse floue, même si le choix est discutable.

*(Recommandation : somme des cotisations du membre sur le cycle actif, nette du capital
restant dû sur ses prêts en cours. C'est le plus proche de la réalité d'une tontine —
tu ne peux pas emprunter trois fois une épargne que tu as déjà nantie.)*

### R7 — attention au gel de cycle

Que se passe-t-il si le cycle est gelé **pendant** qu'un prêt est en cours d'approbation ?
Question classique. Réponds : la vérification R3 se fait **au moment du déblocage**, pas
seulement à la demande.

---

## Ta première heure (D1)

1. Configure ton poste — [`Akiwacu/docs/GUIDE-SETUP-POSTE.md`](../Akiwacu/docs/GUIDE-SETUP-POSTE.md)
2. Clone le dépôt, `docker compose up -d`, vérifie que l'appli démarre en local
3. **Lis [`SPECS-GHERKIN.md`](SPECS-GHERKIN.md) en entier.** C'est le métier que tu vas coder pendant 8 jours.
4. Fais ton premier commit (même un `README.md` de package) et pousse-le

---

## Tes 8 jours

| Jour | Ce que tu livres |
|---|---|
| **D1** | Poste prêt · appli locale qui tourne · specs lues · 1er commit |
| **D2** | Package `demandepret` · `GET /api/demandes-pret` répond |
| **D3** | CRUD demande + vote + prêt complet, avec leurs tests |
| **D4** | **R4, R6, R7** implémentées, tests nommés au vert |
| **D5** | Écrans client : demande de prêt, vote commissaire, détail et échéancier |
| **D6** | Couverture ≥ 80 % sur tes packages · écrans finalisés |
| **D7** | Chapitre « Règles métier » du rapport + captures Swagger |
| **D8** | Répétition de ta démo |

**Rythme cible : 2 stories par jour.** En dessous deux jours de suite, tu le dis au daily.
Ce n'est pas un reproche, c'est le mécanisme d'alerte.

---

## Ton outillage

`/regle R4`, `/regle R6`, `/regle R7` — la commande te fera d'abord reformuler la règle
en Gherkin et attendra ta validation **avant** de coder. C'est fait exprès : c'est comme
ça que tu apprends le domaine au lieu de le recevoir tout fait.

`/explique` **avant chaque daily**. Tu dois pouvoir défendre chaque ligne.

Mets ceci dans `~/.claude/CLAUDE.md` sur ta machine :

```markdown
Je suis Gloria Muhimpundu (@muhimpundugloria), développeuse sur le projet Akiwacu
(plateforme de gestion de tontines, TP Frameworks JEE + API REST, UPG Gitega,
soutenance le 14 août 2026).

Je possède les domaines : demandepret, vote, pret (~16 endpoints).
Mes règles métier : R4 (deux commissaires distincts), R6 (prêt ≤ 3× l'épargne),
R7 (échéance ≤ fin du cycle). Ce sont les règles les plus difficiles du projet.

Au début de chaque session, lis : CLAUDE.md, docs/PLANNING-8-JOURS.md,
docs/roles/M5-*.md, et mes specs Gherkin.

IMPORTANT : je dois pouvoir expliquer chaque ligne de ce code à l'oral devant mon
enseignant. Écris du code lisible plutôt que malin, commente en français ce qui
n'est pas évident, et quand tu génères un test, explique-moi en 3 lignes ce qu'il
vérifie et quel bug il attraperait. Ne me propose pas d'abstraction que je ne
saurais pas justifier.
```

---

## La règle sur l'IA

L'énoncé autorise l'IA pour une première version des tests, **mais tu restes
responsable de ta capacité à expliquer chaque test à la soutenance**.

> L'IA écrit le premier jet. **Toi, tu possèdes l'explication.**

Après toute génération : tu relis ligne par ligne et tu ajoutes 2-3 lignes de
commentaire **avec tes propres mots**. Si tu ne peux pas l'expliquer au daily du
lendemain, tu le réécris.

---

## Ce que tu montres à la soutenance

**Le workflow complet, en direct :**

demande de prêt → vote du commissaire 1 → **tentative d'un second vote par le même
commissaire, refusée (R4)** → vote du commissaire 2 → prêt approuvé → déblocage → reçu PDF

Puis une demande de prêt trop élevée refusée par **R6**, avec le test à l'appui.

C'est la démonstration la plus marquante du groupe. Prépare-la et répète-la.

**Questions à préparer :**
- Comment garantis-tu que deux votes viennent bien de commissaires différents ?
- Qu'est-ce que « l'épargne » exactement, dans ton calcul R6 ? Pourquoi ce choix ?
- Si je gèle un cycle pendant qu'un prêt est en approbation, que se passe-t-il ?
- Pourquoi cette règle est-elle dans le service et pas dans le contrôleur ?
- Ce test — qu'est-ce qu'il vérifie, et quel bug attraperait-il ?

---

## Ton pack

| Fichier | Quand |
|---|---|
| [`INSTALLER-MON-PACK.md`](INSTALLER-MON-PACK.md) | **En premier.** Pack + dépôt côte à côte, Obsidian, Claude Code. |
| **[`SPECS-GHERKIN.md`](SPECS-GHERKIN.md)** | **D1.** Le métier que tu vas coder. |
| **[`TESTS-A-COMPLETER.md`](TESTS-A-COMPLETER.md)** | D3–D4. Squelettes de tests à remplir. |
| [`MES-TACHES.md`](MES-TACHES.md) | Chaque matin. |
| [`MA-SOUTENANCE.md`](MA-SOUTENANCE.md) | D7–D8. |
| [`Akiwacu/docs/GUIDE-SETUP-POSTE.md`](../Akiwacu/docs/GUIDE-SETUP-POSTE.md) | D1, avant tout. |
| [`Akiwacu/docs/GUIDE-GIT-GITHUB.md`](../Akiwacu/docs/GUIDE-GIT-GITHUB.md) | La boucle de travail, à connaître par cœur. |
| [`Akiwacu/docs/GUIDE-JIRA-SCRUM.md`](../Akiwacu/docs/GUIDE-JIRA-SCRUM.md) | Partie A. |
