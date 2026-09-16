import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Role } from '../../api/types';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface RoleGuardProps {
  roles: Role[];
  children: React.ReactNode;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ roles, children }) => {
  const { hasRole, user } = useAuth();
  const navigate = useNavigate();

  if (!hasRole(roles)) {
    return (
      <div className="max-w-2xl mx-auto my-12 p-8 bg-card rounded-2xl border border-border shadow-xs text-center">
        <div className="w-16 h-16 rounded-full bg-primary/10 border border-primary text-primary-foreground flex items-center justify-center mx-auto mb-4">
          <ShieldAlert className="w-8 h-8" />
        </div>
        <span className="text-xs font-mono font-semibold px-2 py-0.5 rounded bg-muted text-muted-foreground">
          HTTP 403 Forbidden
        </span>
        <h2 className="text-xl font-bold text-foreground font-heading mt-2">
          Vous n’avez pas les droits pour cette action.
        </h2>
        <p className="text-sm text-muted-foreground mt-2 max-w-md mx-auto">
          Cette opération requiert l'un des rôles suivants :{' '}
          <span className="font-semibold text-muted-foreground font-mono">[{roles.join(', ')}]</span>.
          Votre compte actuel ({user?.email}) possède :{' '}
          <span className="font-semibold text-primary font-mono">[{user?.roles?.join(', ')}]</span>.
        </p>
        <div className="mt-6 flex items-center justify-center gap-3">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="touch-target inline-flex items-center px-4 py-2 border border-border text-sm font-medium rounded-xl text-muted-foreground bg-card hover:bg-muted"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Retour
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

export default RoleGuard;
