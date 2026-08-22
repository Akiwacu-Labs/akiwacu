# Domaine votes commissaires

- Propriétaire : Gloria Muhimpundu (`@muhimpundugloria`)
- Périmètre : votes POUR/CONTRE sur les demandes de prêt.
- R4 exige deux votes POUR de commissaires distincts.
- La contrainte unique `(demande_pret_id, commissaire_id)` protège l'écriture concurrente.
- Un commissaire d'une autre tontine est refusé par R1.
- Les règles métier restent dans le service, jamais dans le contrôleur.
