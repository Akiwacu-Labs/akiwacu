# BENITHA — Cycles, Cotisations

**8 jours. Jeudi 6 → jeudi 13 août. Soutenance vendredi 14.**

---

## Ton domaine

`cycle` · `cotisation` — environ 14 endpoints

## Tes règles métier

| | Règle | Test obligatoire |
|---|---|---|
| **R2** | Toute opération financière appartient à un **cycle actif** | `shouldRejectOperationOnInactiveCycle()` |
| **R3** | Aucun prêt lorsqu'un cycle est **gelé ou clôturé** | `shouldRejectLoanWhenCycleFrozen()` |

Tu implémentes la **machine à états du cycle** : `OUVERT → GELE → CLOTURE`

---

## Ta position est structurante — lis ça d'abord

R2 est vérifiée par **presque tous les autres domaines** : les cotisations, les prêts de
Gloria, les remboursements et la caisse de Klein. Tu es sur le chemin critique de trois
personnes.

Concrètement, tu dois publier une garde propre et documentée :

```java
CycleGuardService.assertCycleActif(tontineId);
```

> **Publie-la au D3 et annonce-le au daily.** Tout le monde l'appellera.
>
> Si elle arrive tard ou si elle est mal faite, trois domaines la contourneront, R2 sera
> violée partout, et ça se verra immédiatement à la soutenance quand l'enseignant gèlera
> un cycle et tentera une opération.

Ce que la garde doit faire :

```java
@Service
@RequiredArgsConstructor
public class CycleGuardService {

    private final CycleRepository cycleRepository;

    /**
     * Vérifie qu'un cycle OUVERT existe pour cette tontine.
     * @throws RegleMetierException (HTTP 409) si aucun cycle actif — R2
     */
    public Cycle assertCycleActif(Long tontineId) {
        return cycleRepository.findByTontineIdAndStatut(tontineId, StatutCycle.OUVERT)
            .orElseThrow(() -> new RegleMetierException(
                "R2 : aucun cycle actif pour cette tontine, l'opération est impossible"));
    }
}
```

---

## Ton écran phare — celui qu'on montre en démo

> **La saisie rapide des cotisations.**

Le trésorier est en réunion. Il a 40 membres devant lui, en liquide. Il doit aller vite.

Ce que l'écran doit faire :
- la liste des membres du cycle, déjà chargée
- le montant de cotisation **pré-rempli** depuis le cycle
- **un tap = enregistré**, pas trois clics et une confirmation
- le total qui s'incrémente en direct, visible en permanence
- le reçu généré automatiquement
- **pensé pour un téléphone tenu à une main** — les boutons en bas de l'écran, pas en haut

C'est l'écran qui prouve que l'équipe a compris **le métier**, et pas seulement
comment écrire une API REST. C'est celui que l'enseignant retiendra.

Andy a 45 minutes de maquette Figma prévues, et elles sont pour cet écran. Demande-la-lui.

---

## Ta première heure (D1)

1. Configure ton poste — [`Akiwacu/docs/GUIDE-SETUP-POSTE.md`](../Akiwacu/docs/GUIDE-SETUP-POSTE.md)
2. Clone le dépôt, `docker compose up -d`, vérifie que l'appli démarre
3. **Dessine la machine à états du cycle sur papier** : quels états, quelles transitions,
   qui a le droit de les déclencher. Vingt minutes avec un stylo t'éviteront trois heures
   de code confus.
4. Premier commit, poussé

---

## Tes 8 jours

| Jour | Ce que tu livres |
|---|---|
| **D1** | Poste prêt · appli locale · machine à états dessinée · 1er commit |
| **D2** | Package `cycle` · `GET /api/cycles` répond |
| **D3** | CRUD cycle + cotisation · **`CycleGuardService` publié et annoncé** ⚠ |
| **D4** | **R2 et R3** · machine à états complète · reçu de cotisation branché |
| **D5** | **Écran de saisie rapide** ⭐ + écrans cycles |
| **D6** | Couverture ≥ 80 % · écrans finalisés |
| **D7** | **Jeu de données de démo réaliste** + captures client pour le rapport |
| **D8** | Répétition de ta démo |

**Ton livrable du D7 compte plus qu'il n'en a l'air.** Sans jeu de données crédible, les
dashboards Grafana de Klein sont vides et les captures d'écran du rapport font amateur.
Prévois : 3 tontines, ~15 membres, 2 cycles, une cinquantaine de cotisations, 4 prêts à
différents stades, quelques remboursements. Avec des noms burundais réalistes et des
montants en BIF plausibles.

---

## Ton binôme

**Klein** — cohérence entre le cycle et les transactions de caisse.
Si tu bloques plus de 30 minutes, tu écris dans le groupe.

---

## Ton outillage

`/regle R2` et `/regle R3` — la commande te fera reformuler la règle en Gherkin et
attendra ta validation avant de coder.

`/ecran POST /api/cotisations/batch` pour la saisie rapide.

`/explique` **avant chaque daily**.

Mets ceci dans `~/.claude/CLAUDE.md` sur ta machine :

```markdown
Je suis Benitha Gahimbare (@gahibenitha), développeuse sur le projet Akiwacu
(plateforme de gestion de tontines, TP Frameworks JEE + API REST, UPG Gitega,
soutenance le 14 août 2026).

Je possède les domaines : cycle et cotisation (~14 endpoints).
Mes règles métier : R2 (toute opération financière appartient à un cycle actif)
et R3 (aucun prêt sur un cycle gelé ou clôturé). J'implémente la machine à états
du cycle : OUVERT → GELE → CLOTURE.

Je publie CycleGuardService.assertCycleActif(), une garde partagée appelée par
les domaines de Gloria et de Klein. Trois personnes en dépendent : ce service
doit être propre, documenté et stable.

Mon écran phare est la saisie rapide des cotisations : le trésorier enregistre
40 membres en réunion, sur téléphone, à une main. Priorité absolue à la vitesse
de saisie.

Au début de chaque session, lis : CLAUDE.md, docs/PLANNING-8-JOURS.md,
docs/roles/M4-*.md, et mes specs Gherkin.

IMPORTANT : je dois pouvoir expliquer chaque ligne à l'oral devant mon enseignant.
Code lisible plutôt que malin, commentaires en français, et quand tu génères un
test explique-moi en 3 lignes ce qu'il vérifie.
```

---

## La règle sur l'IA

> L'IA écrit le premier jet. **Toi, tu possèdes l'explication.**

Tu relis ligne par ligne et tu ajoutes 2-3 lignes de commentaire **avec tes propres
mots**. Si tu ne peux pas l'expliquer au daily du lendemain, tu le réécris.

---

## Ce que tu montres à la soutenance

1. **La saisie rapide : 10 cotisations en 30 secondes**, sur téléphone. C'est visuel et tout le monde comprend.
2. Tu gèles un cycle, puis tu tentes une cotisation → **HTTP 409** avec un message clair
3. Le test `shouldRejectOperationOnInactiveCycle()`
4. La machine à états, et pourquoi certaines transitions sont interdites

**Questions à préparer :**
- Que se passe-t-il si on tente de rouvrir un cycle clôturé ?
- Pourquoi la garde est-elle dans un service partagé plutôt que dupliquée dans chaque domaine ?
- Si deux trésoriers saisissent la même cotisation en même temps, que se passe-t-il ?
- Peut-on clôturer un cycle s'il reste des prêts en cours ?
- Pourquoi cette règle est-elle dans le service et pas dans le contrôleur ?

---

## Ton pack

| Fichier | Quand |
|---|---|
| [`INSTALLER-MON-PACK.md`](INSTALLER-MON-PACK.md) | **En premier.** Pack + dépôt côte à côte, Obsidian, Claude Code. |
| **[`SPECS-GHERKIN.md`](SPECS-GHERKIN.md)** | **D1.** Le métier que tu vas coder. |
| **[`TESTS-A-COMPLETER.md`](TESTS-A-COMPLETER.md)** | D3–D4. |
| [`MES-TACHES.md`](MES-TACHES.md) | Chaque matin. |
| [`MA-SOUTENANCE.md`](MA-SOUTENANCE.md) | D7–D8. |
| [`Akiwacu/docs/GUIDE-SETUP-POSTE.md`](../Akiwacu/docs/GUIDE-SETUP-POSTE.md) | D1, avant tout. |
| [`Akiwacu/docs/GUIDE-GIT-GITHUB.md`](../Akiwacu/docs/GUIDE-GIT-GITHUB.md) | La boucle de travail. |
| [`Akiwacu/docs/GUIDE-JIRA-SCRUM.md`](../Akiwacu/docs/GUIDE-JIRA-SCRUM.md) | Partie A. |
