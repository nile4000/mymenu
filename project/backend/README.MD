# Quarkus-PDF-Extract Projekt

## pdfbox Übersicht

- Konversion von Dokumenten.

### Start development

```shell
quarkus dev
```

```shell
./mvnw compile quarkus:dev
```

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
