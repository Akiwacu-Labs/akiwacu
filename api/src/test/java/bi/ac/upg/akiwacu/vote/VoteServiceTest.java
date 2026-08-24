package bi.ac.upg.akiwacu.vote;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.RegleMetierException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cycle.Cycle;
import bi.ac.upg.akiwacu.demandepret.DemandePret;
import bi.ac.upg.akiwacu.demandepret.DemandePretRepository;
import bi.ac.upg.akiwacu.demandepret.StatutDemandePret;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import bi.ac.upg.akiwacu.utilisateur.UtilisateurRepository;
import bi.ac.upg.akiwacu.vote.dto.VoteRequest;
import bi.ac.upg.akiwacu.vote.dto.VoteResponse;
import bi.ac.upg.akiwacu.vote.mapper.VoteCommissaireMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService — règle R4")
class VoteServiceTest {

    @Mock
    private VoteCommissaireRepository voteRepository;
    @Mock
    private DemandePretRepository demandePretRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private VoteCommissaireMapper voteMapper;

    @InjectMocks
    private VoteService voteService;

    @BeforeEach
    void initialiserContexte() {
        TenantContext.setTontineId(1L);
    }

    @AfterEach
    void nettoyerContexte() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("R4 — refuse un second vote du même commissaire")
    void shouldRejectApprovalFromSameCommissionerTwice() {
        var demande = demande(20L, 1L);
        var commissaire = utilisateur(7L, 1L, "alice@akiwacu.bi");
        authentifier("alice@akiwacu.bi");
        when(demandePretRepository.findById(20L)).thenReturn(Optional.of(demande));
        when(utilisateurRepository.findByEmail("alice@akiwacu.bi"))
                .thenReturn(Optional.of(commissaire));
        when(voteRepository.existsByDemandePretIdAndCommissaireId(20L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> voteService.voter(20L, new VoteRequest(SensVote.POUR, null)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("R4");

        verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("R4 — approuve la demande au second vote favorable distinct")
    void shouldApproveRequestOnSecondDistinctFavorableVote() {
        var demande = demande(20L, 1L);
        var commissaire = utilisateur(8L, 1L, "bob@akiwacu.bi");
        authentifier("bob@akiwacu.bi");
        var vote = VoteCommissaire.builder().demandePret(demande).commissaire(commissaire)
                .sens(SensVote.POUR).dateVote(Instant.now()).build();
        var reponse = new VoteResponse(31L, 20L, 8L, SensVote.POUR, null, vote.getDateVote());
        when(demandePretRepository.findById(20L)).thenReturn(Optional.of(demande));
        when(utilisateurRepository.findByEmail("bob@akiwacu.bi"))
                .thenReturn(Optional.of(commissaire));
        when(voteRepository.existsByDemandePretIdAndCommissaireId(20L, 8L)).thenReturn(false);
        when(voteRepository.save(any(VoteCommissaire.class))).thenReturn(vote);
        when(voteRepository.countByDemandePretIdAndSens(20L, SensVote.POUR)).thenReturn(2L);
        when(voteRepository.countByDemandePretIdAndSens(20L, SensVote.CONTRE)).thenReturn(0L);
        when(voteMapper.versReponse(vote)).thenReturn(reponse);

        var resultat = voteService.voter(20L, new VoteRequest(SensVote.POUR, null));

        assertThat(resultat).isEqualTo(reponse);
        assertThat(demande.getStatut()).isEqualTo(StatutDemandePret.APPROUVEE);
        verify(demandePretRepository).save(demande);
    }

    @Test
    @DisplayName("R1 — masque un commissaire d'une autre tontine")
    void shouldRejectVoteFromCommissionerOfAnotherTontine() {
        var demande = demande(20L, 1L);
        var commissaire = utilisateur(9L, 2L, "david@autre.bi");
        authentifier("david@autre.bi");
        when(demandePretRepository.findById(20L)).thenReturn(Optional.of(demande));
        when(utilisateurRepository.findByEmail("david@autre.bi"))
                .thenReturn(Optional.of(commissaire));

        assertThatThrownBy(() -> voteService.voter(20L, new VoteRequest(SensVote.POUR, null)))
                .isInstanceOf(RessourceIntrouvableException.class);

        verify(voteRepository, never()).save(any());
    }

    private DemandePret demande(Long id, Long tontineId) {
        var demande = DemandePret.builder().cycle(cycle(tontineId)).build();
        demande.setId(id);
        return demande;
    }

    private Cycle cycle(Long tontineId) {
        return Cycle.builder().tontine(tontine(tontineId)).build();
    }

    private Tontine tontine(Long id) {
        var tontine = Tontine.builder().build();
        tontine.setId(id);
        return tontine;
    }

    private Utilisateur utilisateur(Long id, Long tontineId, String email) {
        var utilisateur = Utilisateur.builder().tontine(tontine(tontineId)).email(email).build();
        utilisateur.setId(id);
        return utilisateur;
    }

    private void authentifier(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
    }
}
