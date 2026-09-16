---
description: Implémente une règle métier R1-R8 avec ses tests
---

Implémente la règle métier `$ARGUMENTS` (voir le tableau R1–R8 dans CLAUDE.md).

Marche à suivre :
1. Reformule la règle en Gherkin (Étant donné / Quand / Alors), y compris les
   cas limites. **Montre-le-moi et attends ma validation avant de coder.**
2. Implémente dans la couche **service**, jamais dans le contrôleur.
3. Lève une `RegleMetierException` dont le message commence par le code de la
   règle, ex. `"R6 : le montant demandé dépasse trois fois l'épargne du membre"`.
   Le `GlobalExceptionHandler` la traduit en HTTP 409.
4. Écris le test portant le nom exact indiqué dans CLAUDE.md, plus les tests des
   cas limites identifiés à l'étape 1.
5. Vérifie qu'aucune autre règle n'est contournée au passage (notamment R1 :
   `tontineId` toujours issu du JWT).
