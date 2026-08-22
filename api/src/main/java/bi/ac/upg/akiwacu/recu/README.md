# Domaine recu

Propriétaire : Juste Daxa Ayikunde.

`RecuService` est le contrat partagé consommé par les domaines cotisation, prêt et remboursement.
`RecuGenerationService` génère une page PDF OpenPDF, consomme la séquence PostgreSQL
`seq_numero_recu`, enregistre le contenu en `BYTEA` et verrouille l'opération dans la
même transaction. La contrainte `(type_operation, operation_id)` empêche un second reçu.

Le téléchargement est exposé par `GET /api/recus/{id}/pdf` avec `Content-Disposition: inline`.
Les contrôles de tenant reposent sur le `TenantContext` lorsqu'il est présent.
