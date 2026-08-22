# Domaine membre — Andy

CRUD des membres d'une tontine (D3). Un membre peut exister sans compte de
connexion (`utilisateur` nullable) — saisie manuelle par le gestionnaire.

- Propriétaire : Andy Miguel Habyarimana (`@miguelandy875`)
- `POST/GET/PUT /api/membres` — réservé à `ADMIN`/`GESTIONNAIRE`
- R1 : `tontineId` vient de `TenantContext` ; le filtre Hibernate sur
  `Membre` et `Utilisateur` (D-33) protège aussi les lectures et le lien
  optionnel vers un compte
- Pas de suppression : un membre change de statut (`SUSPENDU`, `SORTI`),
  il reste référencé par ses cotisations, prêts et reçus
- `numeroMembre` est nullable dans le modèle de données — optionnel ici,
  unique par tontine quand fourni
