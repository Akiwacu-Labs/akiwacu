import React from 'react';
import { AlertTriangle, RefreshCw, ShieldAlert, FileQuestion, Lock } from 'lucide-react';
import { ApiError } from '../../api/types';

interface ErrorStateProps {
  error: unknown;
  onRetry?: () => void;
  title?: string;
}

export const ErrorState: React.FC<ErrorStateProps> = ({ error, onRetry, title }) => {
  let status = 500;
  let message = 'Une erreur inattendue est survenue.';
  let isConflict = false;
  let isForbidden = false;
  let isNotFound = false;

  if (error instanceof ApiError) {
    status = error.status;
    message = error.message;
    if (status === 409) isConflict = true;
    if (status === 403) isForbidden = true;
    if (status === 404) isNotFound = true;
  } else if (error instanceof Error) {
    message = error.message;
  }

  // Icons and titles based on status
  let Icon = AlertTriangle;
  let defaultTitle = 'Erreur technique';
  let badgeColor = 'bg-destructive/10 text-destructive border-destructive';

  if (isForbidden) {
    Icon = ShieldAlert;
    defaultTitle = 'Accès non autorisé';
    badgeColor = 'bg-primary/10 text-primary-foreground border-primary';
  } else if (isNotFound) {
    Icon = FileQuestion;
    defaultTitle = 'Ressource introuvable';
    badgeColor = 'bg-muted text-muted-foreground border-border';
  } else if (isConflict) {
    Icon = Lock;
    defaultTitle = 'Règle métier non respectée';
    badgeColor = 'bg-orange-50 text-orange-800 border-orange-200';
  }

  return (
    <div className="p-6 bg-card rounded-xl border border-border shadow-xs my-4">
      <div className="flex items-start space-x-4">
        <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 border ${badgeColor}`}>
          <Icon className="w-5 h-5" />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center space-x-2">
            <h4 className="text-base font-semibold text-foreground font-heading">
              {title || defaultTitle}
            </h4>
            <span className="text-xs px-2 py-0.5 rounded-sm bg-muted text-muted-foreground font-mono">
              HTTP {status}
            </span>
          </div>
          <p className="mt-1 text-sm text-muted-foreground leading-relaxed">{message}</p>

          {isConflict && (
            <div className="mt-3 p-3 bg-primary/10 border border-primary rounded-lg text-xs text-primary-foreground leading-relaxed">
              <span className="font-semibold">Note de conformité :</span> Cette opération a été rejetée par les règles d'intégrité de la tontine (R1 à R8). Aucune écriture concurrente n'a été altérée.
            </div>
          )}

          {onRetry && !isForbidden && !isConflict && (
            <div className="mt-4">
              <button
                onClick={onRetry}
                type="button"
                className="touch-target inline-flex items-center px-4 py-2 border border-border text-sm font-medium rounded-lg text-muted-foreground bg-card hover:bg-muted transition-colors shadow-xs"
              >
                <RefreshCw className="w-4 h-4 mr-2" />
                Réessayer
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
