interface PagePlaceholderProps {
  titre: string;
}

/** Provisoire — chaque domaine remplace la sienne par son propre écran (voir P5). */
export function PagePlaceholder({ titre }: PagePlaceholderProps) {
  return (
    <div className="flex min-h-[60dvh] flex-col items-center justify-center gap-2 px-6 text-center">
      <h1 className="font-display text-xl font-semibold">{titre}</h1>
      <p className="text-sm text-muted-foreground">Écran à venir.</p>
    </div>
  );
}
