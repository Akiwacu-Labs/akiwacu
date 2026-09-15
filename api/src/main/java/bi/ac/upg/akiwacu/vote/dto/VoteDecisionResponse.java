package bi.ac.upg.akiwacu.vote.dto;

import bi.ac.upg.akiwacu.demandepret.StatutDemandePret;

/** Résumé de décision consommé par l'écran de suivi d'une demande. */
public record VoteDecisionResponse(
        Long demandePretId,
        long votesPour,
        long votesContre,
        int quorumRequis,
        boolean quorumAtteint,
        StatutDemandePret statut) {
}
