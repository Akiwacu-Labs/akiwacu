---
description: Prépare mon point quotidien à partir de l'état réel du dépôt
---

Prépare mon intervention au daily stand-up.

1. `git log --author="$(git config user.name)" --since="yesterday" --oneline`
   → ce que j'ai fait hier
2. `gh pr list --author @me` et `gh pr list --search "review-requested:@me"`
   → mes PR ouvertes et celles que je dois relire
3. `gh run list --limit 5` → état de la CI

Restitue en trois points, 30 secondes de lecture max :
- **Fait hier** :
- **Aujourd'hui** :
- **Bloqué par** : (ou « rien »)

Signale-moi si : je n'ai rien mergé depuis plus de 24 h, une PR attend ma
review depuis plus de 4 h, ou la CI est rouge sur `develop`.
