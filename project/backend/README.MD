# Quarkus-PDF-Extract Projekt

## Middleware Übersicht

- Auslesen von Text aus PDFs
- Extrahieren von bestimmten Textstellen und Metadaten
- [PDFBox](https://pdfbox.apache.org/)

- API Anfragen senden und empfangen
- [ChatGPT](https://chat.openai.com/)

- Speichern von Daten in einer Datenbank
- Coming soon...


### Start development

```shell
quarkus dev
```

```shell
./mvnw compile quarkus:dev
```

### Testen

<http://localhost:8080/q/swagger-ui/#>

## Deployment auf fly.io, build package first

```shell
./mvnw package
```

```shell
flyctl deploy
```

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

It will be created under `target/*-runner.exe`.
