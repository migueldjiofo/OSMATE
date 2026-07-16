# Docker-Dokumentation

## Ziel

Diese Dokumentation beschreibt, wie das OSMATE-Backend mit Docker gestartet wird.

Docker wird genutzt, um das Backend reproduzierbar und unabhängig von der lokalen Python-Installation auszuführen.

## Voraussetzungen

- Docker Desktop
- Docker-Kontext `desktop-linux`
- freier Port `8000`

Docker prüfen:

```powershell
docker version
docker context ls
```

Der aktive Kontext sollte sein:

```text
desktop-linux
```

## Docker-Konfiguration prüfen

```powershell
cd C:\Users\migue\Projects\OSMATE
docker compose config
```

Diese Prüfung validiert `docker-compose.yml`.

## Image bauen

```powershell
docker compose build
```

Dabei wird das Image `osmate-osmate-backend` erstellt.

## Container starten

```powershell
docker compose up -d
```

## Health Check

```powershell
Invoke-RestMethod http://127.0.0.1:8000/api/v1/health
```

Erwartete Antwort:

```text
status      : ok
service     : OSMATE Backend
environment : development
```

## Logs anzeigen

```powershell
docker compose logs --tail=50 osmate-backend
```

Erwarteter Log-Ausschnitt:

```text
Uvicorn running on http://0.0.0.0:8000
GET /api/v1/health HTTP/1.1" 200 OK
```

## Container stoppen

```powershell
docker compose down
```

## Cache-Volume

Das Docker-Setup nutzt ein Volume für den SQLite-Cache:

```text
osmate-backend-cache
```

Im Container liegt der Cache unter:

```text
/app/data/cache/osmate_cache.sqlite3
```

Das Volume bleibt erhalten, auch wenn der Container gestoppt wird.

## Volume vollständig löschen

Nur bei Bedarf:

```powershell
docker compose down -v
```

Dadurch wird auch der Cache gelöscht.

## Wichtige Dateien

```text
backend/Dockerfile
backend/.dockerignore
docker-compose.yml
```