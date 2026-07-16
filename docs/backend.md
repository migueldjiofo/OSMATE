# Backend-Dokumentation

## Überblick

Das OSMATE-Backend ist eine FastAPI-Anwendung. Es verarbeitet natürlichsprachliche Suchanfragen und erzeugt daraus strukturierte räumliche Suchanfragen für OpenStreetMap (OSM).

Der aktuelle Backend-Ablauf lautet:

```text
SearchRequest
→ Spatial Resolver
→ Agent Planner
→ AgentPlan
→ Overpass Builder
→ Overpass Client
→ GeoJSON Service
→ SearchResponse
```

## Hauptmodule

### `app.agent`

Dieses Modul enthält die agentische Planungslogik.

Wichtige Dateien:

- `planner.py`: erstellt den strukturierten AgentPlan
- `tag_catalog.py`: enthält unterstützte OSM-Kategorien und optionale Tag-Regeln
- `validator.py`: validiert den AgentPlan vor der Weiterverarbeitung

Der aktuelle Agent ist bewusst regelbasiert. Dadurch bleibt die erste Version stabil, testbar, reproduzierbar und unabhängig von externen Large-Language-Model-Diensten.

### `app.api`

Dieses Modul stellt die HTTP-Endpunkte bereit.

Wichtige Dateien:

- `routes/health.py`: Health Check
- `routes/search.py`: Suchrouten für Planung, Overpass-QL-Erzeugung und vollständige Suche

### `app.core`

Dieses Modul enthält zentrale Konfiguration.

Wichtige Datei:

- `config.py`: liest Einstellungen aus Umgebungsvariablen und `.env`

### `app.schemas`

Dieses Modul enthält Pydantic-Schemas.

Wichtige Dateien:

- `search_request.py`: Eingabemodell für Suchanfragen
- `agent_plan.py`: strukturierter Agentenplan
- `overpass_query.py`: Antwortmodell für Overpass-QL-Erzeugung
- `search_response.py`: Antwortmodell für vollständige Suchausführung
- `error_response.py`: standardisiertes Fehlermodell

### `app.services`

Dieses Modul enthält externe Dienste und technische Verarbeitung.

Wichtige Dateien:

- `overpass_builder.py`: erzeugt Overpass QL aus einem AgentPlan
- `overpass_client.py`: sendet Overpass-Anfragen
- `nominatim_client.py`: geokodiert Ortsnamen
- `spatial_resolver.py`: stellt Koordinaten für Suchanfragen bereit
- `geojson_service.py`: konvertiert Overpass-Antworten in GeoJSON
- `cache_service.py`: speichert Ergebnisse in einem SQLite-Cache

## API-Endpunkte

### `GET /api/v1/health`

Prüft, ob das Backend erreichbar ist.

### `POST /api/v1/search/plan`

Erstellt nur einen AgentPlan.

### `POST /api/v1/search/overpass-query`

Erstellt einen AgentPlan und daraus eine Overpass-QL-Abfrage.

### `POST /api/v1/search`

Führt die vollständige Suche aus:

```text
AgentPlan
→ Overpass QL
→ Overpass API
→ GeoJSON
```

## Fehlerbehandlung

Das Backend unterscheidet zwischen Planungsfehlern und externen Dienstfehlern.

Planungsfehler:

```text
422 Unprocessable Content
```

Beispiele:

- unbekannte Kategorie
- fehlende Koordinaten
- fehlender Ortsname
- zu großer Suchradius

Externe Dienstfehler:

```text
502 Bad Gateway
```

Beispiele:

- Overpass API nicht erreichbar
- Nominatim API nicht erreichbar
- Timeout
- Rate Limit

## Cache

Das Backend verwendet einen SQLite-Cache für externe Antworten.

Standardpfad lokal:

```text
backend/data/cache/osmate_cache.sqlite3
```

Dieser Pfad wird nicht versioniert und ist in `.gitignore` ausgeschlossen.

## Qualitätssicherung

Tests:

```powershell
.\scripts\run_backend_tests.ps1
```

Ruff:

```powershell
.\backend\.venv\Scripts\python.exe -m ruff check .\backend\app
```

## Aktuelle Grenze

Der Agent ist aktuell regelbasiert. Eine spätere Integration eines Large Language Model (LLM) ist architektonisch vorbereitet, aber noch nicht implementiert.