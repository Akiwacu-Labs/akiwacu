# Domaine adhesion

Propriétaire : Juste Daxa Ayikunde.

Le service gère l'inscription unique d'un membre à un cycle, limitée à la
tontine du TenantContext. La création démarre en statut ACTIVE ; la clôture
reste une modification explicite du gestionnaire ou de l'administrateur.
Les doublons membre/cycle renvoient une erreur métier 409.
