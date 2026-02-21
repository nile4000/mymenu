# Service Layer

Struktur:

- `app/`: oeffentliche Use-Case/Flow-Funktionen (von UI/Stores genutzt)
- `infra/`: technische Kommunikation (HTTP/Supabase aka. postGres-DB/Realtime aka. postGres zu FE)
- `api/`: externe Contract-/Parser-Logik
- `shared/`: wiederverwendbare technische Utilities

Regel:

- Komponenten, Stores und Composables importieren nur `src/services/index.ts`.
- `infra` und `api` werden nur von `app` (oder intern innerhalb services) genutzt.
- Oeffentliche Servicefunktionen liefern `ServiceResult<T>` statt klassische Exceptions.
