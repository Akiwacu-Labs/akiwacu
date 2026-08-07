# Métriques métier à instrumenter (M2)

L'énoncé §10 exige que Grafana affiche **des métriques techniques ET des
indicateurs métier**. Les techniques sortent gratuitement d'Actuator ; les
métiers doivent être instrumentés à la main avec Micrometer.

## Compteurs et jauges à exposer

| Métrique | Type | Où l'instrumenter |
|---|---|---|
| `akiwacu_membres_total` | Gauge | `MembreService` — nombre de membres actifs, par tontine |
| `akiwacu_cotisations_montant_total` | Gauge | `CotisationService` — somme des cotisations du cycle actif |
| `akiwacu_cotisations_enregistrees_total` | Counter | incrémenté à chaque cotisation |
| `akiwacu_prets_montant_total` | Gauge | `PretService` — encours de prêts |
| `akiwacu_prets_accordes_total` | Counter | incrémenté à chaque prêt débloqué |
| `akiwacu_prets_refuses_total` | Counter | par motif (R3, R6, R7) — très parlant en démo |
| `akiwacu_remboursements_montant_total` | Gauge | `RemboursementService` |
| `akiwacu_solde_caisse` | Gauge | `CaisseService` — par tontine |
| `akiwacu_cycles_actifs` | Gauge | `CycleService` |

Tagger systématiquement par `tontine` pour pouvoir filtrer dans Grafana.

## Exemple d'instrumentation

```java
@Service
@RequiredArgsConstructor
public class CotisationService {

    private final MeterRegistry meterRegistry;
    private final CotisationRepository repository;

    @Transactional
    public CotisationResponse enregistrer(CotisationRequest request) {
        // … règles R2, R5 …
        var saved = repository.save(cotisation);

        meterRegistry.counter("akiwacu.cotisations.enregistrees",
                "tontine", tenantContext.getTontineNom()).increment();
        meterRegistry.summary("akiwacu.cotisations.montant",
                "tontine", tenantContext.getTontineNom())
                .record(saved.getMontant().doubleValue());

        return mapper.toResponse(saved);
    }
}
```

## Dashboards Grafana à produire (livrable §12.7)

1. **Dashboard technique** — CPU, mémoire JVM, heap, threads, requêtes HTTP par
   statut, latence p95, disponibilité (uptime), GC.
2. **Dashboard métier** — nombre de membres, total des cotisations, total des
   prêts, total des remboursements, solde de caisse, prêts refusés par motif.

Exporte les deux en JSON (`Dashboard settings → JSON model`) et versionne-les
dans `monitoring/grafana/`. C'est le livrable exigé, pas la capture d'écran.

## Astuce captures d'écran

Des graphes plats font mauvais effet dans le rapport. La veille des captures,
génère du trafic :

```bash
k6 run --vus 10 --duration 5m charge.js
# ou, plus simple
ab -n 5000 -c 10 http://VM_DEV_IP:8080/api/tontines
```

Et fais passer un jeu de données de démo réaliste (M4, D12) pour que les
indicateurs métier aient des valeurs crédibles.
