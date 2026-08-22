-- Stockage transactionnel du PDF : les fichiers ne dépendent pas du disque de la VM.
ALTER TABLE recus ADD COLUMN contenu_pdf BYTEA;
