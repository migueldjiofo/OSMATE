# API-Beispiele

## Swagger UI

Die interaktive API-Dokumentation ist erreichbar unter:

```text
http://127.0.0.1:8000/docs
```

## Health Check

### Request

```http
GET /api/v1/health
```

### Response

```json
{
  "status": "ok",
  "service": "OSMATE Backend",
  "environment": "development"
}
```

## AgentPlan mit Koordinaten erzeugen

### Request

```http
POST /api/v1/search/plan
```

```json
{
  "query": "Finde Cafes mit Terrasse in meiner Naehe",
  "lat": 52.52,
  "lon": 13.405,
  "radius_m": 1000,
  "limit": 10,
  "language": "de"
}
```

### Erwartete Kernaussage

```json
{
  "intent": "poi_search",
  "category": "cafe",
  "osm_tags": {
    "amenity": "cafe",
    "outdoor_seating": "yes"
  },
  "planner_type": "rule_based"
}
```

## AgentPlan mit Ortsnamen erzeugen

### Request

```http
POST /api/v1/search/plan
```

```json
{
  "query": "Finde Cafes mit Terrasse",
  "place_name": "Berlin Alexanderplatz",
  "radius_m": 1000,
  "limit": 10,
  "language": "de"
}
```

Das Backend geokodiert den Ortsnamen über Nominatim und ergänzt anschließend die Koordinaten im AgentPlan.

## Overpass QL erzeugen

### Request

```http
POST /api/v1/search/overpass-query
```

```json
{
  "query": "Finde Cafes mit Terrasse in meiner Naehe",
  "lat": 52.52,
  "lon": 13.405,
  "radius_m": 1000,
  "limit": 10,
  "language": "de"
}
```

### Erwartete Overpass-QL-Struktur

```text
[out:json][timeout:25];
(
  node["amenity"="cafe"]["outdoor_seating"="yes"](around:1000,52.520000,13.405000);
  way["amenity"="cafe"]["outdoor_seating"="yes"](around:1000,52.520000,13.405000);
  relation["amenity"="cafe"]["outdoor_seating"="yes"](around:1000,52.520000,13.405000);
);
out center 10;
```

## Vollständige Suche ausführen

### Request

```http
POST /api/v1/search
```

```json
{
  "query": "Finde Cafes mit Terrasse in meiner Naehe",
  "lat": 52.52,
  "lon": 13.405,
  "radius_m": 1000,
  "limit": 10,
  "language": "de"
}
```

### Response-Struktur

```json
{
  "original_query": "Finde Cafes mit Terrasse in meiner Naehe",
  "agent_plan": {},
  "overpass_ql": "...",
  "geojson": {
    "type": "FeatureCollection",
    "features": []
  },
  "result_count": 0,
  "warnings": []
}
```

## Fehlerbeispiel: unbekannte Kategorie

### Request

```json
{
  "query": "Finde schoene Dinge in meiner Naehe",
  "lat": 52.52,
  "lon": 13.405,
  "radius_m": 1000,
  "limit": 10,
  "language": "de"
}
```

### Response

```json
{
  "code": "planning_error",
  "message": "Die Anfrage konnte keiner unterstuetzten OSM-Kategorie zugeordnet werden."
}
```