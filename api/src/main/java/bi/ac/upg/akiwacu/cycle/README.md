# Domaine cycle — Benitha

Ce package gère les cycles d'une tontine et leur machine à états.

- Propriétaire : Benitha Gahimbare (`@gahibenitha`)
- États : `OUVERT`, `GELE`, `CLOTURE` (final)
- R2 : une opération financière exige un cycle `OUVERT`
- R3 : un prêt est interdit lorsque le cycle est gelé ou clôturé
- Un seul cycle peut être ouvert par tontine
- Les recherches sont toujours filtrées par `tontineId`

`CycleGuardService.assertCycleActif(tontineId)` sera le contrat partagé avec
les domaines cotisation, prêt, remboursement et caisse.
