import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AppLayout } from "@/layouts/AppLayout";
import { PagePlaceholder } from "@/pages/PagePlaceholder";

const queryClient = new QueryClient();

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route index element={<PagePlaceholder titre="Accueil" />} />
            <Route path="cotisations" element={<PagePlaceholder titre="Cotisations" />} />
            <Route path="prets" element={<PagePlaceholder titre="Prêts" />} />
            <Route path="profil" element={<PagePlaceholder titre="Profil" />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
