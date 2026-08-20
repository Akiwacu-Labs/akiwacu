# PREMIÈRE PR — comment on travaille à cinq sans se bloquer

Le socle est mergé. Les 13 tables existent, chacun a ses entités. À partir de
maintenant, **cinq personnes poussent en parallèle** — c'est un mode de défaillance
différent de celui d'hier.

Trois règles suffisent à l'éviter. Elles ne sont pas négociables.

---

## Règle 1 — tu n'écris que dans TON package

`CODEOWNERS` le vérifie, mais la vraie raison est humaine : deux personnes qui
modifient le même fichier le même jour perdent une heure chacune en conflit.

| | Packages | Ne touche à rien d'autre |
|---|---|---|
| **Andy** | `config/` `common/` `auth/` `utilisateur/` `membre/` | |
| **Klein** | `remboursement/` `caisse/` `dashboard/` · `.github/` · `infra/` | |
| **Juste** | `tontine/` `adhesion/` `recu/` | |
| **Benitha** | `cycle/` `cotisation/` | |
| **Gloria** | `demandepret/` `vote/` `pret/` | |

Besoin d'un champ dans l'entité de quelqu'un d'autre ? **Tu le demandes au daily.**
Tu ne l'ajoutes pas toi-même, même si c'est trivial.

## Règle 2 — les numéros de migration Flyway sont réservés par personne

C'est le piège qui va vous coûter une soirée si on ne le pose pas maintenant. Si
quatre personnes créent chacune un `V3__…sql`, Flyway refuse de démarrer et il faut
renuméroter à la main dans quatre branches.

| | Plage réservée |
|---|---|
| **Andy** | `V3` → `V9` |
| **Juste** | `V10` → `V19` |
| **Benitha** | `V20` → `V29` |
| **Gloria** | `V30` → `V39` |
| **Klein** | `V40` → `V49` |

Exemple : `V20__ajouter_index_cotisation_membre.sql`.

> **Une migration déjà mergée ne se modifie JAMAIS.** Flyway garde une empreinte de
> chaque fichier appliqué ; le modifier casse le démarrage de toutes les bases où il
> est déjà passé. On ajoute une nouvelle migration.

## Règle 3 — une seule chose en cours à la fois

Le tableau autorise 2 en `In Progress`. **Sur ce qu'il nous reste, tiens-toi à 1.**
Deux tickets ouverts en parallèle, c'est deux tickets finis à moitié le soir.

---

# Ta première PR

Petite. **2 à 4 heures de travail, pas une journée.** Une PR de 800 lignes ne se
relit pas, elle s'approuve les yeux fermés — et c'est exactement ce que l'énoncé
cherche à empêcher.

| | Première PR | Pourquoi celle-là |
|---|---|---|
| **Andy** | `GlobalExceptionHandler` + les exceptions métier (`RegleMetierException`, `RessourceIntrouvableException`, `OperationVerrouilleeException`) | Tout le monde va lever ces exceptions. Sans elles, chacun invente sa façon de renvoyer un 409. |
| **Klein** | Déploiement automatique sur `vm-dev-g1` — secrets SSH vérifiés, `DEPLOY_ENABLED=true` | C'est la **porte de sortie du D3** et une part directe de la note DevOps. Ses domaines métier viennent après. |
| **Juste** | `CRUD tontine` + **l'interface `RecuService` avec un stub** | Trois personnes attendent cette interface pour compiler. Le stub d'abord, l'implémentation au D4. |
| **Benitha** | `CRUD cycle` + **`CycleGuardService.assertCycleActif()`** | Même raison : Gloria et Klein en dépendent. La garde est courte — statut `OUVERT` ou exception. |
| **Gloria** | `CRUD demandepret` | Elle dépend le moins des autres, elle peut avancer seule immédiatement. |

**Ce qu'on ne fait PAS aujourd'hui :** aucun écran client, aucune requête Bruno.
Le client démarre au D5. La Definition of Done décrit une story **terminée**, pas
chaque PR.

---

# Le prompt de démarrage de ton agent

Ouvre Claude Code **depuis le dépôt**, pas depuis ton pack :

```bash
cd ~/projects/Akiwacu
git checkout develop && git pull origin develop
git checkout -b feat/<domaine>-<slug>-AKW-<numéro>
claude
```

Colle ceci, en remplaçant les deux `<…>` :

> Lis dans cet ordre : `CLAUDE.md`, `docs/MODELE-DE-DONNEES.md`,
> `docs/MATRICE-REGLES-METIER.md`, `docs/DECISIONS.md`, et
> `docs/roles/M<n>-role.md` — c'est ma fiche de rôle.
>
> Je travaille sur le ticket **AKW-\<numéro\>** : \<résumé du ticket\>.
>
> Contraintes que tu ne franchis pas :
> — je n'écris QUE dans mes packages, ceux listés dans ma fiche de rôle ;
> — mes migrations Flyway sont numérotées dans MA plage réservée, voir
>   `docs/PREMIERE-PR.md` ;
> — les règles métier vont dans le **service**, jamais dans le contrôleur ;
> — le modèle de données fait autorité : si un champ manque, dis-le, ne l'invente pas ;
> — les noms de tests des règles R1 à R8 sont imposés, ne les reformule pas.
>
> Commence par me proposer un plan en 5 lignes. Je valide avant que tu écrives du code.
>
> Vérifie avec `cd api && ./mvnw clean verify`, puis `docker compose up -d db` et
> `./mvnw spring-boot:run` pour confirmer que l'application démarre.

Le « plan en 5 lignes d'abord » est la partie qui compte. Un agent qui écrit
400 lignes avant que tu aies regardé produit du code que tu ne sauras pas défendre
le 14.

---

# Le rythme de la journée

**Une PR ouverte se relit dans l'heure.** C'est la seule règle de fonctionnement qui
compte à cinq. Une PR qui dort trois heures bloque son auteur, qui commence autre
chose, et on se retrouve avec cinq travaux à moitié faits.

Andy et Klein sont co-propriétaires de presque tous les chemins : l'un des deux peut
approuver n'importe quelle PR. **Une seule approbation suffit.**

Boucle complète, pour mémoire :

```bash
git checkout develop && git pull origin develop
git checkout -b feat/cycle-crud-AKW-42
# … travail …
cd api && ./mvnw clean verify
git commit -m "feat(cycle): CRUD des cycles avec tests (AKW-42)"
git push -u origin feat/cycle-crud-AKW-42
gh pr create --base develop          # SANS --fill, pour avoir le modèle de PR
```

Puis sur Jira : le ticket passe en **In Review**. Après merge **et** vérification,
en **Done** — pas avant.

**Bloqué plus de 30 minutes ?** Tu écris dans le groupe. Sur ce qu'il reste, une
demi-journée perdue en silence n'est pas rattrapable.
