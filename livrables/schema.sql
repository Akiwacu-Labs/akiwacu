--
-- PostgreSQL database dump
--

\restrict IYms5lCIHXB94NBNkM0qfWLWwXfnuhkA4PJGVDK2zmx1EYUL3LgBOQy8QNX4HGz

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adhesions; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.adhesions (
    id bigint NOT NULL,
    membre_id bigint NOT NULL,
    cycle_id bigint NOT NULL,
    date_adhesion date NOT NULL,
    statut character varying(20) NOT NULL,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_adhesion_statut CHECK (((statut)::text = ANY ((ARRAY['ACTIVE'::character varying, 'CLOTUREE'::character varying])::text[])))
);


ALTER TABLE public.adhesions OWNER TO akiwacu;

--
-- Name: adhesions_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.adhesions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.adhesions_id_seq OWNER TO akiwacu;

--
-- Name: adhesions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.adhesions_id_seq OWNED BY public.adhesions.id;


--
-- Name: cotisations; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.cotisations (
    id bigint NOT NULL,
    cycle_id bigint NOT NULL,
    membre_id bigint NOT NULL,
    montant numeric(15,2) NOT NULL,
    date_cotisation date NOT NULL,
    mode_paiement character varying(20) NOT NULL,
    valide_par_id bigint NOT NULL,
    verrouille boolean DEFAULT false NOT NULL,
    recu_id bigint,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_cotisation_mode_paiement CHECK (((mode_paiement)::text = ANY ((ARRAY['ESPECES'::character varying, 'MOBILE_MONEY'::character varying, 'VIREMENT'::character varying])::text[]))),
    CONSTRAINT ck_cotisation_montant CHECK ((montant > (0)::numeric))
);


ALTER TABLE public.cotisations OWNER TO akiwacu;

--
-- Name: cotisations_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.cotisations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cotisations_id_seq OWNER TO akiwacu;

--
-- Name: cotisations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.cotisations_id_seq OWNED BY public.cotisations.id;


--
-- Name: cycles; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.cycles (
    id bigint NOT NULL,
    tontine_id bigint NOT NULL,
    libelle character varying(100) NOT NULL,
    date_debut date NOT NULL,
    date_fin date NOT NULL,
    montant_cotisation numeric(15,2) NOT NULL,
    periodicite character varying(20) NOT NULL,
    statut character varying(20) NOT NULL,
    date_cloture date,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_cycle_dates CHECK ((date_fin > date_debut)),
    CONSTRAINT ck_cycle_montant CHECK ((montant_cotisation > (0)::numeric)),
    CONSTRAINT ck_cycle_periodicite CHECK (((periodicite)::text = ANY ((ARRAY['HEBDOMADAIRE'::character varying, 'MENSUELLE'::character varying])::text[]))),
    CONSTRAINT ck_cycle_statut CHECK (((statut)::text = ANY ((ARRAY['OUVERT'::character varying, 'GELE'::character varying, 'CLOTURE'::character varying])::text[])))
);


ALTER TABLE public.cycles OWNER TO akiwacu;

--
-- Name: cycles_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.cycles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cycles_id_seq OWNER TO akiwacu;

--
-- Name: cycles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.cycles_id_seq OWNED BY public.cycles.id;


--
-- Name: demandes_pret; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.demandes_pret (
    id bigint NOT NULL,
    cycle_id bigint NOT NULL,
    membre_id bigint NOT NULL,
    montant_demande numeric(15,2) NOT NULL,
    duree_mois integer NOT NULL,
    motif text NOT NULL,
    date_demande date NOT NULL,
    statut character varying(20) NOT NULL,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_demande_pret_duree CHECK ((duree_mois > 0)),
    CONSTRAINT ck_demande_pret_montant CHECK ((montant_demande > (0)::numeric)),
    CONSTRAINT ck_demande_pret_statut CHECK (((statut)::text = ANY ((ARRAY['SOUMISE'::character varying, 'APPROUVEE'::character varying, 'REJETEE'::character varying, 'DEBLOQUEE'::character varying])::text[])))
);


ALTER TABLE public.demandes_pret OWNER TO akiwacu;

--
-- Name: demandes_pret_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.demandes_pret_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.demandes_pret_id_seq OWNER TO akiwacu;

--
-- Name: demandes_pret_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.demandes_pret_id_seq OWNED BY public.demandes_pret.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO akiwacu;

--
-- Name: membres; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.membres (
    id bigint NOT NULL,
    tontine_id bigint NOT NULL,
    utilisateur_id bigint,
    numero_membre character varying(30),
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    telephone character varying(20) NOT NULL,
    date_adhesion date NOT NULL,
    statut character varying(20) NOT NULL,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_membre_statut CHECK (((statut)::text = ANY ((ARRAY['ACTIF'::character varying, 'SUSPENDU'::character varying, 'SORTI'::character varying])::text[])))
);


ALTER TABLE public.membres OWNER TO akiwacu;

--
-- Name: membres_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.membres_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.membres_id_seq OWNER TO akiwacu;

--
-- Name: membres_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.membres_id_seq OWNED BY public.membres.id;


--
-- Name: prets; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.prets (
    id bigint NOT NULL,
    demande_pret_id bigint NOT NULL,
    membre_id bigint NOT NULL,
    cycle_id bigint NOT NULL,
    montant_accorde numeric(15,2) NOT NULL,
    taux_interet numeric(5,2) DEFAULT 0 NOT NULL,
    date_deblocage date NOT NULL,
    date_echeance date NOT NULL,
    statut character varying(20) NOT NULL,
    valide_par_id bigint NOT NULL,
    verrouille boolean DEFAULT false NOT NULL,
    recu_id bigint,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    duree_mois integer NOT NULL,
    CONSTRAINT ck_pret_montant CHECK ((montant_accorde > (0)::numeric)),
    CONSTRAINT ck_pret_statut CHECK (((statut)::text = ANY ((ARRAY['ACTIF'::character varying, 'SOLDE'::character varying, 'EN_RETARD'::character varying])::text[]))),
    CONSTRAINT prets_duree_mois_check CHECK ((duree_mois > 0))
);


ALTER TABLE public.prets OWNER TO akiwacu;

--
-- Name: prets_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.prets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.prets_id_seq OWNER TO akiwacu;

--
-- Name: prets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.prets_id_seq OWNED BY public.prets.id;


--
-- Name: recus; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.recus (
    id bigint NOT NULL,
    numero character varying(30) NOT NULL,
    type_operation character varying(20) NOT NULL,
    operation_id bigint NOT NULL,
    membre_id bigint NOT NULL,
    montant numeric(15,2) NOT NULL,
    date_emission timestamp with time zone NOT NULL,
    emis_par_id bigint NOT NULL,
    chemin_fichier character varying(255),
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    contenu_pdf bytea,
    CONSTRAINT ck_recu_type_operation CHECK (((type_operation)::text = ANY ((ARRAY['COTISATION'::character varying, 'DEBLOCAGE_PRET'::character varying, 'REMBOURSEMENT'::character varying])::text[])))
);


ALTER TABLE public.recus OWNER TO akiwacu;

--
-- Name: recus_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.recus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.recus_id_seq OWNER TO akiwacu;

--
-- Name: recus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.recus_id_seq OWNED BY public.recus.id;


--
-- Name: remboursements; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.remboursements (
    id bigint NOT NULL,
    pret_id bigint NOT NULL,
    montant numeric(15,2) NOT NULL,
    date_remboursement date NOT NULL,
    valide_par_id bigint NOT NULL,
    verrouille boolean DEFAULT false NOT NULL,
    recu_id bigint,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_remboursement_montant CHECK ((montant > (0)::numeric))
);


ALTER TABLE public.remboursements OWNER TO akiwacu;

--
-- Name: remboursements_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.remboursements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.remboursements_id_seq OWNER TO akiwacu;

--
-- Name: remboursements_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.remboursements_id_seq OWNED BY public.remboursements.id;


--
-- Name: schema_info; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.schema_info (
    id bigint NOT NULL,
    description character varying(255) NOT NULL,
    applied_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.schema_info OWNER TO akiwacu;

--
-- Name: schema_info_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.schema_info_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.schema_info_id_seq OWNER TO akiwacu;

--
-- Name: schema_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.schema_info_id_seq OWNED BY public.schema_info.id;


--
-- Name: seq_numero_recu; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.seq_numero_recu
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_numero_recu OWNER TO akiwacu;

--
-- Name: tontines; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.tontines (
    id bigint NOT NULL,
    nom character varying(150) NOT NULL,
    description text,
    date_creation date NOT NULL,
    statut character varying(20) NOT NULL,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_tontine_statut CHECK (((statut)::text = ANY ((ARRAY['ACTIVE'::character varying, 'SUSPENDUE'::character varying, 'CLOTUREE'::character varying])::text[])))
);


ALTER TABLE public.tontines OWNER TO akiwacu;

--
-- Name: tontines_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.tontines_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tontines_id_seq OWNER TO akiwacu;

--
-- Name: tontines_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.tontines_id_seq OWNED BY public.tontines.id;


--
-- Name: transactions_caisse; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.transactions_caisse (
    id bigint NOT NULL,
    tontine_id bigint NOT NULL,
    cycle_id bigint,
    sens character varying(10) NOT NULL,
    montant numeric(15,2) NOT NULL,
    motif character varying(255) NOT NULL,
    date_transaction date NOT NULL,
    valide_par_id bigint NOT NULL,
    reference_operation character varying(50),
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_caisse_montant CHECK ((montant > (0)::numeric)),
    CONSTRAINT ck_caisse_sens CHECK (((sens)::text = ANY ((ARRAY['ENTREE'::character varying, 'SORTIE'::character varying])::text[])))
);


ALTER TABLE public.transactions_caisse OWNER TO akiwacu;

--
-- Name: transactions_caisse_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.transactions_caisse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transactions_caisse_id_seq OWNER TO akiwacu;

--
-- Name: transactions_caisse_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.transactions_caisse_id_seq OWNED BY public.transactions_caisse.id;


--
-- Name: utilisateur_roles; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.utilisateur_roles (
    utilisateur_id bigint NOT NULL,
    role character varying(20) NOT NULL,
    CONSTRAINT ck_utilisateur_role CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'GESTIONNAIRE'::character varying, 'TRESORIER'::character varying, 'COMMISSAIRE'::character varying, 'MEMBRE'::character varying])::text[])))
);


ALTER TABLE public.utilisateur_roles OWNER TO akiwacu;

--
-- Name: utilisateurs; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.utilisateurs (
    id bigint NOT NULL,
    tontine_id bigint NOT NULL,
    email character varying(150) NOT NULL,
    mot_de_passe character varying(100) NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    telephone character varying(20),
    actif boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL
);


ALTER TABLE public.utilisateurs OWNER TO akiwacu;

--
-- Name: utilisateurs_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.utilisateurs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.utilisateurs_id_seq OWNER TO akiwacu;

--
-- Name: utilisateurs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.utilisateurs_id_seq OWNED BY public.utilisateurs.id;


--
-- Name: votes_commissaire; Type: TABLE; Schema: public; Owner: akiwacu
--

CREATE TABLE public.votes_commissaire (
    id bigint NOT NULL,
    demande_pret_id bigint NOT NULL,
    commissaire_id bigint NOT NULL,
    sens character varying(10) NOT NULL,
    commentaire text,
    date_vote timestamp with time zone NOT NULL,
    created_at timestamp with time zone,
    created_by character varying(100),
    updated_at timestamp with time zone,
    updated_by character varying(100),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_vote_sens CHECK (((sens)::text = ANY ((ARRAY['POUR'::character varying, 'CONTRE'::character varying])::text[])))
);


ALTER TABLE public.votes_commissaire OWNER TO akiwacu;

--
-- Name: votes_commissaire_id_seq; Type: SEQUENCE; Schema: public; Owner: akiwacu
--

CREATE SEQUENCE public.votes_commissaire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.votes_commissaire_id_seq OWNER TO akiwacu;

--
-- Name: votes_commissaire_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: akiwacu
--

ALTER SEQUENCE public.votes_commissaire_id_seq OWNED BY public.votes_commissaire.id;


--
-- Name: adhesions id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.adhesions ALTER COLUMN id SET DEFAULT nextval('public.adhesions_id_seq'::regclass);


--
-- Name: cotisations id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cotisations ALTER COLUMN id SET DEFAULT nextval('public.cotisations_id_seq'::regclass);


--
-- Name: cycles id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cycles ALTER COLUMN id SET DEFAULT nextval('public.cycles_id_seq'::regclass);


--
-- Name: demandes_pret id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.demandes_pret ALTER COLUMN id SET DEFAULT nextval('public.demandes_pret_id_seq'::regclass);


--
-- Name: membres id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.membres ALTER COLUMN id SET DEFAULT nextval('public.membres_id_seq'::regclass);


--
-- Name: prets id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets ALTER COLUMN id SET DEFAULT nextval('public.prets_id_seq'::regclass);


--
-- Name: recus id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.recus ALTER COLUMN id SET DEFAULT nextval('public.recus_id_seq'::regclass);


--
-- Name: remboursements id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.remboursements ALTER COLUMN id SET DEFAULT nextval('public.remboursements_id_seq'::regclass);


--
-- Name: schema_info id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.schema_info ALTER COLUMN id SET DEFAULT nextval('public.schema_info_id_seq'::regclass);


--
-- Name: tontines id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.tontines ALTER COLUMN id SET DEFAULT nextval('public.tontines_id_seq'::regclass);


--
-- Name: transactions_caisse id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.transactions_caisse ALTER COLUMN id SET DEFAULT nextval('public.transactions_caisse_id_seq'::regclass);


--
-- Name: utilisateurs id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.utilisateurs ALTER COLUMN id SET DEFAULT nextval('public.utilisateurs_id_seq'::regclass);


--
-- Name: votes_commissaire id; Type: DEFAULT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.votes_commissaire ALTER COLUMN id SET DEFAULT nextval('public.votes_commissaire_id_seq'::regclass);


--
-- Name: adhesions adhesions_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.adhesions
    ADD CONSTRAINT adhesions_pkey PRIMARY KEY (id);


--
-- Name: cotisations cotisations_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cotisations
    ADD CONSTRAINT cotisations_pkey PRIMARY KEY (id);


--
-- Name: cycles cycles_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cycles
    ADD CONSTRAINT cycles_pkey PRIMARY KEY (id);


--
-- Name: demandes_pret demandes_pret_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.demandes_pret
    ADD CONSTRAINT demandes_pret_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: membres membres_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.membres
    ADD CONSTRAINT membres_pkey PRIMARY KEY (id);


--
-- Name: prets prets_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets
    ADD CONSTRAINT prets_pkey PRIMARY KEY (id);


--
-- Name: recus recus_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.recus
    ADD CONSTRAINT recus_pkey PRIMARY KEY (id);


--
-- Name: remboursements remboursements_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.remboursements
    ADD CONSTRAINT remboursements_pkey PRIMARY KEY (id);


--
-- Name: schema_info schema_info_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.schema_info
    ADD CONSTRAINT schema_info_pkey PRIMARY KEY (id);


--
-- Name: tontines tontines_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.tontines
    ADD CONSTRAINT tontines_pkey PRIMARY KEY (id);


--
-- Name: transactions_caisse transactions_caisse_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.transactions_caisse
    ADD CONSTRAINT transactions_caisse_pkey PRIMARY KEY (id);


--
-- Name: adhesions uq_adhesion_membre_cycle; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.adhesions
    ADD CONSTRAINT uq_adhesion_membre_cycle UNIQUE (membre_id, cycle_id);


--
-- Name: membres uq_membre_tontine_numero; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.membres
    ADD CONSTRAINT uq_membre_tontine_numero UNIQUE (tontine_id, numero_membre);


--
-- Name: prets uq_pret_demande_pret; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets
    ADD CONSTRAINT uq_pret_demande_pret UNIQUE (demande_pret_id);


--
-- Name: recus uq_recu_numero; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.recus
    ADD CONSTRAINT uq_recu_numero UNIQUE (numero);


--
-- Name: recus uq_recu_operation; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.recus
    ADD CONSTRAINT uq_recu_operation UNIQUE (type_operation, operation_id);


--
-- Name: tontines uq_tontine_nom; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.tontines
    ADD CONSTRAINT uq_tontine_nom UNIQUE (nom);


--
-- Name: utilisateurs uq_utilisateur_email; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.utilisateurs
    ADD CONSTRAINT uq_utilisateur_email UNIQUE (email);


--
-- Name: votes_commissaire uq_vote_demande_commissaire; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.votes_commissaire
    ADD CONSTRAINT uq_vote_demande_commissaire UNIQUE (demande_pret_id, commissaire_id);


--
-- Name: utilisateur_roles utilisateur_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.utilisateur_roles
    ADD CONSTRAINT utilisateur_roles_pkey PRIMARY KEY (utilisateur_id, role);


--
-- Name: utilisateurs utilisateurs_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.utilisateurs
    ADD CONSTRAINT utilisateurs_pkey PRIMARY KEY (id);


--
-- Name: votes_commissaire votes_commissaire_pkey; Type: CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.votes_commissaire
    ADD CONSTRAINT votes_commissaire_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_caisse_tontine_cycle; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX idx_caisse_tontine_cycle ON public.transactions_caisse USING btree (tontine_id, cycle_id);


--
-- Name: idx_cotisation_cycle; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX idx_cotisation_cycle ON public.cotisations USING btree (cycle_id);


--
-- Name: idx_cotisation_membre; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX idx_cotisation_membre ON public.cotisations USING btree (membre_id);


--
-- Name: idx_cycle_tontine_statut; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX idx_cycle_tontine_statut ON public.cycles USING btree (tontine_id, statut);


--
-- Name: idx_membre_tontine; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX idx_membre_tontine ON public.membres USING btree (tontine_id);


--
-- Name: idx_pret_membre_statut; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX idx_pret_membre_statut ON public.prets USING btree (membre_id, statut);


--
-- Name: idx_remboursement_pret; Type: INDEX; Schema: public; Owner: akiwacu
--

CREATE INDEX idx_remboursement_pret ON public.remboursements USING btree (pret_id);


--
-- Name: adhesions adhesions_cycle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.adhesions
    ADD CONSTRAINT adhesions_cycle_id_fkey FOREIGN KEY (cycle_id) REFERENCES public.cycles(id);


--
-- Name: adhesions adhesions_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.adhesions
    ADD CONSTRAINT adhesions_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membres(id);


--
-- Name: cotisations cotisations_cycle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cotisations
    ADD CONSTRAINT cotisations_cycle_id_fkey FOREIGN KEY (cycle_id) REFERENCES public.cycles(id);


--
-- Name: cotisations cotisations_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cotisations
    ADD CONSTRAINT cotisations_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membres(id);


--
-- Name: cotisations cotisations_recu_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cotisations
    ADD CONSTRAINT cotisations_recu_id_fkey FOREIGN KEY (recu_id) REFERENCES public.recus(id);


--
-- Name: cotisations cotisations_valide_par_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cotisations
    ADD CONSTRAINT cotisations_valide_par_id_fkey FOREIGN KEY (valide_par_id) REFERENCES public.utilisateurs(id);


--
-- Name: cycles cycles_tontine_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.cycles
    ADD CONSTRAINT cycles_tontine_id_fkey FOREIGN KEY (tontine_id) REFERENCES public.tontines(id);


--
-- Name: demandes_pret demandes_pret_cycle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.demandes_pret
    ADD CONSTRAINT demandes_pret_cycle_id_fkey FOREIGN KEY (cycle_id) REFERENCES public.cycles(id);


--
-- Name: demandes_pret demandes_pret_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.demandes_pret
    ADD CONSTRAINT demandes_pret_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membres(id);


--
-- Name: membres membres_tontine_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.membres
    ADD CONSTRAINT membres_tontine_id_fkey FOREIGN KEY (tontine_id) REFERENCES public.tontines(id);


--
-- Name: membres membres_utilisateur_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.membres
    ADD CONSTRAINT membres_utilisateur_id_fkey FOREIGN KEY (utilisateur_id) REFERENCES public.utilisateurs(id);


--
-- Name: prets prets_cycle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets
    ADD CONSTRAINT prets_cycle_id_fkey FOREIGN KEY (cycle_id) REFERENCES public.cycles(id);


--
-- Name: prets prets_demande_pret_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets
    ADD CONSTRAINT prets_demande_pret_id_fkey FOREIGN KEY (demande_pret_id) REFERENCES public.demandes_pret(id);


--
-- Name: prets prets_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets
    ADD CONSTRAINT prets_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membres(id);


--
-- Name: prets prets_recu_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets
    ADD CONSTRAINT prets_recu_id_fkey FOREIGN KEY (recu_id) REFERENCES public.recus(id);


--
-- Name: prets prets_valide_par_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.prets
    ADD CONSTRAINT prets_valide_par_id_fkey FOREIGN KEY (valide_par_id) REFERENCES public.utilisateurs(id);


--
-- Name: recus recus_emis_par_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.recus
    ADD CONSTRAINT recus_emis_par_id_fkey FOREIGN KEY (emis_par_id) REFERENCES public.utilisateurs(id);


--
-- Name: recus recus_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.recus
    ADD CONSTRAINT recus_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membres(id);


--
-- Name: remboursements remboursements_pret_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.remboursements
    ADD CONSTRAINT remboursements_pret_id_fkey FOREIGN KEY (pret_id) REFERENCES public.prets(id);


--
-- Name: remboursements remboursements_recu_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.remboursements
    ADD CONSTRAINT remboursements_recu_id_fkey FOREIGN KEY (recu_id) REFERENCES public.recus(id);


--
-- Name: remboursements remboursements_valide_par_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.remboursements
    ADD CONSTRAINT remboursements_valide_par_id_fkey FOREIGN KEY (valide_par_id) REFERENCES public.utilisateurs(id);


--
-- Name: transactions_caisse transactions_caisse_cycle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.transactions_caisse
    ADD CONSTRAINT transactions_caisse_cycle_id_fkey FOREIGN KEY (cycle_id) REFERENCES public.cycles(id);


--
-- Name: transactions_caisse transactions_caisse_tontine_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.transactions_caisse
    ADD CONSTRAINT transactions_caisse_tontine_id_fkey FOREIGN KEY (tontine_id) REFERENCES public.tontines(id);


--
-- Name: transactions_caisse transactions_caisse_valide_par_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.transactions_caisse
    ADD CONSTRAINT transactions_caisse_valide_par_id_fkey FOREIGN KEY (valide_par_id) REFERENCES public.utilisateurs(id);


--
-- Name: utilisateur_roles utilisateur_roles_utilisateur_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.utilisateur_roles
    ADD CONSTRAINT utilisateur_roles_utilisateur_id_fkey FOREIGN KEY (utilisateur_id) REFERENCES public.utilisateurs(id);


--
-- Name: utilisateurs utilisateurs_tontine_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.utilisateurs
    ADD CONSTRAINT utilisateurs_tontine_id_fkey FOREIGN KEY (tontine_id) REFERENCES public.tontines(id);


--
-- Name: votes_commissaire votes_commissaire_commissaire_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.votes_commissaire
    ADD CONSTRAINT votes_commissaire_commissaire_id_fkey FOREIGN KEY (commissaire_id) REFERENCES public.utilisateurs(id);


--
-- Name: votes_commissaire votes_commissaire_demande_pret_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: akiwacu
--

ALTER TABLE ONLY public.votes_commissaire
    ADD CONSTRAINT votes_commissaire_demande_pret_id_fkey FOREIGN KEY (demande_pret_id) REFERENCES public.demandes_pret(id);


--
-- PostgreSQL database dump complete
--

\unrestrict IYms5lCIHXB94NBNkM0qfWLWwXfnuhkA4PJGVDK2zmx1EYUL3LgBOQy8QNX4HGz

