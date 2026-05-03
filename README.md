# My Menü — AI Essensplaner

[![Deploy](https://github.com/nile4000/mymenu/actions/workflows/static.yml/badge.svg)](https://github.com/nile4000/mymenu/actions/workflows/static.yml)
[![Last Commit](https://img.shields.io/github/last-commit/nile4000/mymenu)](https://github.com/nile4000/mymenu/commits/main)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

![Vue 3](https://img.shields.io/badge/Vue-3-42b883?logo=vue.js&logoColor=white)
![Quasar](https://img.shields.io/badge/Quasar-2-1976D2?logo=quasar&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)
![Quarkus](https://img.shields.io/badge/Quarkus-3.22-4695EB?logo=quarkus&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-336791?logo=postgresql&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-GPT-412991?logo=openai&logoColor=white)

![Showcase](image.png)

**Live-App:** <https://nile4000.github.io/mymenu/#/>

---

## Überblick

My Menü ist eine Web-App zum Erfassen, Strukturieren und Auswerten von Einkaufsbelegen. Die App liest PDF-Belege ein, extrahiert daraus Artikel, Preise, Mengen und Belegmetadaten und macht die Daten in einer übersichtlichen Oberfläche nutzbar.

Der Fokus liegt auf Schweizer Einkaufsbelegen von Coop/Supercard und Migros/Cumulus. Importierte Artikel können gefiltert, korrigiert, kategorisiert und für Rezeptvorschläge verwendet werden.

---

## Funktionen

- PDF-Belege hochladen und auslesen
- Artikel, Mengen, Preise, Rabatte und Belegmetadaten strukturiert erfassen
- Einkaufsartikel nach Kategorien auswerten
- Einheiten wie `g`, `kg`, `ml`, `l` oder `stk` automatisch erkennen
- Kategorien zentral über das Backend bereitstellen
- Rezeptvorschläge aus vorhandenen Artikeln erzeugen
- Ausgaben nach Beleg, Zeitraum und Kategorie analysieren
- Supercard-Belege über eine manuelle Session synchronisieren

---

## AI-Funktionen

| Funktion            | Beschreibung                                                                           |
| ------------------- | -------------------------------------------------------------------------------------- |
| Beleganalyse        | PDF-Text wird im Backend vorbereitet und per OpenAI in strukturiertes JSON umgewandelt |
| Kategorisierung     | Artikel werden über einen eingebetteten AI-Service Einkaufskategorien zugeordnet       |
| Einheitenerkennung  | Aus Artikelnamen und Preisen werden sinnvolle Einheiten extrahiert                     |
| Rezeptgenerierung   | Aus vorhandenen Lebensmitteln erstellt die AI deutschsprachige Rezeptvorschläge        |

---

## Technischer Aufbau

```text
mymenu/
├── frontend/           # Quasar/Vue 3 SPA
├── middleware/         # Kotlin/Quarkus REST API
├── categorization-srv/ # Embedded Python AI-Service
├── database/           # SQL-Schema, Migrations, Snapshots
└── docker-compose.yml
```

| Schicht       | Technologie                                        |
| ------------- | -------------------------------------------------- |
| Frontend      | Vue 3, Quasar, TypeScript, Pinia, Supabase Client  |
| Backend       | Kotlin 2.1, Quarkus 3.22, Maven, JDBC/PostgreSQL   |
| Datenhaltung  | Supabase (PostgreSQL)                              |
| AI            | OpenAI API, Embedded Categorization Service        |
| Deployment    | GitHub Pages (Frontend), Docker (Backend)          |

---

## Backend-Endpunkte

| Methode  | Pfad                                    | Beschreibung                         |
| -------- | --------------------------------------- | ------------------------------------ |
| `POST`   | `/api/extract-pdf`                      | PDF-Beleg auslesen                   |
| `GET`    | `/api/categories`                       | Verfügbare Kategorien abrufen        |
| `POST`   | `/api/ai/categorize`                    | Artikel kategorisieren               |
| `POST`   | `/api/ai/extract-unit`                  | Einheiten aus Artikeln extrahieren   |
| `POST`   | `/api/ai/recipe`                        | Rezept aus Artikeln erstellen        |
| `GET`    | `/api/integrations/supercard/status`    | Supercard-Session prüfen             |
| `GET`    | `/api/integrations/supercard/available` | Verfügbare Supercard-Belege anzeigen |
| `POST`   | `/api/integrations/supercard/sync`      | Supercard-Belege importieren         |

---

## Lokale Entwicklung

**Backend:**

```shell
cd middleware
quarkus dev
```

**Frontend:**

```shell
cd frontend
npm install
npm run start
```

**Docker (alle Services):**

```shell
docker-compose up
```
