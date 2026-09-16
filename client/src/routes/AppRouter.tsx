import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import ProtectedRoute from '../components/layout/ProtectedRoute';
import RoleGuard from '../components/layout/RoleGuard';

// Screen imports
import LoginPage from '../pages/LoginPage';
import SignupPage from '../pages/SignupPage';
import DashboardPage from '../pages/DashboardPage';
import CotisationsPage from '../pages/CotisationsPage';
import PretsPage from '../pages/PretsPage';
import MembresPage from '../pages/MembresPage';
import AdhesionsPage from '../pages/AdhesionsPage';
import CyclesPage from '../pages/CyclesPage';
import CaissePage from '../pages/CaissePage';
import RemboursementsPage from '../pages/RemboursementsPage';
import TontinesPage from '../pages/TontinesPage';
import UtilisateursPage from '../pages/UtilisateursPage';
import RecusPage from '../pages/RecusPage';
import { useAuth } from '../context/AuthContext';

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      {/* Public Authentication Route */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/inscription" element={<SignupPage />} />

      {/* Protected Main Application Layout */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        {/* 1. Dashboard (GET /api/dashboard) */}
        <Route index element={<RoleEntryPoint />} />

        {/* 2. Flagship Cotisations (Direction C Compteur, GET, POST, POST/batch, PUT) */}
        <Route
          path="cotisations"
          element={
            <RoleGuard roles={['ADMIN', 'GESTIONNAIRE', 'TRESORIER']}>
              <CotisationsPage />
            </RoleGuard>
          }
        />

        {/* 3. Demandes de prêt, votes commissaires, déblocage et échéancier */}
        <Route path="prets" element={<PretsPage />} />

        {/* 4. Registre des membres (ADMIN, GESTIONNAIRE) */}
        <Route
          path="membres"
          element={
            <RoleGuard roles={['ADMIN', 'GESTIONNAIRE']}>
              <MembresPage />
            </RoleGuard>
          }
        />

        {/* 5. Adhésions aux cycles (ADMIN, GESTIONNAIRE) */}
        <Route
          path="adhesions"
          element={
            <RoleGuard roles={['ADMIN', 'GESTIONNAIRE']}>
              <AdhesionsPage />
            </RoleGuard>
          }
        />

        {/* 6. Cycles et machine à états (ADMIN, GESTIONNAIRE) */}
        <Route
          path="cycles"
          element={
            <RoleGuard roles={['ADMIN', 'GESTIONNAIRE']}>
              <CyclesPage />
            </RoleGuard>
          }
        />

        {/* 7. Grand livre Caisse & Solde (ADMIN, TRESORIER, GESTIONNAIRE) */}
        <Route
          path="caisse"
          element={
            <RoleGuard roles={['ADMIN', 'TRESORIER', 'GESTIONNAIRE']}>
              <CaissePage />
            </RoleGuard>
          }
        />

        {/* 8. Remboursements & Versements (ADMIN, TRESORIER) */}
        <Route
          path="remboursements"
          element={
            <RoleGuard roles={['ADMIN', 'TRESORIER']}>
              <RemboursementsPage />
            </RoleGuard>
          }
        />

        {/* 9. Administration des tontines multi-tenant (ADMIN) */}
        <Route
          path="tontines"
          element={
            <RoleGuard roles={['ADMIN']}>
              <TontinesPage />
            </RoleGuard>
          }
        />

        {/* 10. Utilisateurs & Habilitations RBAC (ADMIN, GESTIONNAIRE) */}
        <Route
          path="utilisateurs"
          element={
            <RoleGuard roles={['ADMIN', 'GESTIONNAIRE']}>
              <UtilisateursPage />
            </RoleGuard>
          }
        />

        {/* 11. Reçus scellés R8 et téléchargement PDF */}
        <Route path="recus" element={<RecusPage />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRouter;

function RoleEntryPoint() {
  const { hasRole } = useAuth();
  return hasRole(['ADMIN', 'GESTIONNAIRE', 'TRESORIER'])
    ? <DashboardPage />
    : <Navigate to="/prets" replace />;
}
