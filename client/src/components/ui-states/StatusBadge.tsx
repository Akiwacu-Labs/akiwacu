import React from 'react';

interface StatusBadgeProps {
  status: string | undefined;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  if (!status) return null;

  let colorClasses = 'bg-muted text-muted-foreground border-border';
  let label = status;

  switch (status) {
    case 'OUVERT':
    case 'ACTIVE':
    case 'ACTIF':
      colorClasses = 'bg-primary/10 text-foreground border-primary';
      label = status === 'OUVERT' ? 'Ouvert' : status === 'ACTIVE' ? 'Active' : 'Actif';
      break;
    case 'GELE':
      colorClasses = 'bg-primary/10 text-primary-foreground border-primary';
      label = 'Gelé';
      break;
    case 'CLOTURE':
    case 'CLOTUREE':
    case 'SOLDE':
      colorClasses = 'bg-muted text-muted-foreground border-border';
      label = status === 'SOLDE' ? 'Soldé' : 'Clôturé';
      break;
    case 'SUSPENDU':
    case 'SUSPENDUE':
      colorClasses = 'bg-primary/10 text-primary-foreground border-primary';
      label = 'Suspendu';
      break;
    case 'SORTI':
      colorClasses = 'bg-destructive/10 text-destructive border-destructive';
      label = 'Sorti';
      break;
    case 'SOUMISE':
      colorClasses = 'bg-muted text-foreground border-border';
      label = 'Soumise (En attente de votes)';
      break;
    case 'APPROUVEE':
      colorClasses = 'bg-primary/10 text-foreground border-primary';
      label = 'Approuvée (Quorum atteint)';
      break;
    case 'REJETEE':
      colorClasses = 'bg-destructive/10 text-destructive border-destructive';
      label = 'Rejetée';
      break;
    case 'DEBLOQUEE':
      colorClasses = 'bg-muted text-foreground border-border';
      label = 'Débloquée (Prêt actif)';
      break;
    case 'EN_RETARD':
      colorClasses = 'bg-destructive/10 text-destructive border-destructive font-semibold';
      label = 'En retard d’échéance';
      break;
    case 'POUR':
      colorClasses = 'bg-primary/10 text-foreground border-primary';
      label = 'Pour';
      break;
    case 'CONTRE':
      colorClasses = 'bg-destructive/10 text-destructive border-destructive';
      label = 'Contre';
      break;
  }

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border font-heading ${colorClasses}`}
    >
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-70"></span>
      {label}
    </span>
  );
};

export default StatusBadge;
