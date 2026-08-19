-- D-25 : le taux d'intérêt de Pret est un forfait MENSUEL, pas un forfait unique.
-- montantDu = montantAccorde + (montantAccorde x tauxInteret / 100 x dureeMois)
-- dureeMois est recopié depuis DemandePret au moment du déblocage (voir DECISIONS.md D-25) :
-- Pret reste calculable et testable sans charger sa DemandePret d'origine.
--
-- Table encore vide (aucun PretService n'existe) : NOT NULL sans DEFAULT est sûr ici.
ALTER TABLE prets ADD COLUMN duree_mois INTEGER NOT NULL CHECK (duree_mois > 0);
