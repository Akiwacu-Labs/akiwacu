import React, { useState } from 'react';
import { UserPlus, Trash2, Edit2 } from 'lucide-react';
import {
  useUtilisateursQuery,
  useCreateUtilisateurMutation,
  useUpdateUtilisateurMutation,
  useDeleteUtilisateurMutation,
} from '../api/queries';
import { StatusBadge } from '../components/ui-states/StatusBadge';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { ErrorState } from '../components/ui-states/ErrorState';
import { Role } from '../api/types';
import type { Schemas } from '../api/client';

const ALL_ROLES: Role[] = ['ADMIN', 'GESTIONNAIRE', 'TRESORIER', 'COMMISSAIRE', 'MEMBRE'];

export const UtilisateursPage: React.FC = () => {
  const { data: utilisateurs = [], isLoading, isError, error, refetch } = useUtilisateursQuery();
  const createMutation = useCreateUtilisateurMutation();
  const updateMutation = useUpdateUtilisateurMutation();
  const deleteMutation = useDeleteUtilisateurMutation();

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingUser, setEditingUser] = useState<Schemas['UtilisateurResponse'] | null>(null);

  // Form states
  const [nom, setNom] = useState('');
  const [prenom, setPrenom] = useState('');
  const [email, setEmail] = useState('');
  const [motDePasse, setMotDePasse] = useState('Password123!');
  const [telephone, setTelephone] = useState('+257 79 000 000');
  const [selectedRoles, setSelectedRoles] = useState<Role[]>(['MEMBRE']);
  const [actif, setActif] = useState<boolean>(true);
  const [formError, setFormError] = useState<unknown | null>(null);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    try {
      await createMutation.mutateAsync({
        nom,
        prenom,
        email,
        motDePasse,
        telephone,
        roles: selectedRoles,
      });
      setShowCreateModal(false);
      setNom('');
      setPrenom('');
      setEmail('');
    } catch (err) {
      setFormError(err);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser) return;
    setFormError(null);
    try {
      await updateMutation.mutateAsync({
        id: editingUser.id,
        data: {
          nom,
          prenom,
          telephone,
          roles: selectedRoles,
          actif,
        },
      });
      setEditingUser(null);
    } catch (err) {
      setFormError(err);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Désactiver logiquement ce compte utilisateur (DELETE) ?')) return;
    try {
      await deleteMutation.mutateAsync(id);
    } catch {
      alert('Erreur lors de la désactivation.');
    }
  };

  const toggleRole = (role: Role) => {
    if (selectedRoles.includes(role)) {
      if (selectedRoles.length === 1) return; // at least 1 role
      setSelectedRoles(selectedRoles.filter((r) => r !== role));
    } else {
      setSelectedRoles([...selectedRoles, role]);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-card rounded-2xl border border-border p-6 shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs uppercase font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-primary/10 text-primary-foreground">
              Contrôle d'accès RBAC
            </span>
            <span className="text-xs text-muted-foreground">·</span>
            <span className="text-xs text-muted-foreground font-medium">GET /api/utilisateurs</span>
          </div>
          <h1 className="text-2xl font-extrabold text-foreground font-heading mt-2">
            Utilisateurs & Rôles
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Gestion des identités, des habilitations et des accès à la tontine.
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            setShowCreateModal(true);
            setNom('');
            setPrenom('');
            setEmail('');
            setSelectedRoles(['MEMBRE']);
            setFormError(null);
          }}
          className="touch-target inline-flex items-center px-4 py-2.5 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground font-heading font-semibold text-sm shadow-xs transition-colors shrink-0"
        >
          <UserPlus className="w-4 h-4 mr-2" />
          <span>Nouvel Utilisateur</span>
        </button>
      </div>

      {isLoading && <LoadingSkeleton rows={5} type="table" />}
      {isError && <ErrorState error={error} onRetry={() => refetch()} />}

      {!isLoading && (
        <div className="bg-card rounded-2xl border border-border overflow-hidden shadow-xs">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-stone-200 text-sm">
              <thead className="bg-muted text-muted-foreground font-heading text-xs font-semibold uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-4 text-left">Utilisateur</th>
                  <th className="py-3.5 px-4 text-left">Email</th>
                  <th className="py-3.5 px-4 text-left">Rôles attribués</th>
                  <th className="py-3.5 px-4 text-center">Statut</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-stone-100 font-sans">
                {utilisateurs.map((u) => (
                  <tr key={u.id} className="hover:bg-muted/70 transition-colors">
                    <td className="py-3.5 px-4">
                      <p className="font-bold text-foreground font-heading">
                        {u.prenom} {u.nom}
                      </p>
                      <span className="text-xs text-muted-foreground font-mono">{u.telephone}</span>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-xs text-muted-foreground">
                      {u.email}
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="flex flex-wrap gap-1">
                        {u.roles?.map((r) => (
                          <span
                            key={r}
                            className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-muted text-muted-foreground border border-border"
                          >
                            {r}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <StatusBadge status={u.actif ? 'ACTIF' : 'INACTIF'} />
                    </td>
                    <td className="py-3.5 px-4 text-right space-x-2">
                      <button
                        type="button"
                        onClick={() => {
                          setEditingUser(u);
                          setNom(u.nom || '');
                          setPrenom(u.prenom || '');
                          setTelephone(u.telephone || '');
                          setSelectedRoles(u.roles || ['MEMBRE']);
                          setActif(u.actif ?? true);
                          setFormError(null);
                        }}
                        className="p-1.5 rounded-lg border border-border text-muted-foreground hover:bg-muted"
                        title="Modifier (PUT /api/utilisateurs/{id})"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(u.id!)}
                        className="p-1.5 rounded-lg border border-destructive text-destructive hover:bg-destructive/10"
                        title="Désactiver (DELETE /api/utilisateurs/{id})"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
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
                Créer un utilisateur (POST)
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
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Prénom</label>
                  <input
                    type="text"
                    required
                    value={prenom}
                    onChange={(e) => setPrenom(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Nom</label>
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
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Email</label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Mot de passe</label>
                  <input
                    type="password"
                    required
                    value={motDePasse}
                    onChange={(e) => setMotDePasse(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Téléphone</label>
                  <input
                    type="tel"
                    required
                    value={telephone}
                    onChange={(e) => setTelephone(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-2">
                  Rôles à attribuer
                </label>
                <div className="flex flex-wrap gap-2">
                  {ALL_ROLES.map((r) => {
                    const active = selectedRoles.includes(r);
                    return (
                      <button
                        key={r}
                        type="button"
                        onClick={() => toggleRole(r)}
                        className={`touch-target px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition-colors ${
                          active
                            ? 'bg-secondary text-primary shadow-xs'
                            : 'bg-muted text-muted-foreground hover:bg-muted'
                        }`}
                      >
                        {r}
                      </button>
                    );
                  })}
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
                  {createMutation.isPending ? 'Création...' : 'Créer Utilisateur (POST)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {editingUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Modifier utilisateur #{editingUser.id}
              </h3>
              <button
                type="button"
                onClick={() => setEditingUser(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError !== null && <ErrorState error={formError} />}

            <form onSubmit={handleUpdate} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Prénom</label>
                  <input
                    type="text"
                    required
                    value={prenom}
                    onChange={(e) => setPrenom(e.target.value)}
                    className="w-full py-2 px-3 rounded-xl border border-border text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Nom</label>
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
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">Téléphone</label>
                <input
                  type="tel"
                  required
                  value={telephone}
                  onChange={(e) => setTelephone(e.target.value)}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">État du compte</label>
                <select
                  value={actif ? 'ACTIF' : 'INACTIF'}
                  onChange={(e) => setActif(e.target.value === 'ACTIF')}
                  className="w-full py-2 px-3 rounded-xl border border-border text-sm font-medium"
                >
                  <option value="ACTIF">ACTIF (Accès autorisé)</option>
                  <option value="INACTIF">INACTIF (Accès bloqué)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-2">Rôles</label>
                <div className="flex flex-wrap gap-2">
                  {ALL_ROLES.map((r) => {
                    const active = selectedRoles.includes(r);
                    return (
                      <button
                        key={r}
                        type="button"
                        onClick={() => toggleRole(r)}
                        className={`touch-target px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition-colors ${
                          active
                            ? 'bg-secondary text-primary shadow-xs'
                            : 'bg-muted text-muted-foreground hover:bg-muted'
                        }`}
                      >
                        {r}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setEditingUser(null)}
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
    </div>
  );
};

export default UtilisateursPage;
