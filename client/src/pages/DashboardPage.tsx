import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  Users,
  Coins,
  FileSpreadsheet,
  Wallet,
  ShieldCheck,
  Calendar,
  CheckCircle2,
  ChevronRight,
  TrendingUp,
} from 'lucide-react';
import { useDashboardQuery } from '../api/queries';
import { Montant } from '../components/Montant';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { ErrorState } from '../components/ui-states/ErrorState';
import { useAuth } from '../context/AuthContext';

export const DashboardPage: React.FC = () => {
  const { data: dashboard, isLoading, isError, error, refetch } = useDashboardQuery();
  const { user, hasRole } = useAuth();
  const canRecordCotisation = hasRole(['ADMIN', 'GESTIONNAIRE', 'TRESORIER']);

  return (
    <div className="space-y-6">
      {/* 1. Header & Welcome message */}
      <div className="bg-card rounded-2xl border border-border/80 p-6 shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs uppercase font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-primary/10 text-primary-foreground">
              GET /api/dashboard
            </span>
            <span className="text-xs text-muted-foreground">·</span>
            <span className="text-xs text-foreground flex items-center font-medium">
              <CheckCircle2 className="w-3.5 h-3.5 mr-1 text-foreground" />
              Tenant isolé (R1)
            </span>
          </div>
          <h1 className="text-2xl font-extrabold text-foreground font-heading mt-2">
            Bonjour, {user?.prenom || 'Utilisateur'}
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Tableau de bord de la tontine courante. Données agrégées calculées par le serveur.
          </p>
        </div>

        {canRecordCotisation && <div className="flex items-center gap-2">
          <NavLink
            to="/cotisations"
            className="touch-target inline-flex items-center px-4 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground font-heading font-semibold text-sm shadow-xs transition-colors"
          >
            <Coins className="w-4 h-4 mr-2" />
            <span>Saisie Cotisations</span>
          </NavLink>
        </div>}
      </div>

      {/* 2. Error state or Loading skeleton */}
      {isLoading && <LoadingSkeleton rows={4} type="table" />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} title="Erreur lors du chargement des indicateurs" />}

      {/* 3. Aggregates Grid - 5 Cards corresponding to OpenAPI DashboardResponse */}
      {dashboard && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {/* Card 1: Solde Caisse (Highlight) */}
          <div className="bg-secondary text-primary-foreground rounded-2xl p-6 border border-secondary shadow-sm lg:col-span-1 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono font-medium text-muted-foreground uppercase tracking-wider">
                Solde réel en caisse
              </span>
              <div className="w-8 h-8 rounded-lg bg-muted flex items-center justify-center text-primary">
                <Wallet className="w-4 h-4" />
              </div>
            </div>
            <div className="my-4">
              <p className="text-3xl font-extrabold font-heading text-primary tracking-tight">
                <Montant valeur={dashboard.soldeCaisse} />
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Entrées - Sorties validées par le Trésorier (R5)
              </p>
            </div>
            <NavLink
              to="/caisse"
              className="text-xs text-muted-foreground hover:text-primary-foreground flex items-center font-heading font-semibold pt-3 border-t border-border"
            >
              <span>Détail du grand livre</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1 text-primary" />
            </NavLink>
          </div>

          {/* Card 2: Cotisations Totales */}
          <div className="bg-card rounded-2xl p-6 border border-border shadow-xs flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono font-medium text-muted-foreground uppercase tracking-wider">
                Cotisations collectées
              </span>
              <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center text-primary">
                <Coins className="w-4 h-4" />
              </div>
            </div>
            <div className="my-4">
              <p className="text-2xl font-bold font-heading text-foreground tracking-tight">
                <Montant valeur={dashboard.cotisationsTotal} />
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Toutes cotisations avec reçus émis (R8)
              </p>
            </div>
            <NavLink
              to="/cotisations"
              className="text-xs text-muted-foreground hover:text-primary flex items-center font-heading font-semibold pt-3 border-t border-border"
            >
              <span>Accéder au Compteur</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </NavLink>
          </div>

          {/* Card 3: Prêts en cours */}
          <div className="bg-card rounded-2xl p-6 border border-border shadow-xs flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono font-medium text-muted-foreground uppercase tracking-wider">
                Prêts débloqués actifs
              </span>
              <div className="w-8 h-8 rounded-lg bg-muted flex items-center justify-center text-foreground">
                <FileSpreadsheet className="w-4 h-4" />
              </div>
            </div>
            <div className="my-4">
              <p className="text-2xl font-bold font-heading text-foreground tracking-tight">
                <Montant valeur={dashboard.pretsEnCours} />
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Plafond 3x épargne (R6) · Échéance cycle (R7)
              </p>
            </div>
            <NavLink
              to="/prets"
              className="text-xs text-muted-foreground hover:text-foreground flex items-center font-heading font-semibold pt-3 border-t border-border"
            >
              <span>Suivi des prêts & votes</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </NavLink>
          </div>

          {/* Card 4: Remboursements Totaux */}
          <div className="bg-card rounded-2xl p-6 border border-border shadow-xs flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono font-medium text-muted-foreground uppercase tracking-wider">
                Remboursements perçus
              </span>
              <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center text-foreground">
                <TrendingUp className="w-4 h-4" />
              </div>
            </div>
            <div className="my-4">
              <p className="text-2xl font-bold font-heading text-foreground tracking-tight">
                <Montant valeur={dashboard.remboursementsTotal} />
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Capital remboursé au fonds commun
              </p>
            </div>
            <NavLink
              to="/remboursements"
              className="text-xs text-muted-foreground hover:text-foreground flex items-center font-heading font-semibold pt-3 border-t border-border"
            >
              <span>Consulter les remboursements</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </NavLink>
          </div>

          {/* Card 5: Membres Actifs */}
          <div className="bg-card rounded-2xl p-6 border border-border shadow-xs flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono font-medium text-muted-foreground uppercase tracking-wider">
                Membres actifs
              </span>
              <div className="w-8 h-8 rounded-lg bg-muted flex items-center justify-center text-foreground">
                <Users className="w-4 h-4" />
              </div>
            </div>
            <div className="my-4">
              <p className="text-2xl font-bold font-heading text-foreground tracking-tight">
                {dashboard.membresActifs || 0} membres
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Inscrits dans la tontine courante
              </p>
            </div>
            <NavLink
              to="/membres"
              className="text-xs text-muted-foreground hover:text-foreground flex items-center font-heading font-semibold pt-3 border-t border-border"
            >
              <span>Registre des membres</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </NavLink>
          </div>

          {/* Card 6: Cycle Actuel Information */}
          <div className="bg-gradient-to-br from-stone-50 to-amber-50/40 rounded-2xl p-6 border border-primary/60 shadow-xs flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono font-medium text-primary-foreground uppercase tracking-wider">
                Cycle actif (R2)
              </span>
              <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center text-primary-foreground">
                <Calendar className="w-4 h-4" />
              </div>
            </div>
            <div className="my-4">
              <p className="text-lg font-bold font-heading text-foreground tracking-tight">
                Cycle Annuel 2026
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Du 01/01/2026 au 31/12/2026 · 50 000 BIF
              </p>
            </div>
            <NavLink
              to="/cycles"
              className="text-xs text-primary-foreground hover:text-primary-foreground flex items-center font-heading font-semibold pt-3 border-t border-primary/50"
            >
              <span>Machine à états du cycle</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </NavLink>
          </div>
        </div>
      )}

      {/* 4. Business Rules Status Section (R1 to R8) */}
      <div className="bg-card rounded-2xl border border-border p-6 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <ShieldCheck className="w-5 h-5 text-primary" />
            <h3 className="text-base font-bold text-foreground font-heading">
              Gouvernance et intégrité métier (Matrice R1 → R8)
            </h3>
          </div>
          <span className="text-xs text-muted-foreground font-mono">100% contrôlé côté serveur</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
          <div className="p-3 bg-muted rounded-xl border border-border">
            <span className="font-bold text-foreground font-mono">R1 — Isolation Tenant</span>
            <p className="text-muted-foreground mt-1">tontineId extrait du JWT. Aucune fuite inter-tontines.</p>
          </div>

          <div className="p-3 bg-muted rounded-xl border border-border">
            <span className="font-bold text-foreground font-mono">R2 & R3 — Garde de Cycle</span>
            <p className="text-muted-foreground mt-1">Opérations financières et déblocage de prêt sur cycle OUVERT.</p>
          </div>

          <div className="p-3 bg-muted rounded-xl border border-border">
            <span className="font-bold text-foreground font-mono">R4 — Quorum 2 Commissaires</span>
            <p className="text-muted-foreground mt-1">Deux votes POUR de commissaires distincts requis pour approbation.</p>
          </div>

          <div className="p-3 bg-muted rounded-xl border border-border">
            <span className="font-bold text-foreground font-mono">R5 & R8 — Reçu & Verrou</span>
            <p className="text-muted-foreground mt-1">Trésorier validateur tracé. Opération avec reçu non modifiable (409).</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
