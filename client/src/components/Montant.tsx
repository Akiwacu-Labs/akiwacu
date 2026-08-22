import { cn } from "@/lib/utils";

interface MontantProps {
  /** Toujours en BIF — le franc burundais n'a pas de sous-unité utilisée. */
  valeur: number | string;
  className?: string;
}

const formateurBif = new Intl.NumberFormat("fr-FR", {
  maximumFractionDigits: 0,
});

/**
 * Affiche un montant en BIF : jamais de décimale, espace comme séparateur
 * de milliers, chiffres tabulaires pour que les colonnes de montants
 * s'alignent (docs/GUIDE-CLIENT-REACT.md §3). Le style (taille, poids,
 * couleur) reste au composant appelant via className — seuls le format et
 * l'alignement des chiffres sont centralisés ici.
 */
export function Montant({ valeur, className }: MontantProps) {
  const nombre = typeof valeur === "string" ? Number(valeur) : valeur;
  return (
    <span className={cn("tabular-nums", className)}>
      {formateurBif.format(nombre)}&nbsp;BIF
    </span>
  );
}
