-- V1 — amorçage. Les 13 tables métier arrivent en V2__schema_initial.sql,
-- dans la PR socle, écrite par Andy.
--
-- Convention de nommage des migrations Flyway :
--   V<n>__<description_en_snake_case>.sql
-- Une migration déjà appliquée ne se modifie JAMAIS : on en ajoute une nouvelle.

CREATE TABLE IF NOT EXISTS schema_info (
    id          BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    applied_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO schema_info (description) VALUES ('socle Akiwacu initialisé');
