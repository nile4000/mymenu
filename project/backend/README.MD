# Quarkus-PDF-Extract Projekt

- [PDFBox](https://pdfbox.apache.org/)
- [ChatGPT](https://chat.openai.com/)

## Middleware Übersicht

- Auslesen von Text aus mehreren PDFs
- Extrahieren von Textstellen und Metadaten
- API Anfragen an ChatGPT senden und empfangen

### Start development

```shell
quarkus dev
```

```shell
./mvnw compile quarkus:dev
```

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
