package bi.ac.upg.akiwacu.recu;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.common.exception.OperationVerrouilleeException;
import bi.ac.upg.akiwacu.common.exception.RessourceIntrouvableException;
import bi.ac.upg.akiwacu.cotisation.Cotisation;
import bi.ac.upg.akiwacu.cotisation.CotisationRepository;
import bi.ac.upg.akiwacu.membre.Membre;
import bi.ac.upg.akiwacu.pret.Pret;
import bi.ac.upg.akiwacu.pret.PretRepository;
import bi.ac.upg.akiwacu.remboursement.Remboursement;
import bi.ac.upg.akiwacu.remboursement.RemboursementRepository;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

/** Implémentation partagée du contrat de reçus PDF. */
@Service
public class RecuGenerationService implements RecuService {

    private final RecuRepository recuRepository;
    private final CotisationRepository cotisationRepository;
    private final PretRepository pretRepository;
    private final RemboursementRepository remboursementRepository;

    public RecuGenerationService(RecuRepository recuRepository,
                                 CotisationRepository cotisationRepository,
                                 PretRepository pretRepository,
                                 RemboursementRepository remboursementRepository) {
        this.recuRepository = recuRepository;
        this.cotisationRepository = cotisationRepository;
        this.pretRepository = pretRepository;
        this.remboursementRepository = remboursementRepository;
    }

    @Override
    @Transactional
    public Recu genererPourCotisation(Long cotisationId) {
        Cotisation operation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new RessourceIntrouvableException("Cotisation introuvable : " + cotisationId));
        verifierOperationDisponible(TypeOperationRecu.COTISATION, cotisationId, operation.estVerrouillee());
        var membre = operation.getMembre();
        verifierTenant(membre.getTontine().getId());
        return generer(TypeOperationRecu.COTISATION, cotisationId, operation.getDateCotisation(), membre,
                membre.getTontine().getNom(), membre.getNom() + " " + membre.getPrenom(),
                operation.getMontant(), operation.getValidePar(), recu -> {
                    operation.setRecu(recu);
                    operation.setVerrouille(true);
                    cotisationRepository.save(operation);
                });
    }

    @Override
    @Transactional
    public Recu genererPourPret(Long pretId) {
        Pret operation = pretRepository.findById(pretId)
                .orElseThrow(() -> new RessourceIntrouvableException("Prêt introuvable : " + pretId));
        verifierOperationDisponible(TypeOperationRecu.DEBLOCAGE_PRET, pretId,
                operation.isVerrouille() || operation.getRecu() != null);
        var membre = operation.getMembre();
        verifierTenant(membre.getTontine().getId());
        return generer(TypeOperationRecu.DEBLOCAGE_PRET, pretId, operation.getDateDeblocage(), membre,
                membre.getTontine().getNom(), membre.getNom() + " " + membre.getPrenom(),
                operation.getMontantAccorde(), operation.getValidePar(), recu -> {
                    operation.setRecu(recu);
                    operation.setVerrouille(true);
                    pretRepository.save(operation);
                });
    }

    @Override
    @Transactional
    public Recu genererPourRemboursement(Long remboursementId) {
        Remboursement operation = remboursementRepository.findById(remboursementId)
                .orElseThrow(() -> new RessourceIntrouvableException("Remboursement introuvable : " + remboursementId));
        verifierOperationDisponible(TypeOperationRecu.REMBOURSEMENT, remboursementId,
                operation.isVerrouille() || operation.getRecu() != null);
        var membre = operation.getPret().getMembre();
        verifierTenant(membre.getTontine().getId());
        return generer(TypeOperationRecu.REMBOURSEMENT, remboursementId, operation.getDateRemboursement(), membre,
                membre.getTontine().getNom(), membre.getNom() + " " + membre.getPrenom(),
                operation.getMontant(), operation.getValidePar(), recu -> {
                    operation.setRecu(recu);
                    operation.setVerrouille(true);
                    remboursementRepository.save(operation);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] telecharger(Long recuId) {
        Recu recu = recuRepository.findById(recuId)
                .orElseThrow(() -> new RessourceIntrouvableException("Reçu introuvable : " + recuId));
        verifierTenant(recu.getMembre().getTontine().getId());
        return recu.getContenuPdf();
    }

    private Recu generer(TypeOperationRecu type, Long operationId, LocalDate date,
                         Membre membre, String tontine,
                         String membreNom, BigDecimal montant, Utilisateur tresorier,
                         java.util.function.Consumer<Recu> verrouiller) {
        String numero = "RC-%d-%s-%06d".formatted(date.getYear(), codeTontine(tontine), recuRepository.prochainNumero());
        byte[] pdf = construirePdf(numero, date, tontine, membreNom, montant, type, nomComplet(tresorier));
        Recu recu = Recu.builder()
                .numero(numero)
                .typeOperation(type)
                .operationId(operationId)
                .membre(membre)
                .montant(montant)
                .dateEmission(Instant.now())
                .emisPar(tresorier)
                .contenuPdf(pdf)
                .build();
        recu = recuRepository.save(recu);
        verrouiller.accept(recu);
        return recu;
    }

    private void verifierOperationDisponible(TypeOperationRecu type, Long operationId, boolean verrouillee) {
        if (verrouillee || recuRepository.findByTypeOperationAndOperationId(type, operationId).isPresent()) {
            throw new OperationVerrouilleeException("Un reçu existe déjà pour cette opération");
        }
    }

    private void verifierTenant(Long tontineId) {
        if (TenantContext.estDefini() && !TenantContext.getTontineId().equals(tontineId)) {
            throw new RessourceIntrouvableException("Opération hors de la tontine courante");
        }
    }

    private byte[] construirePdf(String numero, LocalDate date, String tontine, String membre,
                                 BigDecimal montant, TypeOperationRecu type, String tresorier) {
        try (var sortie = new ByteArrayOutputStream()) {
            var document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, sortie);
            document.open();
            var titre = new Font(Font.HELVETICA, 16, Font.BOLD);
            var normal = new Font(Font.HELVETICA, 11);
            document.add(new Paragraph("TONTINE " + tontine.toUpperCase(Locale.ROOT), titre));
            document.add(new Paragraph("Reçu N° " + numero, normal));
            var table = new PdfPTable(2);
            table.setWidthPercentage(100);
            ajouterLigne(table, "Date", date.toString(), normal);
            ajouterLigne(table, "Membre", membre, normal);
            ajouterLigne(table, "Type d'opération", type.name(), normal);
            ajouterLigne(table, "Montant", formater(montant), normal);
            ajouterLigne(table, "Validé par", tresorier, normal);
            document.add(table);
            document.close();
            return sortie.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Impossible de générer le PDF du reçu", exception);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Impossible de fermer le PDF du reçu", exception);
        }
    }

    private void ajouterLigne(PdfPTable table, String libelle, String valeur, Font police) {
        table.addCell(new Phrase(libelle, police));
        table.addCell(new Phrase(valeur, police));
    }

    private String formater(BigDecimal montant) {
        return NumberFormat.getInstance(Locale.FRANCE).format(montant) + " BIF";
    }

    private String nomComplet(Utilisateur utilisateur) {
        return utilisateur.getNom() + " " + utilisateur.getPrenom();
    }

    private String codeTontine(String nom) {
        String code = nom.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return (code + "XXXX").substring(0, 4);
    }
}
