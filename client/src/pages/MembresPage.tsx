import React, { useState } from 'react';
import { UserPlus, Search, Edit2 } from 'lucide-react';
import {
  useMembresQuery,
  useMembreQuery,
  useCreateMembreMutation,
  useUpdateMembreMutation,
  useUtilisateursQuery,
} from '../api/queries';
import { StatusBadge } from '../components/ui-states/StatusBadge';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { EmptyState } from '../components/ui-states/EmptyState';
import { ErrorState } from '../components/ui-states/ErrorState';
import type { Schemas } from '../api/client';

export const MembresPage: React.FC = () => {
  const { data: membres = [], isLoading, isError, error, refetch } = useMembresQuery();
  const { data: utilisateurs = [] } = useUtilisateursQuery();

  const createMembreMutation = useCreateMembreMutation();
  const updateMembreMutation = useUpdateMembreMutation();

  const [search, setSearch] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingMembre, setEditingMembre] = useState<Schemas['MembreResponse'] | null>(null);
  const [inspectMembreId, setInspectMembreId] = useState<number | null>(null);

  const { data: inspectedMembre, isLoading: loadingInspected } = useMembreQuery(inspectMembreId || 0);

  // Creation form state
  const [nom, setNom] = useState('');
  const [prenom, setPrenom] = useState('');
  const [telephone, setTelephone] = useState('');
  const [numeroMembre, setNumeroMembre] = useState('');
  const [dateAdhesion, setDateAdhesion] = useState(new Date().toISOString().split('T')[0]);
  const [utilisateurId, setUtilisateurId] = useState<string>('');
  const [formError, setFormError] = useState<unknown | null>(null);

  // Edit form state
  const [editNom, setEditNom] = useState('');
  const [editPrenom, setEditPrenom] = useState('');
  const [editTelephone, setEditTelephone] = useState('');
  const [editStatut, setEditStatut] = useState<'ACTIF' | 'SUSPENDU' | 'SORTI'>('ACTIF');

  const filteredMembres = membres.filter((m) => {
    const q = search.toLowerCase();
    return (
      m.nom.toLowerCase().includes(q) ||
      m.prenom.toLowerCase().includes(q) ||
      m.telephone.includes(q) ||
      m.numeroMembre?.toLowerCase().includes(q)
    );
  });

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    try {
      await createMembreMutation.mutateAsync({
        nom,
        prenom,
        telephone,
        numeroMembre: numeroMembre || undefined,
        dateAdhesion,
        utilisateurId: utilisateurId ? Number(utilisateurId) : undefined,
      });
      setShowCreateModal(false);
      setNom('');
      setPrenom('');
      setTelephone('');
      setNumeroMembre('');
    } catch (err) {
      setFormError(err);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingMembre) return;
    setFormError(null);
    try {
      await updateMembreMutation.mutateAsync({
        id: editingMembre.id,
        data: {
          nom: editNom,
          prenom: editPrenom,
          telephone: editTelephone,
          statut: editStatut,
        },
      });
      setEditingMembre(null);
    } catch (err) {
      setFormError(err);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-card rounded-2xl border border-border p-6 shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs uppercase font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-muted text-foreground">
              Registre des membres
            </span>
            <span className="text-xs text-muted-foreground">·</span>
            <span className="text-xs text-muted-foreground font-medium">GET /api/membres</span>
          </div>
          <h1 className="text-2xl font-extrabold text-foreground font-heading mt-2">
            Membres de la tontine
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Gestion du registre officiel des adhérents et association avec les comptes utilisateurs.
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
          <UserPlus className="w-4 h-4 mr-2" />
          <span>Nouveau Membre</span>
        </button>
      </div>

      <div className="bg-card rounded-2xl border border-border p-4 shadow-xs">
        <div className="relative">
          <Search className="w-4 h-4 absolute left-3.5 top-3.5 text-muted-foreground" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Rechercher par nom, numéro ou téléphone..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-border text-sm focus:ring-2 focus:ring-ring focus:outline-hidden"
          />
        </div>
      </div>

      {isLoading && <LoadingSkeleton rows={5} type="table" />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} />}

      {!isLoading && filteredMembres.length === 0 && (
        <EmptyState
          title="Aucun membre trouvé"
          description="Enregistrez un nouveau membre pour alimenter le registre de la tontine."
          actionLabel="Ajouter un membre"
          onAction={() => setShowCreateModal(true)}
        />
      )}

      {!isLoading && filteredMembres.length > 0 && (
        <div className="bg-card rounded-2xl border border-border overflow-hidden shadow-xs">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-stone-200 text-sm">
              <thead className="bg-muted text-muted-foreground font-heading text-xs font-semibold uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-4 text-left">N° Membre</th>
                  <th className="py-3.5 px-4 text-left">Nom & Prénom</th>
                  <th className="py-3.5 px-4 text-left">Téléphone</th>
                  <th className="py-3.5 px-4 text-left">Date Adhésion</th>
                  <th className="py-3.5 px-4 text-center">Statut</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-stone-100 font-sans">
                {filteredMembres.map((m) => (
                  <tr key={m.id} className="hover:bg-muted/70 transition-colors">
                    <td className="py-3.5 px-4 font-mono text-xs text-muted-foreground font-bold">
                      {m.numeroMembre || `MEM-${m.id}`}
                    </td>
                    <td className="py-3.5 px-4 font-bold text-foreground font-heading">
                      {m.prenom} {m.nom}
                    </td>
                    <td className="py-3.5 px-4 text-muted-foreground font-mono text-xs">
                      {m.telephone}
                    </td>
                    <td className="py-3.5 px-4 text-muted-foreground text-xs font-mono">
                      {m.dateAdhesion}
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <StatusBadge status={m.statut} />
                    </td>
                    <td className="py-3.5 px-4 text-right space-x-2">
                      <button
                        type="button"
                        onClick={() => setInspectMembreId(m.id!)}
                        className="inline-flex items-center px-2.5 py-1.5 text-xs font-medium rounded-[6px] text-muted-foreground bg-muted hover:bg-muted transition-colors"
                      >
                        Consulter
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setEditingMembre(m);
                          setEditNom(m.nom);
                          setEditPrenom(m.prenom);
                          setEditTelephone(m.telephone);
                          setEditStatut(m.statut as 'ACTIF' | 'SUSPENDU' | 'SORTI');
                          setFormError(null);
                        }}
                        className="inline-flex items-center px-2.5 py-1.5 text-xs font-medium rounded-[6px] text-muted-foreground bg-muted hover:bg-muted transition-colors"
                      >
                        <Edit2 className="w-3.5 h-3.5 mr-1 text-primary" />
                        Modifier
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* CREATE MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Nouveau membre (POST /api/membres)
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
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Prénom
                  </label>
                  <input
                    type="text"
                    required
                    value={prenom}
                    onChange={(e) => setPrenom(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                  />
                </div>
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
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Téléphone (Burundi)
                </label>
                <input
                  type="tel"
                  required
                  placeholder="+257 79 000 000"
                  value={telephone}
                  onChange={(e) => setTelephone(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    N° Membre
                  </label>
                  <input
                    type="text"
                    placeholder="MEM-2026-00X"
                    value={numeroMembre}
                    onChange={(e) => setNumeroMembre(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Date Adhésion
                  </label>
                  <input
                    type="date"
                    required
                    value={dateAdhesion}
                    onChange={(e) => setDateAdhesion(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Lier à un compte utilisateur (optionnel)
                </label>
                <select
                  value={utilisateurId}
                  onChange={(e) => setUtilisateurId(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                >
                  <option value="">Aucun compte lié</option>
                  {utilisateurs.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.prenom} {u.nom} ({u.email})
                    </option>
                  ))}
                </select>
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
                  disabled={createMembreMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {createMembreMutation.isPending ? 'Enregistrement...' : 'Enregistrer (POST)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL (PUT /api/membres/{id}) */}
      {editingMembre && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Modifier membre #{editingMembre.id}
              </h3>
              <button
                type="button"
                onClick={() => setEditingMembre(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError !== null && <ErrorState error={formError} />}

            <form onSubmit={handleUpdate} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Prénom
                  </label>
                  <input
                    type="text"
                    required
                    value={editPrenom}
                    onChange={(e) => setEditPrenom(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                    Nom
                  </label>
                  <input
                    type="text"
                    required
                    value={editNom}
                    onChange={(e) => setEditNom(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Téléphone
                </label>
                <input
                  type="tel"
                  required
                  value={editTelephone}
                  onChange={(e) => setEditTelephone(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Statut du membre
                </label>
                <select
                  value={editStatut}
                  onChange={(e) => setEditStatut(e.target.value as 'ACTIF' | 'SUSPENDU' | 'SORTI')}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-medium"
                >
                  <option value="ACTIF">ACTIF</option>
                  <option value="SUSPENDU">SUSPENDU</option>
                  <option value="SORTI">SORTI</option>
                </select>
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setEditingMembre(null)}
                  className="touch-target px-4 py-2 rounded-xl border border-border text-xs font-medium hover:bg-muted"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={updateMembreMutation.isPending}
                  className="touch-target px-5 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {updateMembreMutation.isPending ? 'Mise à jour...' : 'Mettre à jour (PUT)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* INSPECT MODAL (GET /api/membres/{id}) */}
      {inspectMembreId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-[14px] max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <div>
                <span className="text-xs font-mono text-primary font-bold">GET /api/membres/{inspectMembreId}</span>
                <h3 className="font-heading font-bold text-lg text-foreground">
                  Dossier membre #{inspectMembreId}
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setInspectMembreId(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {loadingInspected && <LoadingSkeleton rows={3} />}

            {inspectedMembre && (
              <div className="space-y-4">
                <div className="p-4 bg-muted rounded-[10px] border border-border space-y-2.5 text-xs">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Nom complet :</span>
                    <span className="font-bold text-foreground">{inspectedMembre.prenom} {inspectedMembre.nom}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Numéro membre :</span>
                    <span className="font-mono font-semibold text-muted-foreground">{inspectedMembre.numeroMembre || 'Non attribué'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Téléphone :</span>
                    <span className="font-mono text-muted-foreground">{inspectedMembre.telephone}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Date d'adhésion :</span>
                    <span className="font-mono text-muted-foreground">{inspectedMembre.dateAdhesion}</span>
                  </div>
                  <div className="flex justify-between items-center pt-2 border-t border-border">
                    <span className="text-muted-foreground">Statut :</span>
                    <StatusBadge status={inspectedMembre.statut} />
                  </div>
                </div>

                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={() => setInspectMembreId(null)}
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

export default MembresPage;
