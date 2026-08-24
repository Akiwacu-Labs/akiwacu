package bi.ac.upg.akiwacu.vote.dto;

import bi.ac.upg.akiwacu.vote.SensVote;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(
        @NotNull(message = "le sens du vote est obligatoire")
        SensVote sens,
        String commentaire) {
}
