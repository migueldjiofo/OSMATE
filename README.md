# OSMATE

OSMATE ist ein agentenbasierter räumlicher Assistent für OpenStreetMap.

Das Projekt entwickelt eine Android-Anwendung, die natürlichsprachliche Suchanfragen in strukturierte räumliche Abfragen übersetzt und die Ergebnisse auf einer interaktiven Karte visualisiert.

## Ziel

Ziel ist die Entwicklung einer mobilen Geoanwendung, die den Zugang zu OpenStreetMap-Daten vereinfacht. Nutzerinnen und Nutzer sollen räumliche Fragen in natürlicher Sprache stellen können, ohne Overpass QL direkt schreiben zu müssen.

## Geplante Architektur

- Android-App mit Kotlin, Jetpack Compose und MapLibre
- Backend mit Python, FastAPI und Docker
- Zugriff auf OpenStreetMap-Daten über Overpass API
- Geokodierung über Nominatim
- Regelbasierter Agent als stabile erste Version
- Erweiterbare Architektur für spätere LLM-Integration

## Projektstruktur

```text
OSMATE
├── backend
├── android-app
├── docs
├── scripts
├── .gitattributes
├── .gitignore
└── README.md
```

## Status

Das Projekt wird aktuell auf einer neuen, professionell strukturierten Basis neu aufgebaut.
