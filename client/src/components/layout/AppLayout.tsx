import { useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { BookOpenCheck, Calendar, Coins, FileCheck2, FileSpreadsheet, Landmark, LayoutDashboard, LogOut, Menu, ShieldCheck, UserCheck, Users, Wallet, X } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import type { Role } from "../../api/types";

interface NavItem { name: string; path: string; icon: typeof LayoutDashboard; roles?: Role[]; }

const ALL_ITEMS: NavItem[] = [
  { name: "Tableau de bord", path: "/", icon: LayoutDashboard, roles: ["ADMIN", "GESTIONNAIRE", "TRESORIER"] },
  { name: "Cotisations", path: "/cotisations", icon: Coins, roles: ["ADMIN", "GESTIONNAIRE", "TRESORIER"] },
  { name: "Demandes & prêts", path: "/prets", icon: FileSpreadsheet },
  { name: "Membres", path: "/membres", icon: Users, roles: ["ADMIN", "GESTIONNAIRE"] },
  { name: "Adhésions", path: "/adhesions", icon: UserCheck, roles: ["ADMIN", "GESTIONNAIRE"] },
  { name: "Cycles", path: "/cycles", icon: Calendar, roles: ["ADMIN", "GESTIONNAIRE"] },
  { name: "Caisse", path: "/caisse", icon: Wallet, roles: ["ADMIN", "GESTIONNAIRE", "TRESORIER"] },
  { name: "Remboursements", path: "/remboursements", icon: FileCheck2, roles: ["ADMIN", "TRESORIER"] },
  { name: "Tontines", path: "/tontines", icon: Landmark, roles: ["ADMIN"] },
  { name: "Utilisateurs", path: "/utilisateurs", icon: ShieldCheck, roles: ["ADMIN", "GESTIONNAIRE"] },
  { name: "Reçus PDF", path: "/recus", icon: BookOpenCheck },
];

export default function AppLayout() {
  const { user, logout, hasRole } = useAuth();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const items = ALL_ITEMS.filter((item) => !item.roles || hasRole(item.roles));

  const handleLogout = () => { logout(); navigate("/login"); };
  const roleLabel = user?.roles?.join(", ") ?? "MEMBRE";

  return (
    <div className="min-h-dvh bg-background text-foreground md:flex">
      <aside className="hidden w-64 shrink-0 border-r border-border bg-card px-4 py-6 md:flex md:flex-col">
        <div className="mb-8 px-2"><span className="font-display text-lg font-semibold">Akiwacu</span><p className="text-xs text-muted-foreground">Gestion de tontines</p></div>
        <NavList items={items} currentPath={location.pathname} onNavigate={() => undefined} />
        <div className="mt-auto border-t border-border pt-4 text-xs text-muted-foreground">
          <p className="font-medium text-foreground">{user?.prenom} {user?.nom}</p><p>{roleLabel}</p>
          <button type="button" onClick={handleLogout} className="mt-4 flex min-h-12 w-full items-center gap-2 rounded-lg px-3 text-destructive hover:bg-muted"><LogOut size={17} /> Déconnexion</button>
        </div>
      </aside>

      <div className="flex min-h-dvh min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-border bg-secondary px-4 text-secondary-foreground md:px-8">
          <button type="button" onClick={() => setDrawerOpen(true)} className="touch-target rounded-lg p-2 md:hidden" aria-label="Ouvrir le menu"><Menu size={22} /></button>
          <div><p className="font-display text-lg font-semibold">Akiwacu</p><p className="hidden text-xs text-secondary-foreground/70 sm:block">Le Compteur</p></div>
          <div className="text-right text-xs"><p>{user?.prenom} {user?.nom}</p><p className="text-secondary-foreground/70">{roleLabel}</p></div>
        </header>
        <main className="flex-1 px-4 py-6 pb-24 md:px-8 md:pb-8"><Outlet /></main>
        <nav className="fixed inset-x-0 bottom-0 z-20 flex border-t border-border bg-card md:hidden">
          {items.slice(0, 4).map((item) => <NavLink key={item.path} to={item.path} end={item.path === "/"} className={({ isActive }) => `touch-target flex flex-1 flex-col items-center justify-center gap-1 text-[11px] ${isActive ? "font-semibold text-primary" : "text-muted-foreground"}`}><item.icon size={20} /><span>{item.name}</span></NavLink>)}
          <button type="button" onClick={() => setDrawerOpen(true)} className="touch-target flex flex-1 flex-col items-center justify-center gap-1 text-[11px] text-muted-foreground"><Menu size={20} /><span>Menu</span></button>
        </nav>
      </div>

      {drawerOpen && <div className="fixed inset-0 z-40 md:hidden"><button type="button" aria-label="Fermer le menu" className="absolute inset-0 bg-foreground/40" onClick={() => setDrawerOpen(false)} /><aside className="relative z-10 flex h-full w-80 max-w-[85vw] flex-col bg-card p-4 shadow-xl"><div className="flex items-center justify-between border-b border-border pb-4"><div><p className="font-display font-semibold">Navigation</p><p className="text-xs text-muted-foreground">{roleLabel}</p></div><button type="button" onClick={() => setDrawerOpen(false)} className="touch-target rounded-lg p-2" aria-label="Fermer"><X size={20} /></button></div><div className="mt-4 flex-1"><NavList items={items} currentPath={location.pathname} onNavigate={() => setDrawerOpen(false)} /></div><button type="button" onClick={handleLogout} className="touch-target flex items-center justify-center gap-2 rounded-lg border border-destructive/30 text-destructive"><LogOut size={17} /> Déconnexion</button></aside></div>}
    </div>
  );
}

function NavList({ items, currentPath, onNavigate }: { items: NavItem[]; currentPath: string; onNavigate: () => void }) {
  return <nav className="space-y-1">{items.map((item) => <NavLink key={item.path} to={item.path} end={item.path === "/"} onClick={onNavigate} className={({ isActive }) => `flex min-h-12 items-center gap-3 rounded-lg px-3 text-sm font-medium ${isActive || (item.path !== "/" && currentPath.startsWith(item.path)) ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-muted hover:text-foreground"}`}><item.icon size={19} />{item.name}</NavLink>)}</nav>;
}
