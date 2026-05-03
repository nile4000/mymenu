# MyMenu – Projektrichtlinien für Claude

## Projektüberblick

Full-Stack-Applikation zum Importieren, Analysieren und Kategorisieren von Kassenbons.

- **Middleware**: Quarkus 3 + Kotlin, Maven, PostgreSQL, OpenAI-API
- **Frontend**: Vue 3 + TypeScript, Quasar, Pinia, Supabase

---

## Architektur

### Middleware – Schichtenmodell (DDD)

```
api/        REST-Endpunkte, DTOs, Request-/Response-Typen
app/        Business-Logik, Orchestrierung, Services
infra/      Framework-Integration (DB, HTTP, PDFBox)
domain/     Reine Datenklassen, keine Abhängigkeiten
shared/     Querschnittsthemen: Error-Handling, Config, Clients
```

Abhängigkeiten fließen nur nach innen: `api → app → infra`, `domain` ist frei von Abhängigkeiten.

### Frontend – Feature-Struktur

```
pages/      Seiten und ihre lokalen Composables
components/ Wiederverwendbare UI-Komponenten
services/   API-Calls und Transformationen
helpers/    Composables, Interfaces, Utilities
stores/     Pinia Stores (globaler State)
```

---

## Coding-Richtlinien

### Allgemein

- Schreibe einfachen, direkt lesbaren Code. Vermeide unnötige Abstraktionen.
- Keine vorzeitige Generalisierung – drei ähnliche Zeilen sind besser als eine schlechte Abstraktion.
- Keine halbfertigen Implementierungen oder Feature-Flags.
- Keine Backwards-Compatibility-Shims für Code, der nirgendwo mehr verwendet wird.

### Kommentare

Kommentare nur dann, wenn das **Warum** nicht aus dem Code selbst hervorgeht:

```kotlin
// PDFBox emits harmless font-fallback warnings for embedded Type1 fonts;
// text extraction is unaffected since glyphs are Unicode-encoded in the PDF.
```

Nicht kommentieren:
- Was der Code tut (das zeigen die Namen)
- Aufgaben-Referenzen oder Ticket-Nummern
- Mehrzeilige Docstrings für offensichtliche Methoden

### Fehlerbehandlung

- Nur an Systemgrenzen validieren (User-Input, externe APIs).
- Interne Invarianten per `require()` / `check()` absichern – nicht defensiv für unmögliche Zustände.
- Custom Exceptions für fachliche Fehlerdomänen (`UploadOpenAiException`).
- HTTP-Fehler ausschließlich über `GlobalExceptionMapper` – kein manuelles `Response.status()` in Services.

### Keine Fehlerbehandlung für Szenarien, die nicht eintreten können

```kotlin
// Falsch: unnötige Null-Prüfung für intern erzeugte Liste
val items = buildItemList()
if (items == null) return  // buildItemList gibt niemals null zurück

// Richtig:
val items = buildItemList()
```

---

## Middleware (Kotlin / Quarkus)

### Stil

- Kotlin-Idiome bevorzugen: `when`, Extension Functions, `data class`, `object` für Singletons.
- `BigDecimal` für alle Währungsbeträge.
- Java `Logger` (nicht SLF4J) mit Trace-IDs im Format `[operation:$traceId]`.
- Constructor-Injection mit `@Inject` – kein Field-Injection.

### Struktur

```kotlin
@ApplicationScoped
class MyService @Inject constructor(
    private val dependency: Dependency
) {
    fun publicOperation(input: Input): Output {
        validate(input)
        return process(input)
    }

    private fun validate(input: Input) { ... }
    private fun process(input: Input): Output { ... }

    companion object {
        private val LOGGER = Logger.getLogger(MyService::class.java.name)
    }
}
```

### Datenbank

- Rohe JDBC über `prepareStatement` – kein ORM.
- Transaktionen immer explizit: `conn.autoCommit = false`, `commit()` / `rollback()`.
- `AutoCloseable`-Ressourcen immer mit `.use { }` schließen.

### HTTP-Responses

Statuscodes:
- `200` – Erfolg
- `422` – Validierungsfehler (IllegalArgumentException)
- `502` – Upload-Fehler (OpenAI)
- `500` – Unbehandelte Ausnahmen

Response-Body immer `ApiError(code, message)` bei Fehler.

---

## Frontend (Vue 3 / TypeScript)

### Stil

- Composition API mit `defineComponent()` und `setup()`-Funktion.
- Kein `<script setup>` – bestehende Komponenten nutzen Options-Stil mit Composition API.
- Reaktive Werte aus externen Quellen immer mit `unref()` zugreifen, um Ref/Value-Transparenz zu gewährleisten.
- `computed()` für alle abgeleiteten Werte – niemals in Template-Expressions berechnen.

### ServiceResult-Pattern

Alle API-Calls geben `ServiceResult<T>` zurück:

```typescript
const result = await someApiCall();
if (result.ok) {
  // result.data ist T
} else {
  handleError("Kontext", result.error.message, $q);
}
```

Niemals direkt `.data` ohne `result.ok`-Prüfung zugreifen.

### Composables

```typescript
export function useFeature(options: UseFeatureOptions = {}) {
  const state = reactive({ ... });

  async function doSomething() { ... }

  return { state, doSomething };
}
```

- Ein Composable pro fachlichem Concern.
- Seiteneffekte (Notifications, Loading) innerhalb des Composable kapseln.
- `onMounted` nur für initiales Laden, nicht für reaktive Abhängigkeiten.

### TypeScript

- Interfaces für alle API-Datenstrukturen in `helpers/interfaces/`.
- API-Felder in `Snake_Case` (Middleware-Mapping), lokale Variablen in `camelCase`.
- Generics nutzen statt `any` – `any` ist verboten.
- Optionale Felder mit `?`, keine Union mit `undefined` explizit setzen wenn `?` ausreicht.

### Quasar-Komponenten

- Quasar-Komponenten (`q-btn`, `q-table`, etc.) direkt nutzen – keine eigenen Wrapper-Komponenten dafür erstellen.
- SCSS scoped in Komponenten, globale Variablen über Quasar-Theme-Variablen (`$primary`, etc.).

---

## Was Claude nicht tun soll

- Keine Refactorings oder Abstraktionen, die über die konkrete Aufgabe hinausgehen.
- Keine zusätzliche Fehlerbehandlung für Szenarien, die im Kontext unmöglich sind.
- Keine neuen Dateien erstellen, wenn eine bestehende bearbeitet werden kann.
- Keine Markdown-Dokumentationsdateien anlegen, außer wenn explizit gewünscht.
- Keine Emojis in Code oder Kommentaren.
- Keine `console.log`-Statements in produktivem Frontend-Code hinterlassen.
- Keine `TODO`-Kommentare ohne konkreten Inhalt.

---

## Änderungen prüfen

Vor dem Abschluss einer Aufgabe sicherstellen:
1. Typen stimmen durch (Kotlin: kompilierbar; TypeScript: keine `any`-Lücken).
2. Neue Parameter sind optional oder überall konsistent ergänzt.
3. Kein toter Code hinterlassen.
4. Kommentare beschreiben das Warum, nicht das Was.
