export type Role = 'ADMIN' | 'GESTIONNAIRE' | 'TRESORIER' | 'COMMISSAIRE' | 'MEMBRE';

export interface DecodedToken {
  sub: string; // email
  email?: string;
  utilisateurId?: number;
  tontineId?: number;
  roles: Role[];
  nom?: string;
  prenom?: string;
  exp?: number;
}

export class ApiError extends Error {
  status: number;
  data?: unknown;
  constructor(status: number, message: string, data?: unknown) {
    super(message);
    this.status = status;
    this.data = data;
    this.name = 'ApiError';
  }
}
