import { NavLink, Outlet } from "react-router-dom";
import { cn } from "@/lib/utils";

interface NavItem {
  to: string;
  label: string;
  icon: (active: boolean) => React.ReactNode;
}

function IconAccueil(active: boolean) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2.2 : 1.8}>
      <path d="M4 11.5L12 4l8 7.5" />
      <path d="M6 10v9a1 1 0 0 0 1 1h4v-6h2v6h4a1 1 0 0 0 1-1v-9" />
    </svg>
  );
}

function IconCotisations(active: boolean) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2.2 : 1.8}>
      <ellipse cx="12" cy="6.5" rx="7" ry="3" />
      <path d="M5 6.5V17c0 1.7 3.1 3 7 3s7-1.3 7-3V6.5" />
      <path d="M19 11.7c0 1.7-3.1 3-7 3s-7-1.3-7-3" />
    </svg>
  );
}

function IconPrets(active: boolean) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2.2 : 1.8}>
      <path d="M4 19V9l8-5 8 5v10" />
      <line x1="10" y1="19" x2="10" y2="12" />
      <line x1="14" y1="19" x2="14" y2="12" />
    </svg>
  );
}

function IconProfil(active: boolean) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2.2 : 1.8}>
      <circle cx="12" cy="8" r="3.5" />
      <path d="M5 20c0-3.9 3.1-7 7-7s7 3.1 7 7" />
    </svg>
  );
}

const NAV_ITEMS: NavItem[] = [
  { to: "/", label: "Accueil", icon: IconAccueil },
  { to: "/cotisations", label: "Cotisations", icon: IconCotisations },
  { to: "/prets", label: "Prêts", icon: IconPrets },
  { to: "/profil", label: "Profil", icon: IconProfil },
];

/**
 * Un seul layout, deux présentations en CSS pur : navigation basse sous
 * md, barre latérale à partir de md (docs/GUIDE-CLIENT-REACT.md P1).
 * Pas de détection JS du viewport — évite tout flash au premier rendu.
 */
export function AppLayout() {
  return (
    <div className="min-h-dvh md:flex">
      <aside className="hidden md:flex md:w-60 md:flex-col md:border-r md:border-border md:bg-card md:px-4 md:py-6">
        <div className="mb-8 px-2">
          <span className="font-display text-lg font-semibold">Akiwacu</span>
        </div>
        <nav className="flex flex-col gap-1">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) =>
                cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium",
                  isActive
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground hover:bg-muted hover:text-foreground",
                )
              }
            >
              {({ isActive }) => (
                <>
                  {item.icon(isActive)}
                  {item.label}
                </>
              )}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex min-h-dvh flex-1 flex-col">
        <main className="flex-1 pb-20 md:pb-0">
          <Outlet />
        </main>

        <nav className="fixed inset-x-0 bottom-0 flex border-t border-border bg-card md:hidden">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) =>
                cn(
                  "flex flex-1 flex-col items-center gap-1 py-2.5 text-[11px] font-medium",
                  isActive ? "text-primary" : "text-muted-foreground",
                )
              }
            >
              {({ isActive }) => (
                <>
                  {item.icon(isActive)}
                  {item.label}
                </>
              )}
            </NavLink>
          ))}
        </nav>
      </div>
    </div>
  );
}
