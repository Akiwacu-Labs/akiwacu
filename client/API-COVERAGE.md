# Couverture du contrat API

Cette matrice est basée sur `client/src/api/generated/schema.d.ts`, généré depuis le contrat OpenAPI fusionné de PR #53. Elle distingue les opérations réellement déclenchées par un écran, celles déclenchées par un formulaire/mutation, celles protégées par le rôle serveur, et les adaptateurs prêts mais dont l’écran détaillé reste à brancher.

| Domaine | Opération | Couverture client | Écran / règle |
| --- | --- | --- | --- |
| Authentification | `POST /api/auth/login` | formulaire/mutation | `LoginPage` |
| Tableau de bord | `GET /api/dashboard` | écran réel | `DashboardPage` |
| Tontines | `GET /api/tontines` | écran réel | `TontinesPage` |
| Tontines | `POST /api/tontines` | formulaire/mutation | `TontinesPage` |
| Tontines | `GET /api/tontines/{id}` | adaptateur + hook, détail à brancher | `useTontineQuery` |
| Tontines | `PUT /api/tontines/{id}` | formulaire/mutation | `TontinesPage` |
| Tontines | `DELETE /api/tontines/{id}` | formulaire/mutation | `TontinesPage` |
| Utilisateurs | `GET /api/utilisateurs` | écran réel | `UtilisateursPage` |
| Utilisateurs | `POST /api/utilisateurs` | formulaire/mutation | `UtilisateursPage` |
| Utilisateurs | `GET /api/utilisateurs/{id}` | adaptateur + hook, détail à brancher | `useUtilisateurQuery` |
| Utilisateurs | `PUT /api/utilisateurs/{id}` | formulaire/mutation | `UtilisateursPage` |
| Utilisateurs | `DELETE /api/utilisateurs/{id}` | formulaire/mutation | `UtilisateursPage` |
| Membres | `GET /api/membres` | écran réel | `MembresPage` |
| Membres | `POST /api/membres` | formulaire/mutation | `MembresPage` |
| Membres | `GET /api/membres/{id}` | écran réel | inspection dans `MembresPage` |
| Membres | `PUT /api/membres/{id}` | formulaire/mutation | `MembresPage` |
| Adhésions | `GET /api/adhesions` | écran réel | `AdhesionsPage` |
| Adhésions | `POST /api/adhesions` | formulaire/mutation | `AdhesionsPage` |
| Adhésions | `GET /api/adhesions/{id}` | écran réel | inspection dans `AdhesionsPage` |
| Adhésions | `PUT /api/adhesions/{id}` | formulaire/mutation | `AdhesionsPage` |
| Cycles | `GET /api/cycles` | écran réel | `CyclesPage` |
| Cycles | `POST /api/cycles` | formulaire/mutation | `CyclesPage` |
| Cycles | `GET /api/cycles/{id}` | écran réel | inspection dans `CyclesPage` |
| Cycles | `PUT /api/cycles/{id}` | formulaire/mutation | `CyclesPage` |
| Cycles | `PATCH /api/cycles/{id}/statut` | formulaire/mutation | `CyclesPage`, transitions autorisées par l’API |
| Cotisations | `GET /api/cotisations` | écran réel | `CotisationsPage` |
| Cotisations | `POST /api/cotisations` | formulaire/mutation | `CotisationsPage` |
| Cotisations | `GET /api/cotisations/{id}` | écran réel | inspection dans `CotisationsPage` |
| Cotisations | `PUT /api/cotisations/{id}` | formulaire/mutation | `CotisationsPage` |
| Cotisations | `POST /api/cotisations/batch` | formulaire/mutation | saisie groupée dans `CotisationsPage` |
| Caisse | `GET /api/transactions-caisse` | écran réel | `CaissePage` |
| Caisse | `POST /api/transactions-caisse` | formulaire/mutation | `CaissePage` |
| Caisse | `GET /api/transactions-caisse/{id}` | adaptateur + hook, détail à brancher | `useTransactionCaisseQuery` |
| Caisse | `PUT /api/transactions-caisse/{id}` | formulaire/mutation | `CaissePage` |
| Caisse | `DELETE /api/transactions-caisse/{id}` | formulaire/mutation | `CaissePage` |
| Caisse | `GET /api/transactions-caisse/solde` | écran réel | carte de solde dans `CaissePage` |
| Caisse | `GET /api/transactions-caisse/cycle/{cycleId}` | adaptateur + hook, filtre à brancher | `useTransactionsParCycleQuery` |
| Remboursements | `GET /api/remboursements` | écran réel | `RemboursementsPage` |
| Remboursements | `POST /api/remboursements` | formulaire/mutation | `RemboursementsPage` |
| Remboursements | `GET /api/remboursements/{id}` | écran réel | inspection dans `RemboursementsPage` |
| Remboursements | `PUT /api/remboursements/{id}` | formulaire/mutation | `RemboursementsPage` |
| Remboursements | `DELETE /api/remboursements/{id}` | formulaire/mutation | `RemboursementsPage` |
| Remboursements | `GET /api/remboursements/pret/{pretId}` | écran réel | détail d’un prêt dans `PretsPage` |
| Demandes de prêt | `GET /api/demandes-pret` | écran réel | filtre statut dans `PretsPage` |
| Demandes de prêt | `POST /api/demandes-pret` | formulaire/mutation, rôle `MEMBRE` | `PretsPage` |
| Votes | `GET /api/demandes-pret/{demandePretId}/votes` | écran réel | panneau de décision dans `PretsPage` |
| Votes | `POST /api/demandes-pret/{demandePretId}/votes` | formulaire/mutation, rôle `COMMISSAIRE` | `PretsPage` |
| Votes | `GET /api/demandes-pret/{demandePretId}/votes/{voteId}` | écran réel | détail du vote dans `PretsPage` |
| Votes | `GET /api/demandes-pret/{demandePretId}/votes/decision` | écran réel | quorum dans `PretsPage` |
| Prêts | `GET /api/prets` | écran réel | `PretsPage` |
| Prêts | `POST /api/prets` | formulaire/mutation, rôle `TRESORIER` | déblocage dans `PretsPage` |
| Prêts | `GET /api/prets/{pretId}` | écran réel | sélection d’un prêt dans `PretsPage` |
| Prêts | `GET /api/prets/{pretId}/echeancier` | écran réel | échéancier dans `PretsPage` |
| Reçus | `GET /api/recus/{id}/pdf` | écran réel | téléchargements dérivés des reçus disponibles dans `RecusPage` |

Total : **55 opérations HTTP**. Les lignes “adaptateur + hook” ne sont pas inventées : l’opération est typée et disponible, mais son écran de détail ou son filtre dédié doit encore être finalisé avant de déclarer une couverture UX complète.
