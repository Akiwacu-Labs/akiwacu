import React, { useState, useMemo } from 'react';
import {
  FileSpreadsheet,
  Plus,
  Vote as VoteIcon,
  CheckCircle2,
  Check,
  X,
  CreditCard,
} from 'lucide-react';
import {
  useDemandesPretQuery,
  useDemandePretQuery,
  useCreateDemandePretMutation,
  useVotesQuery,
  useVoteQuery,
  useCreateVoteMutation,
  useVoteDecisionQuery,
  usePretsQuery,
  usePretQuery,
  useDebloquerPretMutation,
  usePretEcheancierQuery,
  useRemboursementsParPretQuery,
  useMembresQuery,
  useCyclesQuery,
} from '../api/queries';
import { Montant } from '../components/Montant';
import { StatusBadge } from '../components/ui-states/StatusBadge';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { EmptyState } from '../components/ui-states/EmptyState';
import { ErrorState } from '../components/ui-states/ErrorState';
import { useAuth } from '../context/AuthContext';

export const PretsPage: React.FC = () => {
  const { hasRole } = useAuth();
  const canCreateDemande = hasRole(['MEMBRE']);
  const canVote = hasRole(['COMMISSAIRE']);
  const canDebloquer = hasRole(['TRESORIER']);
  const [statutFilter, setStatutFilter] = useState<'SOUMISE' | 'APPROUVEE' | 'REJETEE' | 'DEBLOQUEE' | undefined>(undefined);

  // Queries
  const { data: demandes = [], isLoading: loadingDemandes, error: errorDemandes, refetch: refetchDemandes } = useDemandesPretQuery(statutFilter);
  const { data: prets = [], isLoading: loadingPrets } = usePretsQuery();
  const { data: membres = [] } = useMembresQuery();
  const { data: cycles = [] } = useCyclesQuery();

  const activeCycle = useMemo(() => cycles.find((c) => c.statut === 'OUVERT') || cycles[0], [cycles]);

  // Mutations
  const createDemandeMutation = useCreateDemandePretMutation();
  const createVoteMutation = useCreateVoteMutation();
  const debloquerMutation = useDebloquerPretMutation();

  // Selected for inspection / action
  const [selectedDemandeId, setSelectedDemandeId] = useState<number | null>(null);
  const [selectedPretId, setSelectedPretId] = useState<number | null>(null);
  const [selectedVoteId, setSelectedVoteId] = useState<number | null>(null);

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDebloquerModal, setShowDebloquerModal] = useState(false);

  // Form states for new demande
  const [formMembreId, setFormMembreId] = useState<number>(membres[0]?.id || 1);
  const [formMontant, setFormMontant] = useState<number>(100000);
  const [formDuree, setFormDuree] = useState<number>(3);
  const [formDateEcheance, setFormDateEcheance] = useState<string>('2026-11-30');
  const [formMotif, setFormMotif] = useState<string>('');
  const [demandeError, setDemandeError] = useState<unknown | null>(null);

  // Vote form states
  const [voteCommentaire, setVoteCommentaire] = useState('');
  const [voteError, setVoteError] = useState<unknown | null>(null);
  const [debloquerError, setDebloquerError] = useState<unknown | null>(null);

  // Queries for selected demande and pret
  const { data: votes = [] } = useVotesQuery(selectedDemandeId || 0);
  const { data: decision } = useVoteDecisionQuery(selectedDemandeId || 0);
  const { data: singleDemande } = useDemandePretQuery(selectedDemandeId || 0);
  const { data: singlePret } = usePretQuery(selectedPretId || 0);
  const { data: singleVote } = useVoteQuery(selectedDemandeId || 0, selectedVoteId || 0);
  const { data: echeancier } = usePretEcheancierQuery(selectedPretId || 0);
  const { data: remboursementsDuPret = [] } = useRemboursementsParPretQuery(selectedPretId || 0);

  const selectedDemande = useMemo(() => singleDemande || demandes.find((d) => d.id === selectedDemandeId), [singleDemande, demandes, selectedDemandeId]);
  const selectedPret = useMemo(() => singlePret || prets.find((p) => p.id === selectedPretId), [singlePret, prets, selectedPretId]);

  // Handle new demande submission
  const handleCreateDemande = async (e: React.FormEvent) => {
    e.preventDefault();
    setDemandeError(null);
    try {
      await createDemandeMutation.mutateAsync({
        membreId: Number(formMembreId),
        montantDemande: Number(formMontant),
        dureeMois: Number(formDuree),
        dateEcheance: formDateEcheance,
        motif: formMotif,
      });
      setShowCreateModal(false);
      setFormMotif('');
    } catch (err) {
      setDemandeError(err);
    }
  };

  // Handle vote
  const handleVote = async (sens: 'POUR' | 'CONTRE') => {
    if (!selectedDemandeId) return;
    setVoteError(null);
    try {
      await createVoteMutation.mutateAsync({
        demandePretId: selectedDemandeId,
        data: {
          sens,
          commentaire: voteCommentaire || undefined,
        },
      });
      setVoteCommentaire('');
    } catch (err) {
      setVoteError(err);
    }
  };

  // Handle déblocage de prêt
  const handleDebloquer = async () => {
    if (!selectedDemandeId) return;
    setDebloquerError(null);
    try {
      await debloquerMutation.mutateAsync({
        demandePretId: selectedDemandeId,
        dateEcheance: formDateEcheance,
      });
      setShowDebloquerModal(false);
      setSelectedDemandeId(null);
    } catch (err) {
      setDebloquerError(err);
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Header with Actions */}
      <div className="bg-card rounded-2xl border border-border p-6 shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs uppercase font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-muted text-foreground">
              Prêts & Échéanciers
            </span>
            <span className="text-xs text-muted-foreground">·</span>
            <span className="text-xs text-muted-foreground font-medium">
              Règles R2, R3, R4, R6, R7 & R8
            </span>
          </div>
          <h1 className="text-2xl font-extrabold text-foreground font-heading mt-2">
            Demandes de prêt & Crédits accordés
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Examen collégial des dossiers, vote des commissaires et déblocage de fonds.
          </p>
        </div>

        {canCreateDemande && <button
          type="button"
          onClick={() => {
            setShowCreateModal(true);
            setDemandeError(null);
          }}
          className="touch-target inline-flex items-center px-4 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground font-heading font-semibold text-sm shadow-xs transition-colors shrink-0"
        >
          <Plus className="w-4 h-4 mr-2" />
          <span>Nouvelle Demande</span>
        </button>}
      </div>

      {/* 2. Status filter tabs (GET /api/demandes-pret?statut=...) */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2">
        <button
          type="button"
          onClick={() => setStatutFilter(undefined)}
          className={`touch-target px-3.5 py-1.5 rounded-xl text-xs font-heading font-semibold transition-colors shrink-0 ${
            statutFilter === undefined
              ? 'bg-secondary text-primary-foreground'
              : 'bg-card text-muted-foreground hover:bg-muted border border-border'
          }`}
        >
          Toutes ({demandes.length})
        </button>

        {(['SOUMISE', 'APPROUVEE', 'DEBLOQUEE', 'REJETEE'] as const).map((st) => (
          <button
            key={st}
            type="button"
            onClick={() => setStatutFilter(st)}
            className={`touch-target px-3.5 py-1.5 rounded-xl text-xs font-heading font-semibold transition-colors shrink-0 ${
              statutFilter === st
                ? 'bg-secondary text-primary-foreground'
                : 'bg-card text-muted-foreground hover:bg-muted border border-border'
            }`}
          >
            <StatusBadge status={st} />
          </button>
        ))}
      </div>

      {/* 3. Main Grid: Demandes list vs Detailed Review Panel */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left column: Demandes list */}
        <div className="lg:col-span-7 space-y-4">
          <div className="bg-card rounded-2xl border border-border p-5 shadow-xs">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-heading font-bold text-base text-foreground">
                Dossiers de demande ({demandes.length})
              </h3>
              <span className="text-xs text-muted-foreground font-mono">
                GET /api/demandes-pret
              </span>
            </div>

            {loadingDemandes && <LoadingSkeleton rows={4} />}
            {errorDemandes && <ErrorState error={errorDemandes} onRetry={() => refetchDemandes()} />}

            {!loadingDemandes && demandes.length === 0 && (
              <EmptyState
                title="Aucune demande de prêt"
                description="Aucun dossier ne correspond au filtre sélectionné. Les membres peuvent soumettre une demande sur le cycle actif."
                actionLabel="Déposer une demande"
                onAction={() => setShowCreateModal(true)}
              />
            )}

            <div className="space-y-3">
              {demandes.map((d) => {
                const membre = membres.find((m) => m.id === d.membreId);
                const isSelected = selectedDemandeId === d.id;

                return (
                  <div
                    key={d.id}
                    onClick={() => {
                      setSelectedDemandeId(d.id!);
                      setSelectedPretId(null);
                    }}
                    className={`p-4 rounded-xl border transition-all cursor-pointer ${
                      isSelected
                        ? 'border-primary bg-primary/10 shadow-xs ring-1 ring-ring'
                        : 'border-border bg-card hover:border-border'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <span className="text-xs font-mono text-muted-foreground">Demande #{d.id} · {d.dateDemande}</span>
                        <h4 className="font-heading font-bold text-base text-foreground mt-0.5">
                          {membre ? `${membre.prenom} ${membre.nom}` : `Membre #${d.membreId}`}
                        </h4>
                      </div>
                      <StatusBadge status={d.statut} />
                    </div>

                    <div className="mt-3 flex items-center justify-between pt-2 border-t border-border">
                      <div>
                        <span className="text-xs text-muted-foreground">Montant demandé :</span>
                        <p className="text-base font-extrabold font-heading text-foreground">
                          <Montant valeur={d.montantDemande} />
                        </p>
                      </div>
                      <div className="text-right">
                        <span className="text-xs text-muted-foreground">Durée :</span>
                        <p className="text-sm font-semibold font-mono text-muted-foreground">
                          {d.dureeMois} mois
                        </p>
                      </div>
                    </div>

                    <p className="mt-2 text-xs text-muted-foreground line-clamp-2 italic bg-muted p-2 rounded-lg">
                      "{d.motif}"
                    </p>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Section: Prêts Débloqués Actifs (GET /api/prets) */}
          <div className="bg-card rounded-2xl border border-border p-5 shadow-xs">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center space-x-2">
                <FileSpreadsheet className="w-5 h-5 text-primary" />
                <h3 className="font-heading font-bold text-base text-foreground">
                  Prêts débloqués & Échéanciers ({prets.length})
                </h3>
              </div>
              <span className="text-xs text-muted-foreground font-mono">GET /api/prets</span>
            </div>

            {loadingPrets && <LoadingSkeleton rows={3} />}

            <div className="space-y-3">
              {prets.map((p) => {
                const membre = membres.find((m) => m.id === p.membreId);
                const isSelected = selectedPretId === p.id;
                return (
                  <div
                    key={p.id}
                    onClick={() => {
                      setSelectedPretId(p.id!);
                      setSelectedDemandeId(null);
                    }}
                    className={`p-4 rounded-xl border transition-all cursor-pointer ${
                      isSelected
                        ? 'border-border bg-muted shadow-xs ring-1 ring-purple-600'
                        : 'border-border bg-card hover:border-border'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <span className="text-xs font-mono text-muted-foreground">Prêt #{p.id} · Débloqué le {p.dateDeblocage}</span>
                        <h4 className="font-heading font-bold text-base text-foreground">
                          {membre ? `${membre.prenom} ${membre.nom}` : `Membre #${p.membreId}`}
                        </h4>
                      </div>
                      <StatusBadge status={p.statut} />
                    </div>

                    <div className="mt-3 flex items-center justify-between text-xs">
                      <div>
                        <span className="text-muted-foreground">Capital accordé :</span>
                        <p className="text-base font-extrabold font-heading text-foreground">
                          <Montant valeur={p.montantAccorde} />
                        </p>
                      </div>
                      <div className="text-right">
                        <span className="text-muted-foreground">Échéance finale (R7) :</span>
                        <p className="font-mono font-bold text-muted-foreground">{p.dateEcheance}</p>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Right column: Inspection Panel (Votes, Decision Quorum, Deblocage or Schedule) */}
        <div className="lg:col-span-5 space-y-4">
          {/* A. If a Demande is selected */}
          {selectedDemande ? (
            <div className="bg-card rounded-2xl border border-border p-6 shadow-xs sticky top-24 space-y-5">
              <div className="flex items-center justify-between pb-3 border-b border-border">
                <div>
                  <span className="text-xs text-muted-foreground font-mono">Détail demande #{selectedDemande.id}</span>
                  <h3 className="text-lg font-bold text-foreground font-heading">
                    Examen du dossier
                  </h3>
                </div>
                <StatusBadge status={selectedDemande.statut} />
              </div>

              {/* R4 Decision Summary (GET /api/demandes-pret/{id}/votes/decision) */}
              <div className="bg-muted rounded-xl p-4 border border-border space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-bold text-muted-foreground font-heading">
                    Règle R4 — Quorum des Commissaires
                  </span>
                  <span className="font-mono text-muted-foreground">2 votes POUR distincts</span>
                </div>

                <div className="grid grid-cols-2 gap-2 mt-2">
                  <div className="p-2.5 rounded-lg bg-primary/10 border border-primary text-center">
                    <span className="text-xs text-foreground font-medium">Votes POUR</span>
                    <p className="text-xl font-bold font-heading text-foreground">
                      {decision?.votesPour ?? votes.filter((v) => v.sens === 'POUR').length}
                    </p>
                  </div>

                  <div className="p-2.5 rounded-lg bg-destructive/10 border border-destructive text-center">
                    <span className="text-xs text-destructive font-medium">Votes CONTRE</span>
                    <p className="text-xl font-bold font-heading text-destructive">
                      {decision?.votesContre ?? votes.filter((v) => v.sens === 'CONTRE').length}
                    </p>
                  </div>
                </div>

                {decision?.quorumAtteint && (
                  <div className="mt-2 p-2 bg-primary/10 border border-primary rounded-lg text-xs text-foreground flex items-center">
                    <CheckCircle2 className="w-4 h-4 mr-1.5 text-foreground shrink-0" />
                    <span>Quorum R4 atteint. Demande éligible au déblocage.</span>
                  </div>
                )}
              </div>

              {/* Votes List (GET /api/demandes-pret/{id}/votes) */}
              <div>
                <h4 className="text-xs font-bold text-muted-foreground uppercase tracking-wider font-heading mb-2">
                  Votes enregistrés ({votes.length})
                </h4>
                {votes.length === 0 ? (
                  <p className="text-xs text-muted-foreground italic">Aucun vote enregistré pour l'instant.</p>
                ) : (
                  <div className="space-y-2">
                    {votes.map((v) => {
                      const isVoteSelected = selectedVoteId === v.id;
                      return (
                        <div
                          key={v.id}
                          onClick={() => setSelectedVoteId(isVoteSelected ? null : v.id)}
                          className={`p-3 rounded-[10px] border text-xs cursor-pointer transition-colors ${
                            isVoteSelected
                              ? 'border-primary bg-primary/10'
                              : 'bg-card border-border hover:border-border'
                          }`}
                        >
                          <div className="flex items-start justify-between">
                            <div>
                              <div className="flex items-center space-x-2">
                                <span className="font-bold text-foreground font-heading">
                                  Commissaire #{v.commissaireId}
                                </span>
                                <StatusBadge status={v.sens} />
                              </div>
                              {v.commentaire && (
                                <p className="text-muted-foreground mt-1 italic">"{v.commentaire}"</p>
                              )}
                            </div>
                            <span className="text-[10px] text-muted-foreground font-mono">
                              {new Date(v.dateVote || '').toLocaleDateString('fr-FR')}
                            </span>
                          </div>

                          {/* Detail of selected vote (GET /api/demandes-pret/{id}/votes/{voteId}) */}
                          {isVoteSelected && singleVote && (
                            <div className="mt-2 pt-2 border-t border-border/80 text-[11px] text-muted-foreground space-y-1">
                              <p className="font-mono text-muted-foreground">GET /api/demandes-pret/{selectedDemandeId}/votes/{singleVote.id}</p>
                              <p><span className="font-semibold">Horodatage précis :</span> {singleVote.dateVote}</p>
                              <p><span className="font-semibold">Statut du scrutin :</span> Enregistré & inaltérable</p>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Vote Actions (POST /api/demandes-pret/{id}/votes) - Commissaire / Admin only */}
              {selectedDemande.statut === 'SOUMISE' && canVote && (
                <div className="pt-4 border-t border-border">
                  <h4 className="text-xs font-bold text-foreground uppercase tracking-wider font-heading mb-2 flex items-center">
                    <VoteIcon className="w-4 h-4 mr-1.5 text-primary" />
                    <span>Exprimer un vote commissaire (R4)</span>
                  </h4>

                  {voteError !== null && <ErrorState error={voteError} />}

                  <div className="space-y-3">
                    <input
                      type="text"
                      placeholder="Commentaire de vote (optionnel)..."
                      value={voteCommentaire}
                      onChange={(e) => setVoteCommentaire(e.target.value)}
                      className="w-full py-2 px-3 text-xs rounded-xl border border-border focus:outline-hidden focus:ring-1 focus:ring-ring"
                    />

                    <div className="grid grid-cols-2 gap-2">
                      <button
                        type="button"
                        onClick={() => handleVote('POUR')}
                        disabled={createVoteMutation.isPending}
                        className="touch-target flex items-center justify-center px-4 py-2.5 rounded-xl bg-primary/10 hover:bg-primary/10 text-primary-foreground font-heading font-semibold text-xs transition-colors shadow-xs"
                      >
                        <Check className="w-4 h-4 mr-1.5" />
                        <span>Voter POUR</span>
                      </button>

                      <button
                        type="button"
                        onClick={() => handleVote('CONTRE')}
                        disabled={createVoteMutation.isPending}
                        className="touch-target flex items-center justify-center px-4 py-2.5 rounded-xl bg-destructive/10 hover:bg-destructive/10 text-primary-foreground font-heading font-semibold text-xs transition-colors shadow-xs"
                      >
                        <X className="w-4 h-4 mr-1.5" />
                        <span>Voter CONTRE</span>
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* Déblocage Action (POST /api/prets) - If APPROUVEE */}
              {selectedDemande.statut === 'APPROUVEE' && canDebloquer && (
                <div className="pt-4 border-t border-border">
                  <div className="p-3 bg-muted border border-border rounded-xl mb-3 text-xs text-foreground">
                    <span className="font-bold">Demande prête au déblocage :</span> Le quorum R4 est atteint. Le Trésorier ou Gestionnaire peut débloquer les fonds (R3 & R7 vérifiées).
                  </div>

                  {debloquerError !== null && <ErrorState error={debloquerError} />}

                  <button
                    type="button"
                    onClick={() => {
                      setFormDateEcheance(activeCycle?.dateFin || '2026-12-31');
                      setShowDebloquerModal(true);
                    }}
                    className="touch-target w-full flex items-center justify-center py-3 px-4 rounded-xl bg-secondary hover:bg-muted text-primary-foreground font-heading font-bold text-sm shadow-md transition-colors"
                  >
                    <CreditCard className="w-4 h-4 mr-2 text-primary" />
                    <span>Débloquer le prêt (POST /api/prets)</span>
                  </button>
                </div>
              )}
            </div>
          ) : selectedPret ? (
            /* B. If a Pret is selected: View Echeancier (GET /api/prets/{id}/echeancier) */
            <div className="bg-card rounded-2xl border border-border p-6 shadow-xs sticky top-24 space-y-5">
              <div className="flex items-center justify-between pb-3 border-b border-border">
                <div>
                  <span className="text-xs text-muted-foreground font-mono">Détail Prêt #{selectedPret.id}</span>
                  <h3 className="text-lg font-bold text-foreground font-heading">
                    Échéancier & Solde restant
                  </h3>
                </div>
                <StatusBadge status={selectedPret.statut} />
              </div>

              {echeancier && (
                <div className="space-y-4">
                  <div className="p-4 bg-muted rounded-xl border border-border space-y-3">
                    <div className="flex justify-between text-xs">
                      <span className="text-muted-foreground">Montant total dû (D-25) :</span>
                      <span className="font-bold font-heading text-foreground">
                        <Montant valeur={echeancier.montantDu} />
                      </span>
                    </div>

                    <div className="flex justify-between text-xs">
                      <span className="text-muted-foreground">Solde restant à rembourser :</span>
                      <span className="font-extrabold font-heading text-primary text-sm">
                        <Montant valeur={echeancier.soldeRestant} />
                      </span>
                    </div>

                    <div className="flex justify-between text-xs pt-2 border-t border-border">
                      <span className="text-muted-foreground">Date déblocage :</span>
                      <span className="font-mono text-muted-foreground">{echeancier.dateDeblocage}</span>
                    </div>

                    <div className="flex justify-between text-xs">
                      <span className="text-muted-foreground">Date d'échéance finale (R7) :</span>
                      <span className="font-mono font-bold text-foreground">{echeancier.dateEcheance}</span>
                    </div>
                  </div>

                  <div className="p-3 bg-primary/10 rounded-[10px] border border-primary text-xs text-primary-foreground">
                    <span className="font-bold">Règle R7 appliquée :</span> La date d’échéance ({echeancier.dateEcheance}) est strictement inférieure ou égale à la date de fin du cycle ({activeCycle?.dateFin}).
                  </div>

                  {/* Remboursements associés au prêt (GET /api/remboursements/pret/{pretId}) */}
                  <div className="pt-3 border-t border-border">
                    <div className="flex items-center justify-between mb-2">
                      <h4 className="text-xs font-bold text-muted-foreground uppercase tracking-wider font-heading">
                        Remboursements perçus ({remboursementsDuPret.length})
                      </h4>
                      <span className="text-[10px] font-mono text-muted-foreground">GET /api/remboursements/pret/{selectedPret.id}</span>
                    </div>

                    {remboursementsDuPret.length === 0 ? (
                      <p className="text-xs text-muted-foreground italic">Aucun remboursement effectué pour le moment.</p>
                    ) : (
                      <div className="space-y-1.5">
                        {remboursementsDuPret.map((r) => (
                          <div key={r.id} className="p-2.5 rounded-[10px] bg-muted border border-border flex items-center justify-between text-xs">
                            <div>
                              <span className="font-heading font-bold text-foreground">
                                <Montant valeur={r.montant} />
                              </span>
                              <span className="text-[10px] text-muted-foreground font-mono ml-2">
                                {r.dateRemboursement}
                              </span>
                            </div>
                            {r.verrouille && (
                              <span className="text-[10px] font-mono px-1.5 py-0.5 rounded-[6px] bg-muted text-muted-foreground">
                                R8 Verrouillé
                              </span>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          ) : (
            /* C. Default placeholder when nothing selected */
            <div className="bg-muted rounded-2xl border border-dashed border-border p-8 text-center text-muted-foreground">
              <FileSpreadsheet className="w-8 h-8 mx-auto mb-2 text-muted-foreground" />
              <p className="font-heading font-medium text-muted-foreground">Sélectionnez un dossier</p>
              <p className="text-xs mt-1 text-muted-foreground">
                Cliquez sur une demande ou un prêt pour examiner les votes, le quorum R4 ou l’échéancier.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* 4. MODAL CREATION DEMANDE (POST /api/demandes-pret) */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <div>
                <span className="text-xs font-mono text-primary font-bold">POST /api/demandes-pret</span>
                <h3 className="font-heading font-bold text-lg text-foreground">
                  Nouvelle demande de prêt
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {demandeError !== null && <ErrorState error={demandeError} />}

            <form onSubmit={handleCreateDemande} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Membre demandeur
                </label>
                <select
                  value={formMembreId}
                  onChange={(e) => setFormMembreId(Number(e.target.value))}
                  className="w-full py-2.5 px-3 rounded-xl border border-border text-sm"
                >
                  {membres.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.prenom} {m.nom} ({m.numeroMembre || 'Sans N°'})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Montant demandé (BIF)
                  </label>
                  <input
                    type="number"
                    min="10000"
                    step="10000"
                    value={formMontant}
                    onChange={(e) => setFormMontant(Number(e.target.value))}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-tabular font-bold"
                  />
                  <span className="text-[10px] text-muted-foreground">R6 : Max 3x épargne</span>
                </div>

                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Durée (mois)
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="12"
                    value={formDuree}
                    onChange={(e) => setFormDuree(Number(e.target.value))}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Date d'échéance (R7)
                </label>
                <input
                  type="date"
                  value={formDateEcheance}
                  onChange={(e) => setFormDateEcheance(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                />
                <span className="text-[10px] text-muted-foreground">Doit être ≤ fin de cycle ({activeCycle?.dateFin})</span>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Motif du prêt
                </label>
                <textarea
                  rows={3}
                  required
                  value={formMotif}
                  onChange={(e) => setFormMotif(e.target.value)}
                  placeholder="Justification économique ou sociale..."
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                />
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="touch-target px-4 py-2 rounded-xl border border-border text-xs font-medium hover:bg-muted"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={createDemandeMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {createDemandeMutation.isPending ? 'Soumission...' : 'Soumettre (POST)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 5. MODAL DEBLOCAGE PRET (POST /api/prets) */}
      {showDebloquerModal && selectedDemande && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <div>
                <span className="text-xs font-mono text-foreground font-bold">POST /api/prets</span>
                <h3 className="font-heading font-bold text-lg text-foreground">
                  Débloquer le prêt
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setShowDebloquerModal(false)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            <p className="text-xs text-muted-foreground mb-4">
              Confirmez le décaissement de <span className="font-bold text-foreground"><Montant valeur={selectedDemande.montantDemande} /></span> pour le membre #{selectedDemande.membreId}.
            </p>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Date d'échéance confirmée (R7)
                </label>
                <input
                  type="date"
                  value={formDateEcheance}
                  onChange={(e) => setFormDateEcheance(e.target.value)}
                  className="w-full py-2.5 px-3 rounded-xl border border-border text-sm font-mono"
                />
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowDebloquerModal(false)}
                  className="touch-target px-4 py-2 rounded-xl border border-border text-xs font-medium hover:bg-muted"
                >
                  Annuler
                </button>
                <button
                  type="button"
                  onClick={handleDebloquer}
                  disabled={debloquerMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-muted hover:bg-muted text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {debloquerMutation.isPending ? 'Déblocage...' : 'Confirmer Déblocage (POST)'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PretsPage;
