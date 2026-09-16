import React, { useState } from 'react';
import { ArrowDownLeft, ArrowUpRight, Plus, Trash2, Edit2, Filter } from 'lucide-react';
import {
  useTransactionsCaisseQuery,
  useSoldeCaisseQuery,
  useCreateTransactionCaisseMutation,
  useUpdateTransactionCaisseMutation,
  useDeleteTransactionCaisseMutation,
  useCyclesQuery,
} from '../api/queries';
import { Montant } from '../components/Montant';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { EmptyState } from '../components/ui-states/EmptyState';
import { ErrorState } from '../components/ui-states/ErrorState';
import type { Schemas } from '../api/client';

export const CaissePage: React.FC = () => {
  const { data: transactions = [], isLoading, isError, error, refetch } = useTransactionsCaisseQuery();
  const { data: soldeData } = useSoldeCaisseQuery();
  const { data: cycles = [] } = useCyclesQuery();

  const createMutation = useCreateTransactionCaisseMutation();
  const updateMutation = useUpdateTransactionCaisseMutation();
  const deleteMutation = useDeleteTransactionCaisseMutation();

  const [selectedCycleId, setSelectedCycleId] = useState<number | 'ALL'>('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingTx, setEditingTx] = useState<Schemas['TransactionCaisseResponse'] | null>(null);

  // Form states
  const [sens, setSens] = useState<'ENTREE' | 'SORTIE'>('ENTREE');
  const [montant, setMontant] = useState(25000);
  const [dateTransaction, setDateTransaction] = useState(new Date().toISOString().split('T')[0]);
  const [motif, setMotif] = useState('');
  const [cycleId, setCycleId] = useState<number>(cycles[0]?.id || 1);
  const [formError, setFormError] = useState<unknown | null>(null);

  const filteredTransactions = transactions.filter((tx) => {
    if (selectedCycleId === 'ALL') return true;
    return tx.cycleId === selectedCycleId;
  });

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    try {
      await createMutation.mutateAsync({
        sens,
        montant: Number(montant),
        dateTransaction,
        motif,
        cycleId: Number(cycleId),
      });
      setShowCreateModal(false);
      setMotif('');
    } catch (err) {
      setFormError(err);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingTx) return;
    setFormError(null);
    try {
      await updateMutation.mutateAsync({
        id: editingTx.id,
        data: {
          sens,
          montant: Number(montant),
          dateTransaction,
          motif,
          cycleId: Number(cycleId),
        },
      });
      setEditingTx(null);
    } catch (err) {
      setFormError(err);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Voulez-vous supprimer cette écriture de caisse ?')) return;
    try {
      await deleteMutation.mutateAsync(id);
    } catch {
      alert('Impossible de supprimer la transaction.');
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Solde Hero Card - Direction C */}
      <div className="bg-secondary text-primary-foreground rounded-2xl p-6 border border-secondary shadow-md flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6">
        <div>
          <span className="text-xs font-mono uppercase tracking-wider text-muted-foreground">
            Grand Livre · GET /api/transactions-caisse/solde
          </span>
          <div className="text-3xl sm:text-4xl font-extrabold font-heading text-primary tracking-tight mt-1">
            <Montant valeur={soldeData ?? 0} />
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Solde net recalculé strictement par le serveur (Règles R1 & R5)
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
          <span>Mouvement Manuel</span>
        </button>
      </div>

      {/* 2. Filter Bar */}
      <div className="flex items-center space-x-3 bg-card p-4 rounded-2xl border border-border shadow-xs">
        <Filter className="w-4 h-4 text-muted-foreground shrink-0" />
        <span className="text-xs font-heading font-bold text-muted-foreground uppercase">Filtrer par cycle :</span>
        <select
          value={selectedCycleId}
          onChange={(e) => setSelectedCycleId(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
          className="py-1 px-3 rounded-lg border border-border text-xs font-medium text-muted-foreground"
        >
          <option value="ALL">Tous les cycles ({transactions.length} écritures)</option>
          {cycles.map((c) => (
            <option key={c.id} value={c.id}>
              {c.libelle}
            </option>
          ))}
        </select>
      </div>

      {isLoading && <LoadingSkeleton rows={5} type="table" />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} />}

      {!isLoading && filteredTransactions.length === 0 && (
        <EmptyState
          title="Aucun mouvement de caisse"
          description="Les cotisations, déblocages de prêts et remboursements génèrent des écritures dans ce grand livre."
          actionLabel="Ajouter un mouvement"
          onAction={() => setShowCreateModal(true)}
        />
      )}

      {!isLoading && filteredTransactions.length > 0 && (
        <div className="bg-card rounded-2xl border border-border overflow-hidden shadow-xs">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-stone-200 text-sm">
              <thead className="bg-muted text-muted-foreground font-heading text-xs font-semibold uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-4 text-left">Date</th>
                  <th className="py-3.5 px-4 text-left">Type</th>
                  <th className="py-3.5 px-4 text-left">Motif / Justification</th>
                  <th className="py-3.5 px-4 text-right">Montant</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-stone-100 font-sans">
                {filteredTransactions.map((tx) => {
                  const isEntree = tx.sens === 'ENTREE';
                  return (
                    <tr key={tx.id} className="hover:bg-muted/70 transition-colors">
                      <td className="py-3.5 px-4 font-mono text-xs text-muted-foreground">
                        {tx.dateTransaction}
                      </td>
                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-bold font-heading ${
                            isEntree
                              ? 'bg-primary/10 text-foreground border border-primary'
                              : 'bg-destructive/10 text-destructive border border-destructive'
                          }`}
                        >
                          {isEntree ? (
                            <ArrowDownLeft className="w-3 h-3 mr-1 text-foreground" />
                          ) : (
                            <ArrowUpRight className="w-3 h-3 mr-1 text-destructive" />
                          )}
                          {tx.sens}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 font-medium text-foreground">
                        {tx.motif}
                      </td>
                      <td
                        className={`py-3.5 px-4 text-right font-bold font-heading text-sm ${
                          isEntree ? 'text-foreground' : 'text-destructive'
                        }`}
                      >
                        {isEntree ? '+' : '-'} <Montant valeur={tx.montant} />
                      </td>
                      <td className="py-3.5 px-4 text-right space-x-2">
                        <button
                          type="button"
                          onClick={() => {
                            setEditingTx(tx);
                            setSens((tx.sens as 'ENTREE' | 'SORTIE') || 'ENTREE');
                            setMontant(tx.montant || 0);
                            setDateTransaction(tx.dateTransaction || '');
                            setMotif(tx.motif || '');
                            setCycleId(tx.cycleId || cycles[0]?.id || 1);
                            setFormError(null);
                          }}
                          className="p-1 text-muted-foreground hover:text-primary"
                          title="Modifier (PUT /api/transactions-caisse/{id})"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDelete(tx.id!)}
                          className="p-1 text-muted-foreground hover:text-destructive"
                          title="Supprimer (DELETE /api/transactions-caisse/{id})"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* CREATE MODAL (POST /api/transactions-caisse) */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Nouveau mouvement de caisse
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
                  Type d'opération
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setSens('ENTREE')}
                    className={`py-2 px-3 rounded-xl border text-xs font-heading font-bold ${
                      sens === 'ENTREE'
                        ? 'bg-primary/10 text-primary-foreground border-primary'
                        : 'border-border text-muted-foreground'
                    }`}
                  >
                    ENTRÉE (+)
                  </button>
                  <button
                    type="button"
                    onClick={() => setSens('SORTIE')}
                    className={`py-2 px-3 rounded-xl border text-xs font-heading font-bold ${
                      sens === 'SORTIE'
                        ? 'bg-destructive/10 text-primary-foreground border-destructive'
                        : 'border-border text-muted-foreground'
                    }`}
                  >
                    SORTIE (-)
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Montant (BIF)
                </label>
                <input
                  type="number"
                  min="1000"
                  step="1000"
                  required
                  value={montant}
                  onChange={(e) => setMontant(Number(e.target.value))}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-tabular font-bold"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Date
                </label>
                <input
                  type="date"
                  required
                  value={dateTransaction}
                  onChange={(e) => setDateTransaction(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Cycle
                </label>
                <select
                  value={cycleId}
                  onChange={(e) => setCycleId(Number(e.target.value))}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                >
                  {cycles.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.libelle}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Motif / Libellé
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ex: Achat fournitures registre réunion"
                  value={motif}
                  onChange={(e) => setMotif(e.target.value)}
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
                  disabled={createMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {createMutation.isPending ? 'Enregistrement...' : 'Enregistrer (POST)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {editingTx && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Modifier écriture #{editingTx.id}
              </h3>
              <button
                type="button"
                onClick={() => setEditingTx(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError !== null && <ErrorState error={formError} />}

            <form onSubmit={handleUpdate} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Montant (BIF)
                </label>
                <input
                  type="number"
                  required
                  value={montant}
                  onChange={(e) => setMontant(Number(e.target.value))}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-tabular font-bold"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Motif
                </label>
                <input
                  type="text"
                  required
                  value={motif}
                  onChange={(e) => setMotif(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                />
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setEditingTx(null)}
                  className="touch-target px-4 py-2 rounded-xl border border-border text-xs font-medium hover:bg-muted"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={updateMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {updateMutation.isPending ? 'Enregistrement...' : 'Mettre à jour (PUT)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CaissePage;
