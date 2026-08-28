package bi.ac.upg.akiwacu.vote.dto;

import bi.ac.upg.akiwacu.vote.SensVote;

import java.time.Instant;

public record VoteResponse(
        Long id,
        Long demandePretId,
        Long commissaireId,
        SensVote sens,
        String commentaire,
        Instant dateVote) {
}
