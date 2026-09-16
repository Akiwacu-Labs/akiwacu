import type { components } from "./generated/schema";
import { ApiError, type DecodedToken } from "./types";

type RawSchemas = components["schemas"];
export type Schemas = RawSchemas & {
  UtilisateurResponse: Required<RawSchemas["UtilisateurResponse"]>;
  TransactionCaisseResponse: Required<RawSchemas["TransactionCaisseResponse"]>;
  TontineResponse: Required<RawSchemas["TontineResponse"]>;
  RemboursementResponse: Required<RawSchemas["RemboursementResponse"]>;
  MembreResponse: Required<RawSchemas["MembreResponse"]>;
  CycleResponse: Required<RawSchemas["CycleResponse"]>;
  CotisationResponse: Required<RawSchemas["CotisationResponse"]>;
  AdhesionResponse: Required<RawSchemas["AdhesionResponse"]>;
  PretResponse: Required<RawSchemas["PretResponse"]>;
  DemandePretResponse: Required<RawSchemas["DemandePretResponse"]>;
  VoteResponse: Required<RawSchemas["VoteResponse"]>;
  PretScheduleResponse: Required<RawSchemas["PretScheduleResponse"]>;
  VoteDecisionResponse: Required<RawSchemas["VoteDecisionResponse"]>;
  DashboardResponse: Required<RawSchemas["DashboardResponse"]>;
};

const JWT_KEY = "akiwacu_jwt";

export function getStoredToken(): string | null {
  return localStorage.getItem(JWT_KEY);
}

export function setStoredToken(token: string): void {
  localStorage.setItem(JWT_KEY, token);
}

export function removeStoredToken(): void {
  localStorage.removeItem(JWT_KEY);
}

export function decodeToken(token: string): DecodedToken | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))) as DecodedToken;
    return decoded;
  } catch {
    return null;
  }
}

function errorMessage(status: number): string {
  switch (status) {
    case 400:
      return "Les données envoyées sont invalides.";
    case 401:
      return "Session expirée. Veuillez vous reconnecter.";
    case 403:
      return "Vous n’avez pas les droits pour cette action.";
    case 404:
      return "Ressource introuvable.";
    case 409:
      return "Cette opération est refusée par une règle métier.";
    default:
      return `Une erreur est survenue (HTTP ${status}).`;
  }
}

class ApiClient {
  private async request<T>(path: string, init: RequestInit = {}): Promise<T> {
    const token = getStoredToken();
    const headers = new Headers(init.headers);
    headers.set("Accept", "application/json");
    if (init.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
    if (token) headers.set("Authorization", `Bearer ${token}`);

    let response: Response;
    try {
      response = await fetch(path, { ...init, headers });
    } catch {
      throw new ApiError(0, "Le serveur est momentanément inaccessible. Réessayez.");
    }

    if (!response.ok) {
      let message = errorMessage(response.status);
      try {
        const payload = (await response.json()) as { message?: string; error?: string };
        message = payload.message ?? payload.error ?? message;
      } catch {
        // La réponse peut être vide, notamment pour certaines erreurs HTTP.
      }
      if (response.status === 401) {
        removeStoredToken();
        window.dispatchEvent(new CustomEvent("akiwacu-unauthorized"));
      }
      throw new ApiError(response.status, message);
    }

    if (response.status === 204) return undefined as T;
    if (response.headers.get("content-type")?.includes("application/pdf")) {
      return (await response.blob()) as T;
    }
    return (await response.json()) as T;
  }

  async login(body: Schemas["LoginRequest"]): Promise<Schemas["LoginResponse"]> {
    return this.request("/api/auth/login", { method: "POST", body: JSON.stringify(body) });
  }

  getDashboard() { return this.request<Schemas["DashboardResponse"]>("/api/dashboard"); }

  getTontines() { return this.request<Schemas["TontineResponse"][]>("/api/tontines"); }
  createTontine(body: Schemas["TontineCreationRequest"]) { return this.request<Schemas["TontineResponse"]>("/api/tontines", { method: "POST", body: JSON.stringify(body) }); }
  getTontine(id: number) { return this.request<Schemas["TontineResponse"]>(`/api/tontines/${id}`); }
  updateTontine(id: number, body: Schemas["TontineRequest"]) { return this.request<Schemas["TontineResponse"]>(`/api/tontines/${id}`, { method: "PUT", body: JSON.stringify(body) }); }
  deleteTontine(id: number) { return this.request<void>(`/api/tontines/${id}`, { method: "DELETE" }); }

  getUtilisateurs() { return this.request<Schemas["UtilisateurResponse"][]>("/api/utilisateurs"); }
  createUtilisateur(body: Schemas["UtilisateurRequest"]) { return this.request<Schemas["UtilisateurResponse"]>("/api/utilisateurs", { method: "POST", body: JSON.stringify(body) }); }
  getUtilisateur(id: number) { return this.request<Schemas["UtilisateurResponse"]>(`/api/utilisateurs/${id}`); }
  updateUtilisateur(id: number, body: Schemas["UtilisateurModificationRequest"]) { return this.request<Schemas["UtilisateurResponse"]>(`/api/utilisateurs/${id}`, { method: "PUT", body: JSON.stringify(body) }); }
  deleteUtilisateur(id: number) { return this.request<void>(`/api/utilisateurs/${id}`, { method: "DELETE" }); }

  getMembres() { return this.request<Schemas["MembreResponse"][]>("/api/membres"); }
  createMembre(body: Schemas["MembreRequest"]) { return this.request<Schemas["MembreResponse"]>("/api/membres", { method: "POST", body: JSON.stringify(body) }); }
  getMembre(id: number) { return this.request<Schemas["MembreResponse"]>(`/api/membres/${id}`); }
  updateMembre(id: number, body: Schemas["MembreModificationRequest"]) { return this.request<Schemas["MembreResponse"]>(`/api/membres/${id}`, { method: "PUT", body: JSON.stringify(body) }); }

  getAdhesions() { return this.request<Schemas["AdhesionResponse"][]>("/api/adhesions"); }
  createAdhesion(body: Schemas["AdhesionRequest"]) { return this.request<Schemas["AdhesionResponse"]>("/api/adhesions", { method: "POST", body: JSON.stringify(body) }); }
  getAdhesion(id: number) { return this.request<Schemas["AdhesionResponse"]>(`/api/adhesions/${id}`); }
  updateAdhesion(id: number, body: Schemas["AdhesionModificationRequest"]) { return this.request<Schemas["AdhesionResponse"]>(`/api/adhesions/${id}`, { method: "PUT", body: JSON.stringify(body) }); }

  getCycles() { return this.request<Schemas["CycleResponse"][]>("/api/cycles"); }
  createCycle(body: Schemas["CycleRequest"]) { return this.request<Schemas["CycleResponse"]>("/api/cycles", { method: "POST", body: JSON.stringify(body) }); }
  getCycle(id: number) { return this.request<Schemas["CycleResponse"]>(`/api/cycles/${id}`); }
  updateCycle(id: number, body: Schemas["CycleRequest"]) { return this.request<Schemas["CycleResponse"]>(`/api/cycles/${id}`, { method: "PUT", body: JSON.stringify(body) }); }
  patchCycleStatut(id: number, body: Schemas["CycleStatutRequest"]) { return this.request<Schemas["CycleResponse"]>(`/api/cycles/${id}/statut`, { method: "PATCH", body: JSON.stringify(body) }); }

  getCotisations() { return this.request<Schemas["CotisationResponse"][]>("/api/cotisations"); }
  createCotisation(body: Schemas["CotisationRequest"]) { return this.request<Schemas["CotisationResponse"]>("/api/cotisations", { method: "POST", body: JSON.stringify(body) }); }
  createCotisationBatch(body: Schemas["CotisationBatchRequest"]) { return this.request<Schemas["CotisationResponse"][]>("/api/cotisations/batch", { method: "POST", body: JSON.stringify(body) }); }
  getCotisation(id: number) { return this.request<Schemas["CotisationResponse"]>(`/api/cotisations/${id}`); }
  updateCotisation(id: number, body: Schemas["CotisationModificationRequest"]) { return this.request<Schemas["CotisationResponse"]>(`/api/cotisations/${id}`, { method: "PUT", body: JSON.stringify(body) }); }

  getRecuPdf(id: number) { return this.request<Blob>(`/api/recus/${id}/pdf`); }

  getDemandesPret(statut?: Schemas["DemandePretResponse"]["statut"]) {
    const query = statut ? `?statut=${encodeURIComponent(statut)}` : "";
    return this.request<Schemas["DemandePretResponse"][]>(`/api/demandes-pret${query}`);
  }
  createDemandePret(body: Schemas["DemandePretRequest"]) { return this.request<Schemas["DemandePretResponse"]>("/api/demandes-pret", { method: "POST", body: JSON.stringify(body) }); }
  getDemandePret(id: number) { return this.request<Schemas["DemandePretResponse"]>(`/api/demandes-pret/${id}`); }
  getVotes(id: number) { return this.request<Schemas["VoteResponse"][]>(`/api/demandes-pret/${id}/votes`); }
  createVote(id: number, body: Schemas["VoteRequest"]) { return this.request<Schemas["VoteResponse"]>(`/api/demandes-pret/${id}/votes`, { method: "POST", body: JSON.stringify(body) }); }
  getVote(demandeId: number, voteId: number) { return this.request<Schemas["VoteResponse"]>(`/api/demandes-pret/${demandeId}/votes/${voteId}`); }
  getDecision(id: number) { return this.request<Schemas["VoteDecisionResponse"]>(`/api/demandes-pret/${id}/votes/decision`); }

  getPrets() { return this.request<Schemas["PretResponse"][]>("/api/prets"); }
  debloquerPret(body: Schemas["PretDisbursementRequest"]) { return this.request<Schemas["PretResponse"]>("/api/prets", { method: "POST", body: JSON.stringify(body) }); }
  getPret(id: number) { return this.request<Schemas["PretResponse"]>(`/api/prets/${id}`); }
  getPretEcheancier(id: number) { return this.request<Schemas["PretScheduleResponse"]>(`/api/prets/${id}/echeancier`); }

  getRemboursements() { return this.request<Schemas["RemboursementResponse"][]>("/api/remboursements"); }
  createRemboursement(body: Schemas["RemboursementRequest"]) { return this.request<Schemas["RemboursementResponse"]>("/api/remboursements", { method: "POST", body: JSON.stringify(body) }); }
  getRemboursement(id: number) { return this.request<Schemas["RemboursementResponse"]>(`/api/remboursements/${id}`); }
  updateRemboursement(id: number, body: Schemas["RemboursementRequest"]) { return this.request<Schemas["RemboursementResponse"]>(`/api/remboursements/${id}`, { method: "PUT", body: JSON.stringify(body) }); }
  deleteRemboursement(id: number) { return this.request<void>(`/api/remboursements/${id}`, { method: "DELETE" }); }
  getRemboursementsParPret(id: number) { return this.request<Schemas["RemboursementResponse"][]>(`/api/remboursements/pret/${id}`); }

  getTransactionsCaisse() { return this.request<Schemas["TransactionCaisseResponse"][]>("/api/transactions-caisse"); }
  createTransactionCaisse(body: Schemas["TransactionCaisseRequest"]) { return this.request<Schemas["TransactionCaisseResponse"]>("/api/transactions-caisse", { method: "POST", body: JSON.stringify(body) }); }
  getTransactionCaisse(id: number) { return this.request<Schemas["TransactionCaisseResponse"]>(`/api/transactions-caisse/${id}`); }
  updateTransactionCaisse(id: number, body: Schemas["TransactionCaisseRequest"]) { return this.request<Schemas["TransactionCaisseResponse"]>(`/api/transactions-caisse/${id}`, { method: "PUT", body: JSON.stringify(body) }); }
  deleteTransactionCaisse(id: number) { return this.request<void>(`/api/transactions-caisse/${id}`, { method: "DELETE" }); }
  getSoldeCaisse() { return this.request<number>("/api/transactions-caisse/solde"); }
  getTransactionsParCycle(id: number) { return this.request<Schemas["TransactionCaisseResponse"][]>(`/api/transactions-caisse/cycle/${id}`); }
}

export const apiClient = new ApiClient();
