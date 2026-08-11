-- V2 — socle : les 12 tables métier + utilisateur_roles, conformes à
-- docs/MODELE-DE-DONNEES.md. Ordre imposé par les clés étrangères :
--   tontines → utilisateurs → utilisateur_roles → membres → cycles → adhesions
--   → recus → cotisations → demandes_pret → votes_commissaire → prets
--   → remboursements → transactions_caisse
--
-- Colonnes d'audit communes (created_at/by, updated_at/by, version) : voir
-- common/BaseEntity.java. Elles restent nullables sauf `version`, alimentée
-- systématiquement par Hibernate avant le premier insert.

-- ============================================================
-- 1. tontines
-- ============================================================
CREATE TABLE tontines (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(150) NOT NULL,
    description     TEXT,
    date_creation   DATE NOT NULL,
    statut          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(100),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_tontine_nom UNIQUE (nom),
    CONSTRAINT ck_tontine_statut CHECK (statut IN ('ACTIVE', 'SUSPENDUE', 'CLOTUREE'))
);

-- ============================================================
-- 2. utilisateurs
-- ============================================================
CREATE TABLE utilisateurs (
    id              BIGSERIAL PRIMARY KEY,
    tontine_id      BIGINT NOT NULL REFERENCES tontines(id),
    email           VARCHAR(150) NOT NULL,
    mot_de_passe    VARCHAR(100) NOT NULL,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    telephone       VARCHAR(20),
    actif           BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(100),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_utilisateur_email UNIQUE (email)
);

-- ============================================================
-- 3. utilisateur_roles (@ElementCollection de Utilisateur.roles)
-- ============================================================
CREATE TABLE utilisateur_roles (
    utilisateur_id  BIGINT NOT NULL REFERENCES utilisateurs(id),
    role            VARCHAR(20) NOT NULL,
    PRIMARY KEY (utilisateur_id, role),
    CONSTRAINT ck_utilisateur_role CHECK (role IN ('ADMIN', 'GESTIONNAIRE', 'TRESORIER', 'COMMISSAIRE', 'MEMBRE'))
);

-- ============================================================
-- 4. membres
-- ============================================================
CREATE TABLE membres (
    id              BIGSERIAL PRIMARY KEY,
    tontine_id      BIGINT NOT NULL REFERENCES tontines(id),
    utilisateur_id  BIGINT REFERENCES utilisateurs(id),
    numero_membre   VARCHAR(30),
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    telephone       VARCHAR(20) NOT NULL,
    date_adhesion   DATE NOT NULL,
    statut          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(100),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_membre_tontine_numero UNIQUE (tontine_id, numero_membre),
    CONSTRAINT ck_membre_statut CHECK (statut IN ('ACTIF', 'SUSPENDU', 'SORTI'))
);

-- ============================================================
-- 5. cycles
-- ============================================================
CREATE TABLE cycles (
    id                  BIGSERIAL PRIMARY KEY,
    tontine_id          BIGINT NOT NULL REFERENCES tontines(id),
    libelle             VARCHAR(100) NOT NULL,
    date_debut          DATE NOT NULL,
    date_fin            DATE NOT NULL,
    montant_cotisation  NUMERIC(15,2) NOT NULL,
    periodicite         VARCHAR(20) NOT NULL,
    statut              VARCHAR(20) NOT NULL,
    date_cloture        DATE,
    created_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_cycle_dates CHECK (date_fin > date_debut),
    CONSTRAINT ck_cycle_montant CHECK (montant_cotisation > 0),
    CONSTRAINT ck_cycle_periodicite CHECK (periodicite IN ('HEBDOMADAIRE', 'MENSUELLE')),
    CONSTRAINT ck_cycle_statut CHECK (statut IN ('OUVERT', 'GELE', 'CLOTURE'))
);

-- ============================================================
-- 6. adhesions
-- ============================================================
CREATE TABLE adhesions (
    id              BIGSERIAL PRIMARY KEY,
    membre_id       BIGINT NOT NULL REFERENCES membres(id),
    cycle_id        BIGINT NOT NULL REFERENCES cycles(id),
    date_adhesion   DATE NOT NULL,
    statut          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(100),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_adhesion_membre_cycle UNIQUE (membre_id, cycle_id),
    CONSTRAINT ck_adhesion_statut CHECK (statut IN ('ACTIVE', 'CLOTUREE'))
);

-- ============================================================
-- 7. recus
-- Numéro généré depuis une séquence PostgreSQL par le futur service de
-- génération PDF (package recu/, propriétaire Juste) — non consommée ici,
-- juste déclarée en tant qu'objet de schéma.
-- ============================================================
CREATE SEQUENCE seq_numero_recu START WITH 1 INCREMENT BY 1;

CREATE TABLE recus (
    id                  BIGSERIAL PRIMARY KEY,
    numero              VARCHAR(30) NOT NULL,
    type_operation      VARCHAR(20) NOT NULL,
    operation_id        BIGINT NOT NULL,
    membre_id           BIGINT NOT NULL REFERENCES membres(id),
    montant             NUMERIC(15,2) NOT NULL,
    date_emission       TIMESTAMPTZ NOT NULL,
    emis_par_id         BIGINT NOT NULL REFERENCES utilisateurs(id),
    chemin_fichier      VARCHAR(255),
    created_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_recu_numero UNIQUE (numero),
    CONSTRAINT uq_recu_operation UNIQUE (type_operation, operation_id),
    CONSTRAINT ck_recu_type_operation CHECK (type_operation IN ('COTISATION', 'DEBLOCAGE_PRET', 'REMBOURSEMENT'))
);

-- ============================================================
-- 8. cotisations
-- ============================================================
CREATE TABLE cotisations (
    id                  BIGSERIAL PRIMARY KEY,
    cycle_id            BIGINT NOT NULL REFERENCES cycles(id),
    membre_id           BIGINT NOT NULL REFERENCES membres(id),
    montant             NUMERIC(15,2) NOT NULL,
    date_cotisation     DATE NOT NULL,
    mode_paiement       VARCHAR(20) NOT NULL,
    valide_par_id       BIGINT NOT NULL REFERENCES utilisateurs(id),
    verrouille          BOOLEAN NOT NULL DEFAULT false,
    recu_id             BIGINT REFERENCES recus(id),
    created_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_cotisation_montant CHECK (montant > 0),
    CONSTRAINT ck_cotisation_mode_paiement CHECK (mode_paiement IN ('ESPECES', 'MOBILE_MONEY', 'VIREMENT'))
);

-- ============================================================
-- 9. demandes_pret
-- ============================================================
CREATE TABLE demandes_pret (
    id                  BIGSERIAL PRIMARY KEY,
    cycle_id            BIGINT NOT NULL REFERENCES cycles(id),
    membre_id           BIGINT NOT NULL REFERENCES membres(id),
    montant_demande     NUMERIC(15,2) NOT NULL,
    duree_mois          INTEGER NOT NULL,
    motif               TEXT NOT NULL,
    date_demande        DATE NOT NULL,
    statut              VARCHAR(20) NOT NULL,
    created_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_demande_pret_montant CHECK (montant_demande > 0),
    CONSTRAINT ck_demande_pret_duree CHECK (duree_mois > 0),
    CONSTRAINT ck_demande_pret_statut CHECK (statut IN ('SOUMISE', 'APPROUVEE', 'REJETEE', 'DEBLOQUEE'))
);

-- ============================================================
-- 10. votes_commissaire
-- L'unicité (demande_pret_id, commissaire_id) est ce qui fait réellement R4.
-- ============================================================
CREATE TABLE votes_commissaire (
    id                  BIGSERIAL PRIMARY KEY,
    demande_pret_id     BIGINT NOT NULL REFERENCES demandes_pret(id),
    commissaire_id      BIGINT NOT NULL REFERENCES utilisateurs(id),
    sens                VARCHAR(10) NOT NULL,
    commentaire         TEXT,
    date_vote           TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vote_demande_commissaire UNIQUE (demande_pret_id, commissaire_id),
    CONSTRAINT ck_vote_sens CHECK (sens IN ('POUR', 'CONTRE'))
);

-- ============================================================
-- 11. prets
-- ============================================================
CREATE TABLE prets (
    id                  BIGSERIAL PRIMARY KEY,
    demande_pret_id     BIGINT NOT NULL REFERENCES demandes_pret(id),
    membre_id           BIGINT NOT NULL REFERENCES membres(id),
    cycle_id            BIGINT NOT NULL REFERENCES cycles(id),
    montant_accorde     NUMERIC(15,2) NOT NULL,
    taux_interet        NUMERIC(5,2) NOT NULL DEFAULT 0,
    date_deblocage      DATE NOT NULL,
    date_echeance       DATE NOT NULL,
    statut              VARCHAR(20) NOT NULL,
    valide_par_id       BIGINT NOT NULL REFERENCES utilisateurs(id),
    verrouille          BOOLEAN NOT NULL DEFAULT false,
    recu_id             BIGINT REFERENCES recus(id),
    created_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_pret_demande_pret UNIQUE (demande_pret_id),
    CONSTRAINT ck_pret_montant CHECK (montant_accorde > 0),
    CONSTRAINT ck_pret_statut CHECK (statut IN ('ACTIF', 'SOLDE', 'EN_RETARD'))
);

-- ============================================================
-- 12. remboursements
-- ============================================================
CREATE TABLE remboursements (
    id                  BIGSERIAL PRIMARY KEY,
    pret_id             BIGINT NOT NULL REFERENCES prets(id),
    montant             NUMERIC(15,2) NOT NULL,
    date_remboursement  DATE NOT NULL,
    valide_par_id       BIGINT NOT NULL REFERENCES utilisateurs(id),
    verrouille          BOOLEAN NOT NULL DEFAULT false,
    recu_id             BIGINT REFERENCES recus(id),
    created_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_remboursement_montant CHECK (montant > 0)
);

-- ============================================================
-- 13. transactions_caisse
-- ============================================================
CREATE TABLE transactions_caisse (
    id                      BIGSERIAL PRIMARY KEY,
    tontine_id              BIGINT NOT NULL REFERENCES tontines(id),
    cycle_id                BIGINT REFERENCES cycles(id),
    sens                    VARCHAR(10) NOT NULL,
    montant                 NUMERIC(15,2) NOT NULL,
    motif                   VARCHAR(255) NOT NULL,
    date_transaction        DATE NOT NULL,
    valide_par_id           BIGINT NOT NULL REFERENCES utilisateurs(id),
    reference_operation     VARCHAR(50),
    created_at              TIMESTAMPTZ,
    created_by              VARCHAR(100),
    updated_at              TIMESTAMPTZ,
    updated_by              VARCHAR(100),
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_caisse_sens CHECK (sens IN ('ENTREE', 'SORTIE')),
    CONSTRAINT ck_caisse_montant CHECK (montant > 0)
);

-- ============================================================
-- Index — sans eux, chaque écran de liste fait un balayage complet de table.
-- ============================================================
CREATE INDEX idx_membre_tontine        ON membres(tontine_id);
CREATE INDEX idx_cycle_tontine_statut  ON cycles(tontine_id, statut);
CREATE INDEX idx_cotisation_cycle      ON cotisations(cycle_id);
CREATE INDEX idx_cotisation_membre     ON cotisations(membre_id);
CREATE INDEX idx_pret_membre_statut    ON prets(membre_id, statut);
CREATE INDEX idx_remboursement_pret    ON remboursements(pret_id);
CREATE INDEX idx_caisse_tontine_cycle  ON transactions_caisse(tontine_id, cycle_id);
