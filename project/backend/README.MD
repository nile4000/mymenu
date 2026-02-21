# Quarkus-PDF-Extract for my-menu

- [PDFBox](https://pdfbox.apache.org/)
- [ChatGPT-API](https://chat.openai.com/)

## Middleware Overview

- Auslesen von Text aus mehreren PDFs
- Extrahieren von Textstellen und Metadaten mit Regex
- API Anfragen an ChatGPT senden und empfangen

### Local development

```shell
cd C:\dev\mymenu\project\backend

quarkus dev
```

```shell
./mvnw compile quarkus:dev
```

### Local DB with dummy data (restore)

Prerequisite: Docker Desktop must be running.

```powershell
cd C:\dev\mymenu\project\backend\db

# Restore local DB snapshot
.\restore-local-db.ps1 -DumpFile local_dev_public.sql

# Save current local DB snapshot
.\save-local-db.ps1 -OutputFile local_dev_public.sql
```

Note:

- `local_dev_*.sql` files are ignored by git (local-only snapshots).
- Scripts restore/export the `public` schema only, for local development speed.

### Endpoint testing

<http://localhost:8080/q/swagger-ui/#>

Quick backend-only tests (PowerShell, from project root):

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/ai/categorize" -Method Post -ContentType "application/json" -Body (Get-Content .\tmp_categorize_payload.json -Raw)
Invoke-RestMethod -Uri "http://localhost:8080/api/ai/extract-unit" -Method Post -ContentType "application/json" -Body (Get-Content .\tmp_extract_unit_payload.json -Raw)
Invoke-RestMethod -Uri "http://localhost:8080/api/ai/recipe" -Method Post -ContentType "application/json" -Body (Get-Content .\tmp_recipe_payload.json -Raw)
```

Optional for more recipe variety:

```env
OPENAI_TEMPERATURE_RECIPE=0.6
```

## Deployment production to fly.io

### Testing the container locally for fly.io

```shell
docker-compose up -d
```

### Build package first for production

```shell
./mvnw package
```

### Deploy to production

```shell
flyctl deploy
```

## Creating a native executable

You can create a native executable (.exe) using:

```shell script
./mvnw package -Dnative
```

It will be created under `target/*-runner.exe`.

## Get Swagger docs (only in dev)

<http://localhost:8080/swagger?format=json>
