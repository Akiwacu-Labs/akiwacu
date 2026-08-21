package bi.ac.upg.akiwacu.recu;

/**
 * Contrat partagé de génération et de téléchargement des reçus.
 *
 * <p>Les domaines cotisation, prêt et remboursement dépendent uniquement de
 * cette interface. Sa publication précoce leur permet de compiler sans attendre
 * la génération PDF complète.</p>
 */
public interface RecuService {

    Recu genererPourCotisation(Long cotisationId);

    Recu genererPourPret(Long pretId);

    Recu genererPourRemboursement(Long remboursementId);

    byte[] telecharger(Long recuId);
}
