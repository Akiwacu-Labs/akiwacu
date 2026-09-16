import React, { useState, useMemo } from 'react';
import { Coins, Search, CheckCircle, Download, Lock, Layers, Sparkles, ArrowRight, Receipt } from 'lucide-react';
import {
  useCotisationsQuery,
  useCotisationQuery,
  useCreateCotisationMutation,
  useCreateCotisationBatchMutation,
  useUpdateCotisationMutation,
  useMembresQuery,
  useCyclesQuery,
} from '../api/queries';
import { Montant } from '../components/Montant';
import { LoadingSkeleton } from '../components/ui-states/LoadingSkeleton';
import { EmptyState } from '../components/ui-states/EmptyState';
import { ErrorState } from '../components/ui-states/ErrorState';
import { apiClient } from '../api/client';
import type { Schemas } from '../api/client';

export const CotisationsPage: React.FC = () => {
  const { data: cotisations = [], isLoading, isError, error, refetch } = useCotisationsQuery();
  const { data: membres = [] } = useMembresQuery();
  const { data: cycles = [] } = useCyclesQuery();

  const createCotisationMutation = useCreateCotisationMutation();
  const createBatchMutation = useCreateCotisationBatchMutation();
  const updateCotisationMutation = useUpdateCotisationMutation();

  // Active cycle
  const activeCycle = useMemo(() => cycles.find((c) => c.statut === 'OUVERT') || cycles[0], [cycles]);
  const standardAmount = activeCycle?.montantCotisation || 50000;

  // Mode selection: Compteur (Rapide 1 à 1) vs Batch (Lot) vs Historique
  const [activeTab, setActiveTab] = useState<'compteur' | 'batch' | 'liste'>('compteur');

  // Compteur State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedMembreId, setSelectedMembreId] = useState<number | null>(null);
  const [montantSaisi, setMontantSaisi] = useState<number>(standardAmount);
  const [modePaiement, setModePaiement] = useState<'ESPECES' | 'MOBILE_MONEY' | 'VIREMENT'>('ESPECES');
  const [feedbackSuccess, setFeedbackSuccess] = useState<string | null>(null);
  const [mutationError, setMutationError] = useState<unknown | null>(null);

  // Batch Mode State
  const [batchSelectedMembres, setBatchSelectedMembres] = useState<number[]>([]);
  const [batchModePaiement] = useState<'ESPECES' | 'MOBILE_MONEY' | 'VIREMENT'>('ESPECES');

  // Modification modal state
  const [editingCotisation, setEditingCotisation] = useState<Schemas['CotisationResponse'] | null>(null);
  const [editMontant, setEditMontant] = useState<number>(0);
  const [editModePaiement, setEditModePaiement] = useState<'ESPECES' | 'MOBILE_MONEY' | 'VIREMENT'>('ESPECES');
  const [editError, setEditError] = useState<unknown | null>(null);
  const [inspectCotisationId, setInspectCotisationId] = useState<number | null>(null);

  const { data: inspectedCotisation, isLoading: loadingInspected } = useCotisationQuery(inspectCotisationId || 0);

  // Live session total (Le Compteur motif)
  const sessionTotal = useMemo(() => {
    return cotisations.reduce((sum, c) => sum + (c.montant || 0), 0);
  }, [cotisations]);

  // Filtered members for search
  const filteredMembres = useMemo(() => {
    if (!searchQuery) return membres;
    const q = searchQuery.toLowerCase();
    return membres.filter(
      (m) =>
        m.nom.toLowerCase().includes(q) ||
        m.prenom.toLowerCase().includes(q) ||
        m.numeroMembre?.toLowerCase().includes(q)
    );
  }, [membres, searchQuery]);

  const selectedMembre = useMemo(
    () => membres.find((m) => m.id === selectedMembreId) || filteredMembres[0],
    [membres, selectedMembreId, filteredMembres]
  );

  // Submit 1 cotisation (Le Compteur)
  const handleEnregistrerCotisation = async () => {
    if (!selectedMembre || !activeCycle) return;
    setMutationError(null);
    setFeedbackSuccess(null);

    try {
      await createCotisationMutation.mutateAsync({
        membreId: selectedMembre.id!,
        cycleId: activeCycle.id!,
        montant: Number(montantSaisi),
        dateCotisation: new Date().toISOString().split('T')[0],
        modePaiement,
      });

      setFeedbackSuccess(
        `Cotisation de ${montantSaisi} BIF enregistrée pour ${selectedMembre.prenom} ${selectedMembre.nom}. Reçu généré (R8).`
      );

      // Auto-advance to next member for ultra-fast meeting entry
      const currentIndex = filteredMembres.findIndex((m) => m.id === selectedMembre.id);
      if (currentIndex !== -1 && currentIndex + 1 < filteredMembres.length) {
        setSelectedMembreId(filteredMembres[currentIndex + 1].id!);
      }
    } catch (err: unknown) {
      setMutationError(err);
    }
  };

  // Submit batch cotisations
  const handleEnregistrerBatch = async () => {
    if (!activeCycle || batchSelectedMembres.length === 0) return;
    setMutationError(null);
    setFeedbackSuccess(null);

    try {
      const today = new Date().toISOString().split('T')[0];
      const payload = {
        cotisations: batchSelectedMembres.map((mId) => ({
          membreId: mId,
          cycleId: activeCycle.id!,
          montant: standardAmount,
          dateCotisation: today,
          modePaiement: batchModePaiement,
        })),
      };

      await createBatchMutation.mutateAsync(payload);
      setFeedbackSuccess(
        `Lot de ${batchSelectedMembres.length} cotisations enregistré avec succès en transaction atomique.`
      );
      setBatchSelectedMembres([]);
    } catch (err: unknown) {
      setMutationError(err);
    }
  };

  // Update existing cotisation
  const handleUpdateCotisation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingCotisation) return;
    setEditError(null);

    try {
      await updateCotisationMutation.mutateAsync({
        id: editingCotisation.id,
        data: {
          montant: Number(editMontant),
          dateCotisation: editingCotisation.dateCotisation,
          modePaiement: editModePaiement,
        },
      });
      setEditingCotisation(null);
    } catch (err: unknown) {
      setEditError(err);
    }
  };

  // Download PDF Receipt
  const handleDownloadPdf = async (recuId: number) => {
    try {
      const blob = await apiClient.getRecuPdf(recuId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `recu-REC-2026-${String(recuId).padStart(6, '0')}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch {
      alert('Impossible de générer le PDF du reçu.');
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Bandeau fixe « Le Compteur » - Total en direct qui grimpe en réunion */}
      <div className="bg-secondary text-primary-foreground rounded-2xl p-5 border border-secondary shadow-md flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-muted border border-border flex items-center justify-center text-primary shrink-0">
            <Coins className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs font-mono uppercase tracking-wider text-muted-foreground block">
              Direction C « Le Compteur » · Total collecté
            </span>
            <div className="text-3xl font-extrabold font-heading text-primary tracking-tight mt-0.5">
              <Montant valeur={sessionTotal} />
            </div>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          <div className="px-3 py-1.5 rounded-lg bg-muted/80 border border-border text-xs text-muted-foreground">
            <span className="text-muted-foreground">Cycle : </span>
            <span className="font-semibold text-primary-foreground">{activeCycle?.libelle || 'Cycle 2026'}</span>
          </div>
          <div className="px-3 py-1.5 rounded-lg bg-muted/80 border border-border text-xs text-muted-foreground">
            <span className="text-muted-foreground">Cotisations : </span>
            <span className="font-semibold text-primary font-heading">{cotisations.length} reçus</span>
          </div>
        </div>
      </div>

      {/* 2. Tabs Selector */}
      <div className="flex items-center space-x-2 border-b border-border pb-2">
        <button
          type="button"
          onClick={() => setActiveTab('compteur')}
          className={`touch-target px-4 py-2 rounded-xl text-sm font-heading font-semibold transition-colors flex items-center space-x-2 ${
            activeTab === 'compteur'
              ? 'bg-secondary text-primary-foreground shadow-xs'
              : 'bg-card text-muted-foreground hover:bg-muted border border-border'
          }`}
        >
          <Sparkles className="w-4 h-4 text-primary" />
          <span>Saisie rapide (« Le Compteur »)</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('batch')}
          className={`touch-target px-4 py-2 rounded-xl text-sm font-heading font-semibold transition-colors flex items-center space-x-2 ${
            activeTab === 'batch'
              ? 'bg-secondary text-primary-foreground shadow-xs'
              : 'bg-card text-muted-foreground hover:bg-muted border border-border'
          }`}
        >
          <Layers className="w-4 h-4 text-primary" />
          <span>Saisie par lot (Batch atomique)</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('liste')}
          className={`touch-target px-4 py-2 rounded-xl text-sm font-heading font-semibold transition-colors flex items-center space-x-2 ${
            activeTab === 'liste'
              ? 'bg-secondary text-primary-foreground shadow-xs'
              : 'bg-card text-muted-foreground hover:bg-muted border border-border'
          }`}
        >
          <Receipt className="w-4 h-4 text-primary" />
          <span>Historique & Reçus ({cotisations.length})</span>
        </button>
      </div>

      {/* Global feedback / error alert */}
      {feedbackSuccess && (
        <div className="p-4 bg-primary/10 border border-primary rounded-xl text-foreground text-sm flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <CheckCircle className="w-5 h-5 text-foreground shrink-0" />
            <span>{feedbackSuccess}</span>
          </div>
          <button
            type="button"
            onClick={() => setFeedbackSuccess(null)}
            className="text-xs text-foreground underline font-medium"
          >
            Fermer
          </button>
        </div>
      )}

      {mutationError !== null && (
        <ErrorState
          error={mutationError}
          onRetry={() => {
            setMutationError(null);
            handleEnregistrerCotisation();
          }}
        />
      )}

      {/* 3. TAB A : LE COMPTEUR — Saisie Ultra Rapide 1 Membre à la fois */}
      {activeTab === 'compteur' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Member selector column */}
          <div className="lg:col-span-5 bg-card rounded-2xl border border-border p-5 shadow-xs flex flex-col h-[520px]">
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-heading font-bold text-base text-foreground">
                Choisir un membre
              </h3>
              <span className="text-xs text-muted-foreground font-mono">
                {filteredMembres.length} membres
              </span>
            </div>

            {/* Quick search input */}
            <div className="relative mb-3">
              <Search className="w-4 h-4 absolute left-3.5 top-3.5 text-muted-foreground" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Rechercher par nom ou numéro..."
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-border text-sm focus:ring-2 focus:ring-ring focus:outline-hidden"
              />
            </div>

            {/* Scrollable member list */}
            <div className="flex-1 overflow-y-auto space-y-2 pr-1">
              {filteredMembres.map((m) => {
                const isSelected = selectedMembre?.id === m.id;
                return (
                  <button
                    key={m.id}
                    type="button"
                    onClick={() => setSelectedMembreId(m.id!)}
                    className={`w-full text-left p-3 rounded-xl border transition-all flex items-center justify-between touch-target ${
                      isSelected
                        ? 'border-primary bg-primary/10 shadow-xs'
                        : 'border-border hover:bg-muted'
                    }`}
                  >
                    <div>
                      <p className="text-sm font-bold text-foreground font-heading">
                        {m.prenom} {m.nom}
                      </p>
                      <p className="text-xs text-muted-foreground font-mono mt-0.5">
                        {m.numeroMembre || 'Sans N°'} · {m.telephone}
                      </p>
                    </div>
                    {isSelected && (
                      <span className="w-2.5 h-2.5 rounded-full bg-primary"></span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Rapid Entry Input Area (The Counter Pad) */}
          <div className="lg:col-span-7 bg-card rounded-2xl border border-border p-6 shadow-xs flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-4 border-b border-border">
                <div>
                  <span className="text-xs text-muted-foreground font-mono">Membre actif en réunion :</span>
                  <h2 className="text-xl font-extrabold text-foreground font-heading">
                    {selectedMembre ? `${selectedMembre.prenom} ${selectedMembre.nom}` : 'Aucun membre sélectionné'}
                  </h2>
                </div>
                <div className="text-right">
                  <span className="text-xs text-muted-foreground font-mono">N° de membre</span>
                  <p className="text-sm font-bold font-mono text-muted-foreground">
                    {selectedMembre?.numeroMembre || 'MEM-2026-X'}
                  </p>
                </div>
              </div>

              {/* Amount Selection */}
              <div className="mt-6">
                <label className="block text-xs font-bold text-muted-foreground uppercase tracking-wider font-heading mb-2">
                  Montant à encaisser (BIF)
                </label>

                {/* Fast presets */}
                <div className="grid grid-cols-3 gap-2 mb-3">
                  <button
                    type="button"
                    onClick={() => setMontantSaisi(standardAmount)}
                    className={`touch-target py-2.5 px-3 rounded-xl border text-sm font-heading font-bold transition-all ${
                      montantSaisi === standardAmount
                        ? 'border-primary bg-primary/10 text-foreground ring-2 ring-ring'
                        : 'border-border hover:bg-muted text-muted-foreground'
                    }`}
                  >
                    <Montant valeur={standardAmount} />
                    <span className="block text-[10px] text-muted-foreground font-normal">Standard cycle</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setMontantSaisi(standardAmount * 2)}
                    className={`touch-target py-2.5 px-3 rounded-xl border text-sm font-heading font-bold transition-all ${
                      montantSaisi === standardAmount * 2
                        ? 'border-primary bg-primary/10 text-foreground ring-2 ring-ring'
                        : 'border-border hover:bg-muted text-muted-foreground'
                    }`}
                  >
                    <Montant valeur={standardAmount * 2} />
                    <span className="block text-[10px] text-muted-foreground font-normal">Double cotisation</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setMontantSaisi(100000)}
                    className={`touch-target py-2.5 px-3 rounded-xl border text-sm font-heading font-bold transition-all ${
                      montantSaisi === 100000
                        ? 'border-primary bg-primary/10 text-foreground ring-2 ring-ring'
                        : 'border-border hover:bg-muted text-muted-foreground'
                    }`}
                  >
                    <Montant valeur={100000} />
                    <span className="block text-[10px] text-muted-foreground font-normal">100 000 BIF</span>
                  </button>
                </div>

                {/* Free input field */}
                <div className="relative">
                  <input
                    type="number"
                    min="1000"
                    step="5000"
                    value={montantSaisi}
                    onChange={(e) => setMontantSaisi(Number(e.target.value))}
                    className="w-full text-2xl font-tabular font-bold py-3 px-4 rounded-xl border border-border text-foreground focus:ring-2 focus:ring-ring focus:outline-hidden"
                  />
                  <span className="absolute right-4 top-4 text-sm font-bold text-muted-foreground font-heading">
                    BIF
                  </span>
                </div>
              </div>

              {/* Payment Mode */}
              <div className="mt-6">
                <label className="block text-xs font-bold text-muted-foreground uppercase tracking-wider font-heading mb-2">
                  Mode d'encaissement (OpenAPI Enum)
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {(['ESPECES', 'MOBILE_MONEY', 'VIREMENT'] as const).map((mode) => (
                    <button
                      key={mode}
                      type="button"
                      onClick={() => setModePaiement(mode)}
                      className={`touch-target py-2 px-3 rounded-xl border text-xs font-heading font-semibold transition-all ${
                        modePaiement === mode
                          ? 'border-secondary bg-secondary text-primary-foreground shadow-xs'
                          : 'border-border hover:bg-muted text-muted-foreground'
                      }`}
                    >
                      {mode === 'ESPECES' ? 'Espèces' : mode === 'MOBILE_MONEY' ? 'Mobile Money' : 'Virement'}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Validation CTA Button */}
            <div className="mt-8 pt-4 border-t border-border">
              <button
                type="button"
                onClick={handleEnregistrerCotisation}
                disabled={createCotisationMutation.isPending || !selectedMembre}
                className="touch-target w-full flex items-center justify-center py-4 px-6 rounded-xl shadow-md text-base font-bold text-primary-foreground bg-primary hover:bg-primary/80 focus:outline-hidden disabled:opacity-50 transition-colors font-heading"
              >
                {createCotisationMutation.isPending ? (
                  <span>Génération du reçu (R8) & écriture caisse...</span>
                ) : (
                  <>
                    <span>Valider cotisation (<Montant valeur={montantSaisi} />)</span>
                    <ArrowRight className="w-5 h-5 ml-2" />
                  </>
                )}
              </button>
              <p className="text-center text-xs text-muted-foreground mt-2">
                Règle R5 : Validateur tracé · Règle R8 : Reçu PDF numéroté émis
              </p>
            </div>
          </div>
        </div>
      )}

      {/* 4. TAB B : SAISIE EN LOT (POST /api/cotisations/batch) */}
      {activeTab === 'batch' && (
        <div className="bg-card rounded-2xl border border-border p-6 shadow-xs space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 pb-4 border-b border-border">
            <div>
              <span className="text-xs uppercase font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-primary/10 text-primary-foreground">
                POST /api/cotisations/batch
              </span>
              <h3 className="text-lg font-bold text-foreground font-heading mt-1">
                Saisie par lot atomique
              </h3>
              <p className="text-xs text-muted-foreground">
                Cochez les membres ayant versé leur cotisation mensuelle de {standardAmount} BIF.
              </p>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setBatchSelectedMembres(membres.map((m) => m.id!))}
                className="touch-target px-3 py-1.5 rounded-lg border border-border text-xs font-medium hover:bg-muted"
              >
                Tout cocher
              </button>
              <button
                type="button"
                onClick={() => setBatchSelectedMembres([])}
                className="touch-target px-3 py-1.5 rounded-lg border border-border text-xs font-medium hover:bg-muted"
              >
                Tout décocher
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            {membres.map((m) => {
              const checked = batchSelectedMembres.includes(m.id!);
              return (
                <label
                  key={m.id}
                  className={`p-3.5 rounded-xl border flex items-center space-x-3 cursor-pointer transition-all ${
                    checked
                      ? 'border-primary bg-primary/10 shadow-xs'
                      : 'border-border hover:bg-muted'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setBatchSelectedMembres([...batchSelectedMembres, m.id!]);
                      } else {
                        setBatchSelectedMembres(batchSelectedMembres.filter((id) => id !== m.id));
                      }
                    }}
                    className="w-4 h-4 rounded text-primary focus:ring-ring"
                  />
                  <div>
                    <p className="text-sm font-semibold text-foreground font-heading">
                      {m.prenom} {m.nom}
                    </p>
                    <p className="text-xs text-muted-foreground font-mono">
                      {m.numeroMembre || 'Sans N°'}
                    </p>
                  </div>
                </label>
              );
            })}
          </div>

          {/* Batch CTA */}
          <div className="pt-4 border-t border-border flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <span className="text-xs text-muted-foreground">Total du lot à valider :</span>
              <p className="text-xl font-extrabold font-heading text-primary">
                <Montant valeur={batchSelectedMembres.length * standardAmount} /> ({batchSelectedMembres.length} membres)
              </p>
            </div>

            <button
              type="button"
              onClick={handleEnregistrerBatch}
              disabled={createBatchMutation.isPending || batchSelectedMembres.length === 0}
              className="touch-target px-6 py-3 rounded-xl bg-secondary hover:bg-muted text-primary-foreground font-heading font-semibold text-sm shadow-md disabled:opacity-50 transition-colors"
            >
              {createBatchMutation.isPending ? (
                <span>Enregistrement atomique en cours...</span>
              ) : (
                <span>Confirmer l'enregistrement du lot ({batchSelectedMembres.length})</span>
              )}
            </button>
          </div>
        </div>
      )}

      {/* 5. TAB C : LISTE ET HISTORIQUE (GET, PUT, PDF) */}
      {activeTab === 'liste' && (
        <div className="bg-card rounded-2xl border border-border overflow-hidden shadow-xs">
          <div className="p-5 border-b border-border flex items-center justify-between">
            <h3 className="font-heading font-bold text-base text-foreground">
              Historique des cotisations (GET /api/cotisations)
            </h3>
            <span className="text-xs text-muted-foreground font-mono">
              Protection R8 active sur reçus émis
            </span>
          </div>

          {isLoading && <LoadingSkeleton rows={5} type="table" />}
          {isError && <ErrorState error={error} onRetry={() => refetch()} />}

          {!isLoading && cotisations.length === 0 && (
            <EmptyState
              title="Aucune cotisation enregistrée"
              description="Utilisez le mode « Le Compteur » pour enregistrer la première cotisation de ce cycle."
              actionLabel="Passer au Compteur"
              onAction={() => setActiveTab('compteur')}
            />
          )}

          {!isLoading && cotisations.length > 0 && (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-stone-200 text-sm">
                <thead className="bg-muted text-muted-foreground font-heading text-xs font-semibold uppercase tracking-wider">
                  <tr>
                    <th className="py-3 px-4 text-left">Date</th>
                    <th className="py-3 px-4 text-left">Membre</th>
                    <th className="py-3 px-4 text-left">Mode</th>
                    <th className="py-3 px-4 text-right">Montant</th>
                    <th className="py-3 px-4 text-center">Reçu R8</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-stone-100 font-sans">
                  {cotisations.map((cot) => {
                    const membre = membres.find((m) => m.id === cot.membreId);
                    return (
                      <tr key={cot.id} className="hover:bg-muted/70 transition-colors">
                        <td className="py-3.5 px-4 font-mono text-xs text-muted-foreground">
                          {cot.dateCotisation}
                        </td>
                        <td className="py-3.5 px-4 font-medium text-foreground font-heading">
                          {membre ? `${membre.prenom} ${membre.nom}` : `Membre #${cot.membreId}`}
                        </td>
                        <td className="py-3.5 px-4 text-xs text-muted-foreground">
                          <span className="px-2 py-0.5 rounded bg-muted font-mono">
                            {cot.modePaiement}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right font-bold font-heading text-foreground">
                          <Montant valeur={cot.montant} />
                        </td>
                        <td className="py-3.5 px-4 text-center">
                          {cot.verrouille ? (
                            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-primary/10 text-foreground border border-primary">
                              <Lock className="w-3 h-3 mr-1" />
                              Verrouillé
                            </span>
                          ) : (
                            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-primary/10 text-primary-foreground border border-primary">
                              Non verrouillé
                            </span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 text-right space-x-2">
                          <button
                            type="button"
                            onClick={() => setInspectCotisationId(cot.id!)}
                            className="inline-flex items-center px-2.5 py-1 text-xs font-medium rounded-[6px] text-muted-foreground bg-muted hover:bg-muted transition-colors"
                          >
                            Consulter
                          </button>

                          {cot.recuId && (
                            <button
                              type="button"
                              onClick={() => handleDownloadPdf(cot.recuId!)}
                              className="inline-flex items-center px-2.5 py-1 text-xs font-medium rounded-[6px] text-muted-foreground bg-muted hover:bg-muted transition-colors"
                              title="Télécharger le reçu PDF (GET /api/recus/{id}/pdf)"
                            >
                              <Download className="w-3 h-3 mr-1 text-primary" />
                              PDF
                            </button>
                          )}

                          <button
                            type="button"
                            onClick={() => {
                              setEditingCotisation(cot);
                              setEditMontant(cot.montant || 0);
                              setEditModePaiement((cot.modePaiement as 'ESPECES' | 'MOBILE_MONEY' | 'VIREMENT') || 'ESPECES');
                              setEditError(null);
                            }}
                            className="inline-flex items-center px-2.5 py-1 text-xs font-medium rounded-[6px] text-muted-foreground bg-muted hover:bg-muted transition-colors"
                            title="Modifier (PUT /api/cotisations/{id})"
                          >
                            Modifier
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* 6. MODIFICATION MODAL (PUT /api/cotisations/{id}) */}
      {editingCotisation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-2xl max-w-md w-full p-6 shadow-xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <h3 className="font-heading font-bold text-lg text-foreground">
                Modifier cotisation #{editingCotisation.id}
              </h3>
              <button
                type="button"
                onClick={() => setEditingCotisation(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {editingCotisation.verrouille && (
              <div className="mb-4 p-3 bg-primary/10 border border-primary rounded-xl text-primary-foreground text-xs flex items-start space-x-2">
                <Lock className="w-4 h-4 shrink-0 text-primary-foreground mt-0.5" />
                <div>
                  <span className="font-bold">Avertissement Règle R8 :</span> Cette opération a déjà généré un reçu officiel. La tentative de modification renverra une erreur HTTP 409 explicative du serveur.
                </div>
              </div>
            )}

            {editError !== null && <ErrorState error={editError} />}

            <form onSubmit={handleUpdateCotisation} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Nouveau montant (BIF)
                </label>
                <input
                  type="number"
                  value={editMontant}
                  onChange={(e) => setEditMontant(Number(e.target.value))}
                  className="w-full py-2.5 px-3 rounded-xl border border-border font-tabular font-bold text-lg text-foreground"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-muted-foreground uppercase font-heading mb-1">
                  Mode de paiement
                </label>
                <select
                  value={editModePaiement}
                  onChange={(e) => setEditModePaiement(e.target.value as 'ESPECES' | 'MOBILE_MONEY' | 'VIREMENT')}
                  className="w-full py-2.5 px-3 rounded-xl border border-border text-sm font-medium"
                >
                  <option value="ESPECES">Espèces</option>
                  <option value="MOBILE_MONEY">Mobile Money</option>
                  <option value="VIREMENT">Virement</option>
                </select>
              </div>

              <div className="pt-4 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setEditingCotisation(null)}
                  className="touch-target px-4 py-2 rounded-xl border border-border text-xs font-medium hover:bg-muted"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={updateCotisationMutation.isPending}
                  className="touch-target px-4 py-2 rounded-xl bg-primary hover:bg-primary/80 text-primary-foreground text-xs font-bold font-heading shadow-xs"
                >
                  {updateCotisationMutation.isPending ? 'Envoi...' : 'Enregistrer (PUT)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* INSPECT MODAL (GET /api/cotisations/{id}) */}
      {inspectCotisationId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
          <div className="bg-card rounded-[14px] max-w-md w-full p-6 shadow-2xl border border-border animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-border mb-4">
              <div>
                <span className="text-xs font-mono text-primary font-bold">GET /api/cotisations/{inspectCotisationId}</span>
                <h3 className="font-heading font-bold text-lg text-foreground">
                  Détail cotisation #{inspectCotisationId}
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setInspectCotisationId(null)}
                className="text-muted-foreground hover:text-muted-foreground text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {loadingInspected && <LoadingSkeleton rows={3} />}

            {inspectedCotisation && (
              <div className="space-y-4">
                <div className="p-4 bg-muted rounded-[10px] border border-border space-y-2.5 text-xs">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Montant versé :</span>
                    <span className="font-bold font-heading text-foreground text-sm"><Montant valeur={inspectedCotisation.montant} /></span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Mode de paiement :</span>
                    <span className="font-semibold text-muted-foreground">{inspectedCotisation.modePaiement}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Date cotisation :</span>
                    <span className="font-mono text-muted-foreground">{inspectedCotisation.dateCotisation}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Membre ID :</span>
                    <span className="font-mono text-muted-foreground">#{inspectedCotisation.membreId}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Cycle ID :</span>
                    <span className="font-mono text-muted-foreground">#{inspectedCotisation.cycleId}</span>
                  </div>
                  <div className="flex justify-between items-center pt-2 border-t border-border">
                    <span className="text-muted-foreground">Statut R8 :</span>
                    <span className="font-mono font-bold text-muted-foreground">
                      {inspectedCotisation.verrouille ? 'VERROUILLÉ (Reçu émis)' : 'MODIFIABLE'}
                    </span>
                  </div>
                </div>

                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={() => setInspectCotisationId(null)}
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

export default CotisationsPage;
