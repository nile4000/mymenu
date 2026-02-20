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

```shell
cd C:\dev\mymenu\project\backend\db

# 1) Restart local Supabase stack cleanly
npx supabase stop --no-backup
npx supabase start

# 2) Load dummy data from SQL dump into local DB
Get-Content .\db_cluster.backup -Raw | docker exec -i supabase_db_db psql -U postgres -d postgres

# 3) Optional check
npx supabase status
docker exec supabase_db_db psql -U postgres -d postgres -c "select count(*) as article_count from public.article; select count(*) as receipt_count from public.receipt;"
```

Note:

- `db_cluster.backup` is a SQL text dump (not `pg_restore` format).
- For a full reset, run `npx supabase stop --no-backup` and then the steps above again.

### Endpoint testing

<http://localhost:8080/q/swagger-ui/#>

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
