# Domaine utilisateur — Andy

CRUD des comptes de connexion (D3). Ne couvre pas la création via
`auth/` (login uniquement) ni les membres sans compte (voir `membre/`).

- Propriétaire : Andy Miguel Habyarimana (`@miguelandy875`)
- `POST/GET/PUT/DELETE /api/utilisateurs` — réservé à `ADMIN`/`GESTIONNAIRE`
- R1 : `tontineId` vient de `TenantContext`, jamais du corps de la requête ;
  le filtre Hibernate sur `Utilisateur` (D-33) protège aussi les lectures
- Suppression = désactivation (`actif=false`), jamais physique — voir
  `UtilisateurService.desactiver()` pour le motif (référencé par R5)
- Mot de passe : `BCryptPasswordEncoder`, jamais en clair, jamais renvoyé
