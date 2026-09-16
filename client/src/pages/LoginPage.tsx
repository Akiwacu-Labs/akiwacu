import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { LogIn, ShieldAlert, User, KeyRound } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { ApiError } from '../api/types';

const loginSchema = z.object({
  email: z.string().email('Adresse email valide requise'),
  motDePasse: z.string().min(6, 'Le mot de passe doit contenir au moins 6 caractères'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [authError, setAuthError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/';

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', motDePasse: '' },
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      setIsSubmitting(true);
      setAuthError(null);
      await login(data.email, data.motDePasse);
      navigate(from, { replace: true });
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setAuthError(err.message);
      } else {
        setAuthError('Erreur de connexion. Vérifiez vos identifiants.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8 bg-background">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        {/* Logo Badge */}
        <div className="w-14 h-14 rounded-2xl bg-secondary text-primary font-heading font-bold text-2xl flex items-center justify-center mx-auto shadow-md border border-border">
          A
        </div>
        <h1 className="mt-4 text-3xl font-extrabold text-foreground font-heading tracking-tight">
          AKIWACU
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Système de gestion communautaire d’épargne et de crédit
        </p>
              <div className="mt-2 inline-flex items-center space-x-1.5 px-3 py-1 rounded-full bg-muted border border-border text-muted-foreground text-xs font-mono">
          <span>Direction C « Le Compteur »</span>
          <span className="text-muted-foreground">·</span>
          <span className="font-semibold text-primary">32 paths / 55 ops</span>
        </div>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-card py-8 px-6 sm:px-10 rounded-2xl border border-border shadow-sm">
          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            {authError && (
              <div className="p-4 rounded-xl bg-destructive/10 border border-destructive text-destructive text-xs flex items-start space-x-3">
                <ShieldAlert className="w-5 h-5 shrink-0 text-destructive mt-0.5" />
                <div>
                  <p className="font-semibold font-heading">Échec d'authentification</p>
                  <p className="mt-0.5 leading-relaxed">{authError}</p>
                </div>
              </div>
            )}

            <div>
              <label htmlFor="email" className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider font-heading mb-1.5">
                Adresse email
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-muted-foreground">
                  <User className="w-4 h-4" />
                </div>
                <input
                  id="email"
                  type="email"
                  autoComplete="email"
                  {...register('email')}
                  className="touch-target block w-full pl-10 pr-3 py-2.5 border border-border rounded-xl text-foreground placeholder-muted-foreground text-sm focus:outline-hidden focus:ring-2 focus:ring-ring focus:border-primary transition-colors"
                  placeholder="nom@tontine.bi"
                />
              </div>
              {errors.email && (
                <p className="mt-1.5 text-xs text-destructive">{errors.email.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="motDePasse" className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider font-heading mb-1.5">
                Mot de passe
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-muted-foreground">
                  <KeyRound className="w-4 h-4" />
                </div>
                <input
                  id="motDePasse"
                  type="password"
                  autoComplete="current-password"
                  {...register('motDePasse')}
                  className="touch-target block w-full pl-10 pr-3 py-2.5 border border-border rounded-xl text-foreground placeholder-muted-foreground text-sm focus:outline-hidden focus:ring-2 focus:ring-ring focus:border-primary transition-colors"
                  placeholder="••••••••"
                />
              </div>
              {errors.motDePasse && (
                <p className="mt-1.5 text-xs text-destructive">{errors.motDePasse.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="touch-target w-full flex items-center justify-center py-3 px-4 rounded-xl shadow-xs text-sm font-semibold text-primary-foreground bg-secondary hover:bg-muted focus:outline-hidden focus:ring-2 focus:ring-offset-2 focus:ring-ring disabled:opacity-50 transition-colors font-heading"
            >
              {isSubmitting ? (
                <span>Vérification JWT...</span>
              ) : (
                <>
                  <LogIn className="w-4 h-4 mr-2 text-primary" />
                  <span>Se connecter (POST /api/auth/login)</span>
                </>
              )}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Première utilisation ? <Link to="/inscription" className="font-semibold text-primary-foreground underline">Créer une tontine et un compte ADMIN</Link>
          </p>

        </div>
      </div>
    </div>
  );
};

export default LoginPage;
