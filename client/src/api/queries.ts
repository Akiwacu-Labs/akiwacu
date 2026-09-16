import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient, Schemas } from './client';

export const QUERY_KEYS = {
  dashboard: ['dashboard'] as const,
  tontines: ['tontines'] as const,
  tontine: (id: number) => ['tontines', id] as const,
  utilisateurs: ['utilisateurs'] as const,
  utilisateur: (id: number) => ['utilisateurs', id] as const,
  membres: ['membres'] as const,
  membre: (id: number) => ['membres', id] as const,
  adhesions: ['adhesions'] as const,
  adhesion: (id: number) => ['adhesions', id] as const,
  cycles: ['cycles'] as const,
  cycle: (id: number) => ['cycles', id] as const,
  cotisations: ['cotisations'] as const,
  cotisation: (id: number) => ['cotisations', id] as const,
  demandesPret: (statut?: string) => ['demandes-pret', statut || 'all'] as const,
  demandePret: (id: number) => ['demandes-pret', id] as const,
  votes: (demandeId: number) => ['demandes-pret', demandeId, 'votes'] as const,
  vote: (demandeId: number, voteId: number) => ['demandes-pret', demandeId, 'votes', voteId] as const,
  decision: (demandeId: number) => ['demandes-pret', demandeId, 'decision'] as const,
  prets: ['prets'] as const,
  pret: (pretId: number) => ['prets', pretId] as const,
  echeancier: (pretId: number) => ['prets', pretId, 'echeancier'] as const,
  remboursements: ['remboursements'] as const,
  remboursement: (id: number) => ['remboursements', id] as const,
  remboursementsParPret: (pretId: number) => ['remboursements', 'pret', pretId] as const,
  transactionsCaisse: ['transactions-caisse'] as const,
  transactionCaisse: (id: number) => ['transactions-caisse', id] as const,
  soldeCaisse: ['transactions-caisse', 'solde'] as const,
  transactionsParCycle: (cycleId: number) => ['transactions-caisse', 'cycle', cycleId] as const,
};

/* 1. Dashboard */
export const useDashboardQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.dashboard,
    queryFn: () => apiClient.getDashboard(),
  });
};

/* 2. Tontines */
export const useTontinesQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.tontines,
    queryFn: () => apiClient.getTontines(),
  });
};

export const useTontineQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.tontine(id),
    queryFn: () => apiClient.getTontine(id),
    enabled: !!id,
  });
};

export const useCreateTontineMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['TontineCreationRequest']) => apiClient.createTontine(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.tontines });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.utilisateurs });
    },
  });
};

export const useUpdateTontineMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['TontineRequest'] }) =>
      apiClient.updateTontine(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.tontines });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.tontine(vars.id) });
    },
  });
};

export const useDeleteTontineMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiClient.deleteTontine(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.tontines });
    },
  });
};

/* 3. Utilisateurs */
export const useUtilisateursQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.utilisateurs,
    queryFn: () => apiClient.getUtilisateurs(),
  });
};

export const useUtilisateurQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.utilisateur(id),
    queryFn: () => apiClient.getUtilisateur(id),
    enabled: !!id,
  });
};

export const useCreateUtilisateurMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['UtilisateurRequest']) => apiClient.createUtilisateur(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.utilisateurs });
    },
  });
};

export const useUpdateUtilisateurMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['UtilisateurModificationRequest'] }) =>
      apiClient.updateUtilisateur(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.utilisateurs });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.utilisateur(vars.id) });
    },
  });
};

export const useDeleteUtilisateurMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiClient.deleteUtilisateur(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.utilisateurs });
    },
  });
};

/* 4. Membres */
export const useMembresQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.membres,
    queryFn: () => apiClient.getMembres(),
  });
};

export const useMembreQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.membre(id),
    queryFn: () => apiClient.getMembre(id),
    enabled: !!id,
  });
};

export const useCreateMembreMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['MembreRequest']) => apiClient.createMembre(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.membres });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const useUpdateMembreMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['MembreModificationRequest'] }) =>
      apiClient.updateMembre(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.membres });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.membre(vars.id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

/* 5. Adhesions */
export const useAdhesionsQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.adhesions,
    queryFn: () => apiClient.getAdhesions(),
  });
};

export const useAdhesionQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.adhesion(id),
    queryFn: () => apiClient.getAdhesion(id),
    enabled: !!id,
  });
};

export const useCreateAdhesionMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['AdhesionRequest']) => apiClient.createAdhesion(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.adhesions });
    },
  });
};

export const useUpdateAdhesionMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['AdhesionModificationRequest'] }) =>
      apiClient.updateAdhesion(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.adhesions });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.adhesion(vars.id) });
    },
  });
};

/* 6. Cycles */
export const useCyclesQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.cycles,
    queryFn: () => apiClient.getCycles(),
  });
};

export const useCycleQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.cycle(id),
    queryFn: () => apiClient.getCycle(id),
    enabled: !!id,
  });
};

export const useCreateCycleMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['CycleRequest']) => apiClient.createCycle(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cycles });
    },
  });
};

export const useUpdateCycleMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['CycleRequest'] }) =>
      apiClient.updateCycle(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cycles });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cycle(vars.id) });
    },
  });
};

export const usePatchCycleStatutMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['CycleStatutRequest'] }) =>
      apiClient.patchCycleStatut(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cycles });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cycle(vars.id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

/* 7. Cotisations */
export const useCotisationsQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.cotisations,
    queryFn: () => apiClient.getCotisations(),
  });
};

export const useCotisationQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.cotisation(id),
    queryFn: () => apiClient.getCotisation(id),
    enabled: !!id,
  });
};

export const useCreateCotisationMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['CotisationRequest']) => apiClient.createCotisation(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cotisations });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionsCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const useCreateCotisationBatchMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['CotisationBatchRequest']) => apiClient.createCotisationBatch(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cotisations });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionsCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const useUpdateCotisationMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['CotisationModificationRequest'] }) =>
      apiClient.updateCotisation(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cotisations });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.cotisation(vars.id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

/* 8. Demandes de prêt & Votes */
export const useDemandesPretQuery = (statut?: 'SOUMISE' | 'APPROUVEE' | 'REJETEE' | 'DEBLOQUEE') => {
  return useQuery({
    queryKey: QUERY_KEYS.demandesPret(statut),
    queryFn: () => apiClient.getDemandesPret(statut),
  });
};

export const useDemandePretQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.demandePret(id),
    queryFn: () => apiClient.getDemandePret(id),
    enabled: !!id,
  });
};

export const useCreateDemandePretMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['DemandePretRequest']) => apiClient.createDemandePret(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['demandes-pret'] });
    },
  });
};

export const useVotesQuery = (demandePretId: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.votes(demandePretId),
    queryFn: () => apiClient.getVotes(demandePretId),
    enabled: !!demandePretId,
  });
};

export const useVoteQuery = (demandePretId: number, voteId: number) => {
  return useQuery({
    queryKey: ['vote', demandePretId, voteId],
    queryFn: () => apiClient.getVote(demandePretId, voteId),
    enabled: Boolean(demandePretId && voteId),
  });
};

export const useCreateVoteMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ demandePretId, data }: { demandePretId: number; data: Schemas['VoteRequest'] }) =>
      apiClient.createVote(demandePretId, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.votes(vars.demandePretId) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.decision(vars.demandePretId) });
      qc.invalidateQueries({ queryKey: ['demandes-pret'] });
    },
  });
};

export const useVoteDecisionQuery = (demandePretId: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.decision(demandePretId),
    queryFn: () => apiClient.getDecision(demandePretId),
    enabled: !!demandePretId,
  });
};

/* 9. Prêts */
export const usePretsQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.prets,
    queryFn: () => apiClient.getPrets(),
  });
};

export const usePretQuery = (pretId: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.pret(pretId),
    queryFn: () => apiClient.getPret(pretId),
    enabled: !!pretId,
  });
};

export const useDebloquerPretMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['PretDisbursementRequest']) => apiClient.debloquerPret(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.prets });
      qc.invalidateQueries({ queryKey: ['demandes-pret'] });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionsCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const usePretEcheancierQuery = (pretId: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.echeancier(pretId),
    queryFn: () => apiClient.getPretEcheancier(pretId),
    enabled: !!pretId,
  });
};

/* 10. Remboursements */
export const useRemboursementsQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.remboursements,
    queryFn: () => apiClient.getRemboursements(),
  });
};

export const useRemboursementQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.remboursement(id),
    queryFn: () => apiClient.getRemboursement(id),
    enabled: !!id,
  });
};

export const useRemboursementsParPretQuery = (pretId: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.remboursementsParPret(pretId),
    queryFn: () => apiClient.getRemboursementsParPret(pretId),
    enabled: !!pretId,
  });
};

export const useCreateRemboursementMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['RemboursementRequest']) => apiClient.createRemboursement(data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.remboursements });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.remboursementsParPret(vars.pretId) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.echeancier(vars.pretId) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.prets });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionsCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const useUpdateRemboursementMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['RemboursementRequest'] }) =>
      apiClient.updateRemboursement(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.remboursements });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.remboursement(vars.id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.echeancier(vars.data.pretId) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const useDeleteRemboursementMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiClient.deleteRemboursement(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.remboursements });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

/* 11. Transactions Caisse */
export const useTransactionsCaisseQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.transactionsCaisse,
    queryFn: () => apiClient.getTransactionsCaisse(),
  });
};

export const useTransactionCaisseQuery = (id: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.transactionCaisse(id),
    queryFn: () => apiClient.getTransactionCaisse(id),
    enabled: !!id,
  });
};

export const useSoldeCaisseQuery = () => {
  return useQuery({
    queryKey: QUERY_KEYS.soldeCaisse,
    queryFn: () => apiClient.getSoldeCaisse(),
  });
};

export const useTransactionsParCycleQuery = (cycleId: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.transactionsParCycle(cycleId),
    queryFn: () => apiClient.getTransactionsParCycle(cycleId),
    enabled: !!cycleId,
  });
};

export const useCreateTransactionCaisseMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Schemas['TransactionCaisseRequest']) => apiClient.createTransactionCaisse(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionsCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const useUpdateTransactionCaisseMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Schemas['TransactionCaisseRequest'] }) =>
      apiClient.updateTransactionCaisse(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionsCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionCaisse(vars.id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};

export const useDeleteTransactionCaisseMutation = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiClient.deleteTransactionCaisse(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: QUERY_KEYS.transactionsCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.soldeCaisse });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.dashboard });
    },
  });
};
