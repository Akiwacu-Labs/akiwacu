import React, { useState } from 'react';
import { Plus, Play, Pause, CheckCircle2, Edit2 } from 'lucide-react';
import {
  useCyclesQuery,
  useCycleQuery,
  useCreateCycleMutation,
  useUpdateCycleMutation,
  usePatchCycleStatutMutation,
} from '../api/queries';
import { Montant } from '../components/Montant';
import { StatusBadge } from '../components/ui-states/StatusBadge';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { EmptyState } from '../components/ui-states/EmptyState';
import { ErrorState } from '../components/ui-states/ErrorState';
import type { Schemas } from '../api/client';

export const CyclesPage: React.FC = () => {
  const { data: cycles = [], isLoading, isError, error, refetch } = useCyclesQuery();
  const createMutation = useCreateCycleMutation();
  const updateMutation = useUpdateCycleMutation();
  const patchStatutMutation = usePatchCycleStatutMutation();

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingCycle, setEditingCycle] = useState<Schemas['CycleResponse'] | null>(null);
  const [inspectCycleId, setInspectCycleId] = useState<number | null>(null);

  const { data: inspectedCycle, isLoading: loadingInspected } = useCycleQuery(inspectCycleId || 0);

  // Form states
  const [libelle, setLibelle] = useState('');
  const [dateDebut, setDateDebut] = useState('2026-01-01');
  const [dateFin, setDateFin] = useState('2026-12-31');
  const [montantCotisation, setMontantCotisation] = useState(50000);
  const [periodicite, setPeriodicite] = useState<'MENSUELLE' | 'HEBDOMADAIRE'>('MENSUELLE');
  const [formError, setFormError] = useState<unknown | null>(null);
  const [statutTransitionError, setStatutTransitionError] = useState<unknown | null>(null);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    try {
      await createMutation.mutateAsync({
        libelle,
        dateDebut,
        dateFin,
        montantCotisation: Number(montantCotisation),
        periodicite,
      });
      setShowCreateModal(false);
      setLibelle('');
    } catch (err) {
      setFormError(err);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingCycle) return;
    setFormError(null);
    try {
      await updateMutation.mutateAsync({
        id: editingCycle.id,
        data: {
          libelle,
          dateDebut,
          dateFin,
          montantCotisation: Number(montantCotisation),
          periodicite,
        },
      });
      setEditingCycle(null);
    } catch (err) {
      setFormError(err);
    }
  };

  const handleTransitionStatut = async (cycleId: number, nouveauStatut: 'OUVERT' | 'GELE' | 'CLOTURE') => {
    setStatutTransitionError(null);
    try {
      await patchStatutMutation.mutateAsync({
        id: cycleId,
        data: { statut: nouveauStatut },
      });
    } catch (err) {
      setStatutTransitionError(err);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-card rounded-2xl border border-border p-6 shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs uppercase font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-primary/10 text-primary-foreground">
              Garde de cycle (R2 & R3)
            </span>
            <span className="text-xs text-muted-foreground">·</span>
            <span className="text-xs text-muted-foreground font-medium">GET /api/cycles</span>
          </div>
          <h1 className="text-2xl font-extrabold text-foreground font-heading mt-2">
            Cycles d'épargne & Machine à états
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Chaque cycle encadre les cotisations et échéances de prêts. Statuts : OUVERT, GELE, CLOTURE.
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            setShowCreateModal(true);
            setFormError(null);
          }}
          className="touch-target inline-flex items-center px-4 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground font-heading font-semibold text-sm shadow-xs transition-colors shrink-0"
        >
          <Plus className="w-4 h-4 mr-2" />
          <span>Nouveau Cycle</span>
        </button>
      </div>

      {statutTransitionError !== null && <ErrorState error={statutTransitionError} />}
      {isLoading && <LoadingSkeleton rows={3} />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} />}

      {!isLoading && cycles.length === 0 && (
        <EmptyState
          title="Aucun cycle configuré"
          description="Créez un cycle pour démarrer les opérations de la tontine."
          actionLabel="Créer un cycle"
          onAction={() => setShowCreateModal(true)}
        />
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {cycles.map((cycle) => {
          const isOpen = cycle.statut === 'OUVERT';
          const isFrozen = cycle.statut === 'GELE';
          const isClosed = cycle.statut === 'CLOTURE';

          return (
            <div
              key={cycle.id}
              className={`rounded-2xl border p-6 shadow-xs flex flex-col justify-between transition-all ${
                isOpen
                  ? 'bg-card border-primary ring-1 ring-amber-300'
                  : 'bg-card border-border'
              }`}
            >
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <span className="text-xs font-mono text-muted-foreground">Cycle #{cycle.id}</span>
                    <h3 className="text-lg font-bold font-heading text-foreground mt-0.5">
                      {cycle.libelle}
                    </h3>
                  </div>
                  <StatusBadge status={cycle.statut} />
                </div>

                <div className="mt-4 space-y-2 text-xs">
                  <div className="flex justify-between py-1 border-b border-border">
                    <span className="text-muted-foreground">Cotisation périodique :</span>
                    <span className="font-bold font-heading text-foreground">
                      <Montant valeur={cycle.montantCotisation} /> / {cycle.periodicite?.toLowerCase()}
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-border">
                    <span className="text-muted-foreground">Date de début :</span>
                    <span className="font-mono text-muted-foreground">{cycle.dateDebut}</span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-border">
                    <span className="text-muted-foreground">Date de fin (Plafond R7) :</span>
                    <span className="font-mono font-bold text-foreground">{cycle.dateFin}</span>
                  </div>
                </div>

                {isOpen && (
                  <div className="mt-4 p-3 bg-primary/10 border border-primary rounded-xl text-xs text-foreground">
                    <span className="font-bold">Cycle Actif :</span> Toutes les cotisations et déblocages de prêts sont autorisés conformément à R2 et R3.
                  </div>
                )}

                {isFrozen && (
                  <div className="mt-4 p-3 bg-primary/10 border border-primary rounded-xl text-xs text-primary-foreground">
                    <span className="font-bold">Cycle Gelé :</span> Aucun nouveau prêt ne peut être débloqué (R3). Les remboursements restent autorisés.
                  </div>
                )}

                {isClosed && (
                  <div className="mt-4 p-3 bg-muted border border-border rounded-xl text-xs text-muted-foreground">
                    <span className="font-bold">Cycle Clôturé :</span> Toutes les opérations d'écriture sont verrouillées (R2 rejet 409).
                  </div>
                )}
              </div>

              {/* Machine à états : PATCH /api/cycles/{id}/statut */}
              <div className="mt-6 pt-4 border-t border-border flex items-center justify-between">
                <span className="text-xs text-muted-foreground font-mono">Transition :</span>

                <div className="flex items-center space-x-2">
                  {isOpen && (
                    <button
                      type="button"
                      onClick={() => handleTransitionStatut(cycle.id!, 'GELE')}
                      disabled={patchStatutMutation.isPending}
                      className="touch-target px-3 py-1.5 rounded-lg border border-primary text-primary-foreground bg-primary/10 hover:bg-primary/10 text-xs font-semibold"
                    >
                      <Pause className="w-3.5 h-3.5 inline mr-1" />
                      Geler cycle
                    </button>
                  )}

                  {isFrozen && (
                    <>
                      <button
                        type="button"
                        onClick={() => handleTransitionStatut(cycle.id!, 'OUVERT')}
                        disabled={patchStatutMutation.isPending}
                        className="touch-target px-3 py-1.5 rounded-lg border border-primary text-foreground bg-primary/10 hover:bg-primary/10 text-xs font-semibold"
                      >
                        <Play className="w-3.5 h-3.5 inline mr-1" />
                        Rouvrir
                      </button>

                      <button
                        type="button"
                        onClick={() => handleTransitionStatut(cycle.id!, 'CLOTURE')}
                        disabled={patchStatutMutation.isPending}
                        className="touch-target px-3 py-1.5 rounded-lg border border-border text-muted-foreground bg-muted hover:bg-muted text-xs font-semibold"
                      >
                        <CheckCircle2 className="w-3.5 h-3.5 inline mr-1" />
                        Clôturer définitivement
                      </button>
                    </>
                  )}

                  <button
                    type="button"
                    onClick={() => setInspectCycleId(cycle.id!)}
                    className="touch-target px-2.5 py-1.5 rounded-[6px] border border-border text-muted-foreground bg-card hover:bg-muted text-xs font-semibold"
                  >
                    Consulter
                  </button>

                  <button
                    type="button"
                    onClick={() => {
                      setEditingCycle(cycle);
                      setLibelle(cycle.libelle);
                      setDateDebut(cycle.dateDebut);
                      setDateFin(cycle.dateFin);
                      setMontantCotisation(cycle.montantCotisation);
                      setPeriodicite(cycle.periodicite as 'HEBDOMADAIRE' | 'MENSUELLE');
                      setFormError(null);
                    }}
                    className="touch-target p-2 rounded-[6px] border border-border text-muted-foreground hover:bg-muted text-xs"
                    title="Modifier les paramètres (PUT /api/cycles/{id})"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* CREATE MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Nouveau cycle d'épargne (POST /api/cycles)
              </h3>
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError !== null && <ErrorState error={formError} />}

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Libellé du cycle
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ex: Cycle Annuel 2027"
                  value={libelle}
                  onChange={(e) => setLibelle(e.target.value)}
                  className="w-full py-2.5 px-3 rounded-xl border border-border text-sm"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Date de début
                  </label>
                  <input
                    type="date"
                    required
                    value={dateDebut}
                    onChange={(e) => setDateDebut(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Date de fin (R7)
                  </label>
                  <input
                    type="date"
                    required
                    value={dateFin}
                    onChange={(e) => setDateFin(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Montant cotisation
                  </label>
                  <input
                    type="number"
                    min="5000"
                    step="5000"
                    required
                    value={montantCotisation}
                    onChange={(e) => setMontantCotisation(Number(e.target.value))}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-tabular font-bold"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Périodicité
                  </label>
                  <select
                    value={periodicite}
                    onChange={(e) => setPeriodicite(e.target.value as 'HEBDOMADAIRE' | 'MENSUELLE')}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                  >
                    <option value="MENSUELLE">MENSUELLE</option>
                    <option value="HEBDOMADAIRE">HEBDOMADAIRE</option>
                  </select>
                </div>
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
                  disabled={createMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {createMutation.isPending ? 'Création...' : 'Créer Cycle (POST)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {editingCycle && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Modifier cycle #{editingCycle.id}
              </h3>
              <button
                type="button"
                onClick={() => setEditingCycle(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError !== null && <ErrorState error={formError} />}

            <form onSubmit={handleUpdate} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Libellé
                </label>
                <input
                  type="text"
                  required
                  value={libelle}
                  onChange={(e) => setLibelle(e.target.value)}
                  className="w-full py-2.5 px-3 rounded-xl border border-border text-sm"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Date fin
                  </label>
                  <input
                    type="date"
                    required
                    value={dateFin}
                    onChange={(e) => setDateFin(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Cotisation
                  </label>
                  <input
                    type="number"
                    step="5000"
                    required
                    value={montantCotisation}
                    onChange={(e) => setMontantCotisation(Number(e.target.value))}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-tabular font-bold"
                  />
                </div>
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setEditingCycle(null)}
                  className="touch-target px-4 py-2 rounded-xl border border-border text-xs font-medium hover:bg-muted"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={updateMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {updateMutation.isPending ? 'Mise à jour...' : 'Mettre à jour (PUT)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* INSPECT MODAL (GET /api/cycles/{id}) */}
      {inspectCycleId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-[14px] max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <div>
                <span className="text-xs font-mono text-primary font-bold">GET /api/cycles/{inspectCycleId}</span>
                <h3 className="font-heading font-bold text-lg text-foreground">
                  Détail du cycle #{inspectCycleId}
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setInspectCycleId(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {loadingInspected && <LoadingSkeleton rows={3} />}

            {inspectedCycle && (
              <div className="space-y-4">
                <div className="p-4 bg-muted rounded-[10px] border border-border space-y-2.5 text-xs">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Libellé :</span>
                    <span className="font-bold text-foreground">{inspectedCycle.libelle}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Période :</span>
                    <span className="font-mono text-muted-foreground">{inspectedCycle.dateDebut} → {inspectedCycle.dateFin}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Cotisation exigée :</span>
                    <span className="font-bold font-heading text-foreground"><Montant valeur={inspectedCycle.montantCotisation} /></span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Périodicité :</span>
                    <span className="font-semibold text-muted-foreground">{inspectedCycle.periodicite}</span>
                  </div>
                  <div className="flex justify-between items-center pt-2 border-t border-border">
                    <span className="text-muted-foreground">Statut (R2/R3) :</span>
                    <StatusBadge status={inspectedCycle.statut} />
                  </div>
                </div>

                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={() => setInspectCycleId(null)}
                    className="touch-target px-4 py-2 rounded-[6px] bg-muted text-primary-foreground text-xs font-heading font-semibold"
                  >
                    Fermer
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CyclesPage;
