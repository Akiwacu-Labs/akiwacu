# Domaine adhesion

Propriétaire : Juste Daxa Ayikunde.

Le service gère l'inscription unique d'un membre à un cycle, limitée à la
tontine du TenantContext. La création démarre en statut ACTIVE ; la clôture
reste une modification explicite du gestionnaire ou de l'administrateur.
Les doublons membre/cycle renvoient une erreur métier 409.

Il n'existe volontairement pas de `DELETE /api/adhesions/{id}` : une adhésion
fait partie de l'historique d'un cycle et ne doit pas être supprimée
physiquement. Sa fin de vie est représentée par le statut `CLOTUREE`, via
`PUT /api/adhesions/{id}`.
