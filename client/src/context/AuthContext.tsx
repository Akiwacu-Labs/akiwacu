import { createContext, useContext, useEffect, useState } from "react";
import { apiClient, decodeToken, getStoredToken, removeStoredToken, setStoredToken } from "../api/client";
import type { DecodedToken, Role } from "../api/types";

interface AuthContextValue {
  user: DecodedToken | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, motDePasse: string) => Promise<void>;
  logout: () => void;
  hasRole: (roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken());
  const [user, setUser] = useState<DecodedToken | null>(() => {
    const currentToken = getStoredToken();
    return currentToken ? decodeToken(currentToken) : null;
  });

  const logout = () => {
    removeStoredToken();
    setToken(null);
    setUser(null);
  };

  useEffect(() => {
    const handleUnauthorized = () => logout();
    window.addEventListener("akiwacu-unauthorized", handleUnauthorized);
    return () => window.removeEventListener("akiwacu-unauthorized", handleUnauthorized);
  }, []);

  const login = async (email: string, motDePasse: string) => {
    const response = await apiClient.login({ email, motDePasse });
    if (!response.jeton) throw new Error("Le serveur n’a pas renvoyé de session.");
    setStoredToken(response.jeton);
    setToken(response.jeton);
    setUser(decodeToken(response.jeton));
  };

  const hasRole = (roles: Role[]) => {
    if (!user?.roles?.length) return false;
    return roles.some((role) => user.roles?.includes(role));
  };

  const value = { user, token, isAuthenticated: Boolean(token && user), login, logout, hasRole };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Context hooks are intentionally colocated with their provider for this small client.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth doit être utilisé dans AuthProvider.");
  return context;
}
