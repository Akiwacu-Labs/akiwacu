# Domaine auth — Andy

Authentification par JWT. Pas d'inscription ici : les comptes sont créés par
le domaine `utilisateur` (CRUD, D3).

- Propriétaire : Andy Miguel Habyarimana (`@miguelandy875`)
- `POST /api/auth/login` — seule route publique du domaine
- `JwtService` : émission/lecture des jetons (claims : email, utilisateurId,
  tontineId, roles)
- `JwtAuthenticationFilter` : pose l'`Authentication` Spring Security ET le
  `TenantContext` (R1) à partir des mêmes claims, sur chaque requête
- Stateless — aucune session, aucun jeton de rafraîchissement (hors périmètre)
- R1 : `tontineId` vient exclusivement du jeton (voir DECISIONS.md D-20)
