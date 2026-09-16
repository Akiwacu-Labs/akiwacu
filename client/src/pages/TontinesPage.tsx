import React, { useState } from 'react';
import { Plus, Trash2, Edit2, Building } from 'lucide-react';
import {
  useTontinesQuery,
  useCreateTontineMutation,
  useUpdateTontineMutation,
  useDeleteTontineMutation,
} from '../api/queries';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { ErrorState } from '../components/ui-states/ErrorState';
import type { Schemas } from '../api/client';

export const TontinesPage: React.FC = () => {
  const { data: tontines = [], isLoading, isError, error, refetch } = useTontinesQuery();
  const createMutation = useCreateTontineMutation();
  const updateMutation = useUpdateTontineMutation();
  const deleteMutation = useDeleteTontineMutation();

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingTontine, setEditingTontine] = useState<Schemas['TontineResponse'] | null>(null);

  // Create form states (with first admin, D-35)
  const [nom, setNom] = useState('');
  const [description, setDescription] = useState('');
  const [dateCreation, setDateCreation] = useState(new Date().toISOString().split('T')[0]);
  const [adminNom, setAdminNom] = useState('');
  const [adminPrenom, setAdminPrenom] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminMotDePasse, setAdminMotDePasse] = useState('Password123!');
  const [adminTelephone, setAdminTelephone] = useState('+257 79 000 000');
  const [formError, setFormError] = useState<unknown | null>(null);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    try {
      await createMutation.mutateAsync({
        nom,
        description,
        administrateur: {
          nom: adminNom,
          prenom: adminPrenom,
          email: adminEmail,
          motDePasse: adminMotDePasse,
          telephone: adminTelephone,
        },
      });
      setShowCreateModal(false);
      setNom('');
      setDescription('');
    } catch (err) {
      setFormError(err);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingTontine) return;
    setFormError(null);
    try {
      await updateMutation.mutateAsync({
        id: editingTontine.id,
        data: {
          nom,
          description,
          dateCreation: editingTontine.dateCreation || new Date().toISOString().split('T')[0],
          statut: (editingTontine.statut as 'ACTIVE' | 'SUSPENDUE' | 'CLOTUREE') || 'ACTIVE',
        },
      });
      setEditingTontine(null);
    } catch (err) {
      setFormError(err);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Supprimer cette tontine et purger toutes ses données ?')) return;
    try {
      await deleteMutation.mutateAsync(id);
    } catch {
      alert('Impossible de supprimer la tontine.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-card rounded-2xl border border-border p-6 shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs uppercase font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-muted text-muted-foreground">
              Multi-tenant (R1)
            </span>
            <span className="text-xs text-muted-foreground">·</span>
            <span className="text-xs text-muted-foreground font-medium">GET /api/tontines</span>
          </div>
          <h1 className="text-2xl font-extrabold text-foreground font-heading mt-2">
            Gestion des Tontines
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Administration des associations communautaires enregistrées sur la plateforme.
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
          <span>Nouvelle Tontine</span>
        </button>
      </div>

      {isLoading && <LoadingSkeleton rows={3} />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} />}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {tontines.map((t) => (
          <div key={t.id} className="bg-card rounded-2xl border border-border p-6 shadow-xs flex flex-col justify-between">
            <div>
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center text-muted-foreground">
                    <Building className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-heading font-bold text-lg text-foreground">
                      {t.nom}
                    </h3>
                    <span className="text-xs font-mono text-muted-foreground">Tenant ID #{t.id}</span>
                  </div>
                </div>
              </div>

              <p className="text-sm text-muted-foreground mt-3">{t.description}</p>
              <div className="mt-4 pt-3 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                <span>Créée le :</span>
                <span className="font-mono font-medium text-muted-foreground">{t.dateCreation}</span>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-border flex items-center justify-end space-x-2">
              <button
                type="button"
                onClick={() => {
                  setEditingTontine(t);
                  setNom(t.nom);
                  setDescription(t.description || '');
                  setFormError(null);
                }}
                className="p-2 rounded-lg border border-border text-muted-foreground hover:bg-muted text-xs flex items-center"
              >
                <Edit2 className="w-3.5 h-3.5 mr-1" />
                Modifier
              </button>
              <button
                type="button"
                onClick={() => handleDelete(t.id!)}
                className="p-2 rounded-lg border border-destructive text-destructive hover:bg-destructive/10 text-xs flex items-center"
              >
                <Trash2 className="w-3.5 h-3.5 mr-1" />
                Supprimer
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* CREATE MODAL (POST /api/tontines avec admin, D-35) */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <div>
                <span className="text-xs font-mono text-primary font-bold">POST /api/tontines (D-35)</span>
                <h3 className="font-heading font-bold text-lg text-foreground">
                  Créer une nouvelle Tontine
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

            {formError !== null && <ErrorState error={formError} />}

            <form onSubmit={handleCreate} className="space-y-4">
              <div className="p-3 bg-muted border border-border rounded-xl">
                <span className="text-xs font-bold text-muted-foreground uppercase font-heading block mb-2">
                  1. Paramètres de l'association
                </span>
                <div className="space-y-3">
                  <div>
                    <label className="block text-xs font-medium text-muted-foreground mb-1">Nom de la tontine</label>
                    <input
                      type="text"
                      required
                      placeholder="Ex: Tontine Dushirehamwe"
                      value={nom}
                      onChange={(e) => setNom(e.target.value)}
                      className="w-full py-2 px-3 rounded-lg border border-border text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-muted-foreground mb-1">Description</label>
                    <input
                      type="text"
                      placeholder="Ex: Tontine solidaire des commerçants de Bujumbura"
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      className="w-full py-2 px-3 rounded-lg border border-border text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-muted-foreground mb-1">Date de création</label>
                    <input
                      type="date"
                      required
                      value={dateCreation}
                      onChange={(e) => setDateCreation(e.target.value)}
                      className="w-full py-2 px-3 rounded-lg border border-border text-sm font-mono"
                    />
                  </div>
                </div>
              </div>

              <div className="p-3 bg-primary/10 border border-primary rounded-xl">
                <span className="text-xs font-bold text-primary-foreground uppercase font-heading block mb-2">
                  2. Premier Administrateur de la tontine (D-35)
                </span>
                <div className="space-y-3">
                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-xs font-medium text-muted-foreground mb-1">Prénom</label>
                      <input
                        type="text"
                        required
                        value={adminPrenom}
                        onChange={(e) => setAdminPrenom(e.target.value)}
                        className="w-full py-2 px-3 rounded-lg border border-border text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-medium text-muted-foreground mb-1">Nom</label>
                      <input
                        type="text"
                        required
                        value={adminNom}
                        onChange={(e) => setAdminNom(e.target.value)}
                        className="w-full py-2 px-3 rounded-lg border border-border text-sm"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-medium text-muted-foreground mb-1">Email administrateur</label>
                    <input
                      type="email"
                      required
                      value={adminEmail}
                      onChange={(e) => setAdminEmail(e.target.value)}
                      className="w-full py-2 px-3 rounded-lg border border-border text-sm"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-xs font-medium text-muted-foreground mb-1">Mot de passe</label>
                      <input
                        type="password"
                        required
                        value={adminMotDePasse}
                        onChange={(e) => setAdminMotDePasse(e.target.value)}
                        className="w-full py-2 px-3 rounded-lg border border-border text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-medium text-muted-foreground mb-1">Téléphone</label>
                      <input
                        type="tel"
                        required
                        value={adminTelephone}
                        onChange={(e) => setAdminTelephone(e.target.value)}
                        className="w-full py-2 px-3 rounded-lg border border-border text-sm font-mono"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <div className="pt-3 flex items-center justify-end space-x-2">
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
                  {createMutation.isPending ? 'Création...' : 'Créer Tontine (POST)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {editingTontine && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Modifier Tontine #{editingTontine.id}
              </h3>
              <button
                type="button"
                onClick={() => setEditingTontine(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError !== null && <ErrorState error={formError} />}

            <form onSubmit={handleUpdate} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Nom
                </label>
                <input
                  type="text"
                  required
                  value={nom}
                  onChange={(e) => setNom(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Description
                </label>
                <textarea
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                />
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setEditingTontine(null)}
                  className="touch-target px-4 py-2 rounded-xl border border-border text-xs font-medium hover:bg-muted"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={updateMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {updateMutation.isPending ? 'Envoi...' : 'Mettre à jour (PUT)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TontinesPage;
