# OSMATE

OSMATE ist ein agentenbasierter räumlicher Assistent für OpenStreetMap (OSM).

Das Projekt entwickelt eine Android-Anwendung, die natürlichsprachliche Suchanfragen in strukturierte räumliche Abfragen übersetzt und die Ergebnisse auf einer interaktiven Karte visualisiert.

## Ziel des Projekts

Ziel ist die Entwicklung einer mobilen Geoanwendung, die den Zugang zu OpenStreetMap-Daten vereinfacht. Nutzerinnen und Nutzer sollen räumliche Fragen in natürlicher Sprache stellen können, ohne Overpass QL direkt schreiben zu müssen.

Beispiel:

```text
Finde Cafes mit Terrasse in meiner Nähe
```

Die Anfrage wird im Backend verarbeitet und in eine strukturierte Suchlogik übersetzt:

```text
Natürliche Sprache
→ AgentPlan
→ Overpass QL
→ Overpass API
→ GeoJSON
→ Kartenanzeige in der Android-App
```

## Aktueller Entwicklungsstand

Der aktuelle Stand umfasst ein funktionsfähiges Backend mit:

- FastAPI als Backend-Framework
- regelbasiertem Agentenplaner
- Overpass-QL-Generator
- Overpass-API-Anbindung
- Nominatim-Geokodierung
- GeoJSON-Konvertierung
- SQLite-Cache
- Docker-Unterstützung
- automatisierten Tests mit pytest
- Codeprüfung mit Ruff

## Projektstruktur

```text
OSMATE
├── android-app
├── backend
│   ├── app
│   │   ├── agent
│   │   ├── api
│   │   ├── core
│   │   ├── schemas
│   │   ├── services
│   │   └── tests
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pyproject.toml
│   └── requirements.txt
├── docs
├── scripts
├── docker-compose.yml
├── .gitattributes
├── .gitignore
└── README.md
```

## Backend lokal starten

```powershell
cd C:\Users\migue\Projects\OSMATE
.\scripts\run_backend.ps1
```

Danach ist die Dokumentation der API erreichbar unter:

```text
http://127.0.0.1:8000/docs
```

## Backend mit Docker starten

```powershell
cd C:\Users\migue\Projects\OSMATE
docker compose up -d
```

Health Check:

```powershell
Invoke-RestMethod http://127.0.0.1:8000/api/v1/health
```

Container stoppen:

```powershell
docker compose down
```

## Tests ausführen

```powershell
cd C:\Users\migue\Projects\OSMATE
.\scripts\run_backend_tests.ps1
```

## Codequalität prüfen

```powershell
cd C:\Users\migue\Projects\OSMATE
.\backend\.venv\Scripts\python.exe -m ruff check .\backend\app
```

## Wichtige Dokumente

- `docs/backend.md`: technische Backend-Dokumentation
- `docs/docker.md`: Docker-Ausführung
- `docs/api_examples.md`: Beispiele für API-Anfragen

## Status

Das Backend ist funktionsfähig und bildet die Grundlage für die spätere Android-App.