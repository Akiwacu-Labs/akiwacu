import { useMemo, useState } from "react";
import { Download, FileCheck2, Receipt } from "lucide-react";
import { apiClient } from "../api/client";
import { useCotisationsQuery, useMembresQuery, usePretsQuery } from "../api/queries";
import { Montant } from "../components/Montant";
import { EmptyState } from "../components/ui-states/EmptyState";
import { ErrorState } from "../components/ui-states/ErrorState";
import { LoadingSkeleton } from "../components/ui-states/LoadingSkeleton";

interface ReceiptItem { id: number; label: string; date?: string; montant: number; membreId?: number; }

export default function RecusPage() {
  const cotisations = useCotisationsQuery();
  const prets = usePretsQuery();
  const membres = useMembresQuery();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [downloadError, setDownloadError] = useState<unknown>(null);

  const receipts = useMemo<ReceiptItem[]>(() => [
    ...(cotisations.data ?? []).filter((item) => item.recuId).map((item) => ({ id: item.recuId ?? 0, label: `Cotisation #${item.id}`, date: item.dateCotisation, montant: item.montant ?? 0, membreId: item.membreId })),
    ...(prets.data ?? []).filter((item) => item.recuId).map((item) => ({ id: item.recuId ?? 0, label: `Prêt #${item.id}`, date: item.dateDeblocage, montant: item.montantAccorde ?? 0, membreId: item.membreId })),
  ], [cotisations.data, prets.data]);

  const selected = receipts.find((item) => item.id === selectedId) ?? receipts[0];
  const loading = cotisations.isLoading || prets.isLoading || membres.isLoading;
  const error = cotisations.error ?? prets.error ?? membres.error;

  const download = async (id: number) => {
    setDownloadError(null);
    try {
      const blob = await apiClient.getRecuPdf(id);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = `recu-${id}.pdf`;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (downloadFailure) {
      setDownloadError(downloadFailure);
    }
  };

  return <div className="mx-auto max-w-6xl space-y-6">
    <header><p className="text-xs font-semibold uppercase tracking-wider text-primary">R8 · Reçus</p><h1 className="mt-2 font-display text-2xl font-semibold">Reçus officiels</h1><p className="mt-1 text-sm text-muted-foreground">Téléchargez le PDF d’un reçu réellement associé à une opération.</p></header>
    {loading && <LoadingSkeleton type="table" />}
    {!loading && error && <ErrorState error={error} onRetry={() => { void cotisations.refetch(); void prets.refetch(); void membres.refetch(); }} />}
    {!loading && !error && receipts.length === 0 && <EmptyState title="Aucun reçu disponible" description="Les reçus apparaîtront ici lorsqu’une opération validée en fournira l’identifiant." icon={Receipt} />}
    {!loading && !error && receipts.length > 0 && <div className="grid gap-6 lg:grid-cols-[minmax(0,0.8fr)_minmax(0,1.2fr)]">
      <section className="rounded-xl border border-border bg-card p-4"><h2 className="mb-3 font-display font-semibold">Opérations avec reçu</h2><div className="space-y-2">{receipts.map((item) => { const member = membres.data?.find((candidate) => candidate.id === item.membreId); return <button type="button" key={`${item.label}-${item.id}`} onClick={() => setSelectedId(item.id)} className={`flex min-h-12 w-full items-center justify-between rounded-lg border p-3 text-left ${selected?.id === item.id ? "border-primary bg-primary/10" : "border-border hover:bg-muted"}`}><span><span className="block text-sm font-medium">{item.label}</span><span className="block text-xs text-muted-foreground">{member ? `${member.prenom} ${member.nom}` : "Membre non détaillé"} · {item.date ?? "Date non fournie"}</span></span><Montant valeur={item.montant} /></button>; })}</div></section>
      <section className="rounded-xl border border-border bg-card p-6">{selected ? <><div className="flex items-start justify-between gap-4"><div><FileCheck2 className="mb-3 text-primary" /><h2 className="font-display text-xl font-semibold">{selected.label}</h2><p className="mt-1 text-sm text-muted-foreground">Identifiant du reçu : {selected.id}</p></div><Receipt className="text-muted-foreground" /></div><div className="my-6 rounded-lg bg-muted p-4"><p className="text-sm text-muted-foreground">Montant associé</p><Montant valeur={selected.montant} className="mt-1 text-xl" /></div>{downloadError !== null && <ErrorState error={downloadError} onRetry={() => void download(selected.id)} />}<button type="button" onClick={() => void download(selected.id)} className="touch-target inline-flex w-full items-center justify-center gap-2 rounded-lg bg-secondary px-4 font-display font-semibold text-secondary-foreground hover:bg-secondary/90"><Download size={18} /> Télécharger le PDF</button></> : <p className="text-sm text-muted-foreground">Sélectionnez un reçu.</p>}</section>
    </div>}
  </div>;
}
