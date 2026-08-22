# Akiwacu — client

React 18 + TypeScript + Vite, direction visuelle « Le Compteur »
(voir `docs/DECISIONS.md` D-34, `docs/GUIDE-CLIENT-REACT.md`).

```bash
pnpm install
pnpm dev            # http://localhost:5173, proxy /api → localhost:8080
pnpm build           # → dist/, ce que le Dockerfile embarque dans nginx
pnpm gen:api         # régénère src/api/generated/ depuis /v3/api-docs (API démarrée requise)
```

## Règles non négociables (voir `docs/GUIDE-CLIENT-REACT.md`)

- Aucun `fetch` écrit à la main — tout appel API passe par le client généré
  (`pnpm gen:api`, jamais modifié à la main).
- Aucune couleur ni taille en dur dans un composant — tout vient des
  variables de `src/index.css`.
- `localStorage` réservé au jeton JWT, rien d'autre.
- Montants : toujours via `<Montant valeur={...} />` (`src/components/Montant.tsx`) —
  BIF sans décimale, chiffres tabulaires.
