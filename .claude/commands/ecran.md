---
description: Génère l'écran React qui consomme un endpoint donné
---

Génère l'écran React pour `$ARGUMENTS`.

Contraintes :
- TypeScript strict, **mobile-first** (conçois pour 360 px puis élargis)
- Client API **généré** depuis OpenAPI — n'écris jamais un `fetch` à la main
- TanStack Query pour les appels serveur (pas de `useEffect` + `fetch`)
- React Hook Form + Zod pour les formulaires, messages d'erreur en français
- Composants shadcn/ui, pas de CSS custom sauf nécessité
- États gérés explicitement : chargement, erreur, vide, succès
- Route protégée par rôle (ADMIN / GESTIONNAIRE / TRESORIER / COMMISSAIRE / MEMBRE)
- Les erreurs 409 du backend correspondent à des violations de règle métier :
  affiche le message du serveur tel quel, il est déjà en français

Si une maquette Figma existe pour cet écran, récupère son contexte de design via
le MCP Figma avant de générer, et respecte l'espacement et la typographie.
