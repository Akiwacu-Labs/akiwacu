# Domaine tontine

Propriétaire : Juste Daxa Ayikunde.

Ce package permet de créer, consulter, modifier, lister et supprimer les tontines.
La création est un onboarding libre-service explicitement public : elle crée la tontine
et son premier utilisateur `ADMIN` dans la même transaction. C'est l'exception documentée
à R1, car aucun JWT ni `TenantContext` n'existe encore au moment de l'inscription.
La date de création et le statut initial `ACTIVE` sont fixés par le serveur, jamais par
le client. L'email du premier administrateur est vérifié avant toute écriture et un doublon
est refusé avec HTTP 409.
Le nom d'une tontine est unique : le service refuse un doublon avec une erreur HTTP 409.
Les contrôleurs ne contiennent aucune règle métier ; ils délèguent au `TontineService`.
Les DTO valident les champs obligatoires avant d'appeler le service.
