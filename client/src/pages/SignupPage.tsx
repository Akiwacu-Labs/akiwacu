import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Building2, KeyRound, Mail, UserPlus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { apiClient } from '../api/client';
import { ApiError } from '../api/types';

const signupSchema = z.object({
  tontine: z.string().min(2, 'Le nom de la tontine est obligatoire'),
  description: z.string().optional(),
  email: z.string().email('Adresse email valide requise'),
  motDePasse: z.string().min(8, 'Le mot de passe doit contenir au moins 8 caractères'),
  nom: z.string().min(1, 'Le nom est obligatoire'),
  prenom: z.string().min(1, 'Le prénom est obligatoire'),
  telephone: z.string().optional(),
});

type SignupFormData = z.infer<typeof signupSchema>;

export default function SignupPage() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [created, setCreated] = useState(false);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<SignupFormData>({
    resolver: zodResolver(signupSchema),
  });

  const onSubmit = async (data: SignupFormData) => {
    setError(null);
    try {
      await apiClient.createTontine({
        nom: data.tontine,
        description: data.description || undefined,
        administrateur: {
          email: data.email,
          motDePasse: data.motDePasse,
          nom: data.nom,
          prenom: data.prenom,
          telephone: data.telephone || undefined,
        },
      });
      setCreated(true);
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Impossible de créer la tontine.');
    }
  };

  if (created) {
    return (
      <main className="min-h-dvh bg-background px-4 py-12">
        <section className="mx-auto max-w-md rounded-2xl border border-border bg-card p-8 text-center shadow-sm">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/20 text-primary-foreground"><UserPlus /></div>
          <h1 className="mt-5 font-heading text-2xl font-bold text-foreground">Tontine créée</h1>
          <p className="mt-2 text-sm text-muted-foreground">Votre compte ADMIN est prêt. Connectez-vous pour commencer la configuration.</p>
          <button type="button" onClick={() => navigate('/login')} className="touch-target mt-6 w-full rounded-xl bg-secondary px-4 py-3 font-semibold text-secondary-foreground">Se connecter</button>
        </section>
      </main>
    );
  }

  return (
    <main className="min-h-dvh bg-background px-4 py-10 sm:px-6">
      <section className="mx-auto max-w-2xl">
        <div className="mb-8 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-secondary text-2xl font-bold text-primary">A</div>
          <h1 className="mt-4 font-heading text-3xl font-bold text-foreground">Créer une tontine</h1>
          <p className="mt-2 text-sm text-muted-foreground">Ce parcours public crée la tontine et son premier compte ADMIN.</p>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 rounded-2xl border border-border bg-card p-6 shadow-sm sm:p-8">
          {error && <div role="alert" className="rounded-xl border border-destructive bg-destructive/10 p-4 text-sm text-destructive">{error}</div>}
          <fieldset className="space-y-4">
            <legend className="flex items-center gap-2 font-heading font-semibold text-foreground"><Building2 size={18} /> Tontine</legend>
            <Field label="Nom de la tontine" error={errors.tontine?.message}><input {...register('tontine')} className="field" /></Field>
            <Field label="Description (facultatif)" error={errors.description?.message}><textarea {...register('description')} className="field min-h-24" /></Field>
          </fieldset>
          <fieldset className="space-y-4 border-t border-border pt-6">
            <legend className="flex items-center gap-2 font-heading font-semibold text-foreground"><KeyRound size={18} /> Premier administrateur</legend>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Prénom" error={errors.prenom?.message}><input {...register('prenom')} className="field" /></Field>
              <Field label="Nom" error={errors.nom?.message}><input {...register('nom')} className="field" /></Field>
            </div>
            <Field label="Email" error={errors.email?.message}><div className="relative"><Mail className="pointer-events-none absolute left-3 top-3 text-muted-foreground" size={17} /><input type="email" {...register('email')} className="field pl-10" /></div></Field>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Mot de passe" error={errors.motDePasse?.message}><input type="password" {...register('motDePasse')} className="field" /></Field>
              <Field label="Téléphone (facultatif)" error={errors.telephone?.message}><input {...register('telephone')} className="field" /></Field>
            </div>
          </fieldset>
          <button type="submit" disabled={isSubmitting} className="touch-target w-full rounded-xl bg-secondary px-4 py-3 font-semibold text-secondary-foreground disabled:opacity-50">{isSubmitting ? 'Création…' : 'Créer la tontine et le compte ADMIN'}</button>
          <p className="text-center text-sm text-muted-foreground">Déjà inscrit ? <Link to="/login" className="font-semibold text-primary-foreground underline">Se connecter</Link></p>
        </form>
      </section>
    </main>
  );
}

function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return <label className="block text-sm font-medium text-foreground">{label}{children}{error && <span className="mt-1 block text-xs text-destructive">{error}</span>}</label>;
}
