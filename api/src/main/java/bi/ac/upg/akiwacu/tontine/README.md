# Domaine tontine

Propriétaire : Juste Daxa Ayikunde.

Ce package permet de créer, consulter, modifier, lister et supprimer les tontines.
Le nom d'une tontine est unique : le service refuse un doublon avec une erreur HTTP 409.
Les contrôleurs ne contiennent aucune règle métier ; ils délèguent au `TontineService`.
Les DTO valident les champs obligatoires avant d'appeler le service.
