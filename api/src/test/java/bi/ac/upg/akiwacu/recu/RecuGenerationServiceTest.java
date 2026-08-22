package bi.ac.upg.akiwacu.recu;

import bi.ac.upg.akiwacu.common.exception.OperationVerrouilleeException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.cotisation.Cotisation;
import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.cotisation.ModePaiement;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.pret.PretRepository;
import bi.ac.upg.akiwacu.remboursement.RemboursementRepository;
import bi.ac.upg.akiwacu.tontine.Tontine;
import bi.ac.upg.akiwacu.utilisateur.Role;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecuGenerationService — génération et verrouillage")
class RecuGenerationServiceTest {

    @AfterEach
    void nettoyerTenant() {
        TenantContext.clear();
    }

    @Mock
    private RecuRepository recuRepository;
    @Mock
    private CotisationRepository cotisationRepository;
    @Mock
    private PretRepository pretRepository;
    @Mock
    private RemboursementRepository remboursementRepository;
    @InjectMocks
    private RecuGenerationService service;

    @Test
    @DisplayName("Génère un reçu contenant les sept champs obligatoires")
    void shouldGenerateReceiptWithAllRequiredFields() {
        Cotisation cotisation = uneCotisation(10L);
        preparerCotisation(cotisation, 147L);

        Recu recu = service.genererPourCotisation(10L);

        assertThat(recu.getNumero()).isEqualTo("RC-2026-TWIY-000147");
        assertThat(recu.getContenuPdf()).startsWith(new byte[]{'%', 'P', 'D', 'F'});
        assertThat(recu.getTypeOperation()).isEqualTo(TypeOperationRecu.COTISATION);
        assertThat(recu.getMontant()).isEqualByComparingTo("10000");
        assertThat(recu.getMembre().getNom()).isEqualTo("Ndayisenga");
        assertThat(recu.getEmisPar().getPrenom()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Le numéro de reçu est unique par génération")
    void shouldGenerateUniqueReceiptNumberPerTontineAndYear() {
        Cotisation premiere = uneCotisation(10L);
        Cotisation seconde = uneCotisation(11L);
        preparerCotisation(premiere, 1L);
        preparerCotisation(seconde, 2L);
        when(recuRepository.prochainNumero()).thenReturn(1L, 2L);

        Recu recu1 = service.genererPourCotisation(10L);
        Recu recu2 = service.genererPourCotisation(11L);

        assertThat(recu1.getNumero()).isNotEqualTo(recu2.getNumero());
    }

    @Test
    @DisplayName("Refuse de générer deux reçus pour la même opération")
    void shouldRejectDuplicateReceiptForSameOperation() {
        Cotisation cotisation = uneCotisation(10L);
        when(cotisationRepository.findById(10L)).thenReturn(Optional.of(cotisation));
        when(recuRepository.findByTypeOperationAndOperationId(TypeOperationRecu.COTISATION, 10L))
                .thenReturn(Optional.of(new Recu()));

        assertThatThrownBy(() -> service.genererPourCotisation(10L))
                .isInstanceOf(OperationVerrouilleeException.class);
    }

    @Test
    @DisplayName("Verrouille l'opération dès la génération du reçu")
    void shouldLockOperationOnReceiptGeneration() {
        Cotisation cotisation = uneCotisation(10L);
        preparerCotisation(cotisation, 1L);

        service.genererPourCotisation(10L);

        assertThat(cotisation.estVerrouillee()).isTrue();
        assertThat(cotisation.getRecu()).isNotNull();
    }

    @Test
    @DisplayName("Enregistre le trésorier validateur sur le reçu")
    void shouldRecordValidatingTreasurerOnReceipt() {
        Cotisation cotisation = uneCotisation(10L);
        preparerCotisation(cotisation, 1L);

        Recu recu = service.genererPourCotisation(10L);

        assertThat(recu.getEmisPar().getRoles()).contains(Role.TRESORIER);
    }

    @Test
    @DisplayName("Lève une exception si l'opération est introuvable")
    void shouldThrowWhenOperationNotFound() {
        when(cotisationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.genererPourCotisation(404L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("R1 — masque l'état d'une opération appartenant à une autre tontine")
    void shouldNotRevealStateFromAnotherTontine() {
        Cotisation cotisation = uneCotisation(10L);
        when(cotisationRepository.findById(10L)).thenReturn(Optional.of(cotisation));
        TenantContext.setTontineId(99L);

        assertThatThrownBy(() -> service.genererPourCotisation(10L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("Le PDF généré est un document valide et non vide")
    void shouldProduceValidNonEmptyPdf() {
        Cotisation cotisation = uneCotisation(10L);
        preparerCotisation(cotisation, 1L);

        byte[] pdf = service.genererPourCotisation(10L).getContenuPdf();

        assertThat(pdf).isNotEmpty().startsWith(new byte[]{'%', 'P', 'D', 'F'});
    }

    private void preparerCotisation(Cotisation cotisation, long numero) {
        when(cotisationRepository.findById(cotisation.getId())).thenReturn(Optional.of(cotisation));
        when(recuRepository.findByTypeOperationAndOperationId(TypeOperationRecu.COTISATION, cotisation.getId()))
                .thenReturn(Optional.empty());
        lenient().when(recuRepository.prochainNumero()).thenReturn(numero);
        lenient().when(recuRepository.save(any(Recu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Cotisation uneCotisation(Long id) {
        var cotisation = Cotisation.builder()
                .cycle(null)
                .membre(unMembre())
                .montant(new BigDecimal("10000"))
                .dateCotisation(LocalDate.of(2026, 8, 9))
                .modePaiement(ModePaiement.ESPECES)
                .validePar(unTresorier())
                .build();
        cotisation.setId(id);
        return cotisation;
    }

    private Membre unMembre() {
        var tontine = Tontine.builder().nom("Twiyungunganye").build();
        tontine.setId(7L);
        return Membre.builder().tontine(tontine).nom("Ndayisenga").prenom("Alice")
                .telephone("+25770000000").dateAdhesion(LocalDate.of(2026, 8, 1)).build();
    }

    private Utilisateur unTresorier() {
        return Utilisateur.builder().nom("Ndayisenga").prenom("Alice")
                .roles(Set.of(Role.TRESORIER)).email("tresorier@example.bi").build();
    }
}
