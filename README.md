# OSMATE

OSMATE ist ein agentenbasierter räumlicher Assistent für OpenStreetMap. Das Projekt kombiniert eine Android-Anwendung, ein FastAPI-Backend und OpenStreetMap-Daten, um räumliche Suchanfragen in natürlicher Sprache in strukturierte Overpass-Abfragen zu übersetzen.

Das Projekt wurde im Kontext des Moduls Mobile Geoanwendungen entwickelt.

## Projektidee

Viele räumliche Daten in OpenStreetMap sind über die Overpass API zugänglich. Die direkte Nutzung von Overpass QL ist jedoch für viele Nutzerinnen und Nutzer technisch anspruchsvoll. OSMATE reduziert diese Einstiegshürde, indem natürliche Sprache in einen strukturierten Suchplan übersetzt wird.

Beispiele:

- Finde Cafes mit Terrasse
- Finde Spaetis in der Naehe
- Finde Spielplaetze mit Wasserspielen
- Finde Baeckereien in Berlin Mitte
- Finde Ladestationen in Berlin Alexanderplatz

Die Anwendung erzeugt daraus einen AgentPlan, baut eine Overpass-Abfrage, ruft OpenStreetMap-Daten ab und visualisiert die Ergebnisse auf einer mobilen Karte.

## Aktueller Funktionsumfang

OSMATE unterstützt aktuell:

- Android-App mit Jetpack Compose
- Eingabe natürlicher räumlicher Suchanfragen
- Schnellbeispiele für die Demonstration
- Validierung der Eingaben
- FastAPI-Backend
- regelbasierte AgentPlan-Erzeugung
- Overpass-Abfragegenerierung
- Overpass-Ausführung
- GeoJSON-Konvertierung
- Kartendarstellung mit MapLibre
- Ergebnisliste
- Auswahl einzelner Ergebnisse
- Detailkarte mit OpenStreetMap-Link
- Verständliche Fehlermeldungen
- Backend-Tests mit pytest
- Android-Unit-Tests für Validierung und Fehlermeldungen
- Docker-Konfiguration für das Backend

## Architekturübersicht

Der zentrale Datenfluss sieht wie folgt aus:

Benutzereingabe -> Android-App -> FastAPI-Backend -> AgentPlan -> Overpass QL -> Overpass API -> GeoJSON -> Android-Karte

Die Anwendung besteht aus zwei Hauptteilen:

| Komponente | Beschreibung |
|---|---|
| Android-App | Mobile Benutzeroberfläche, Karte, Ergebnisliste und Eingabevalidierung |
| Backend | Agentische Suchplanung, Overpass-Abfrage und GeoJSON-Konvertierung |

## Backend

Das Backend basiert auf Python 3.11 und FastAPI.

Wichtige Aufgaben des Backends:

- Annahme natürlicher Suchanfragen
- Erstellung eines AgentPlan
- Erkennung unterstützter OpenStreetMap-Kategorien
- Erkennung optionaler Tags wie Terrasse, WLAN oder Barrierefreiheit
- Validierung des AgentPlan
- Erzeugung von Overpass QL
- Ausführung der Overpass-Abfrage
- Konvertierung der Ergebnisse in GeoJSON

### Unterstützte Kategorien

Der regelbasierte Agent unterstützt aktuell unter anderem:

- cafe
- spaeti
- restaurant
- supermarket
- playground
- park
- pharmacy
- atm
- bakery
- library
- hospital
- school
- charging_station
- bus_stop

### Wichtige Backend-Endpunkte

| Endpunkt | Zweck |
|---|---|
| GET /api/v1/health | Backend-Status prüfen |
| POST /api/v1/search/plan | AgentPlan erzeugen |
| POST /api/v1/search/overpass-query | Overpass-Abfrage erzeugen |
| POST /api/v1/search | Vollständige Suche ausführen |

### Backend lokal starten

cd C:\Users\migue\Projects\OSMATE\backend
.\.venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000

Danach ist das Backend erreichbar unter:

http://127.0.0.1:8000

Die API-Dokumentation ist erreichbar unter:

http://127.0.0.1:8000/docs

### Backend-Tests

cd C:\Users\migue\Projects\OSMATE\backend
.\.venv\Scripts\Activate.ps1
python -m pytest

Erwartetes Ergebnis:

23 passed

Ein StarletteDeprecationWarning kann aktuell erscheinen und ist nicht blockierend.

## Android-App

Die Android-App bildet die mobile Benutzeroberfläche von OSMATE.

Technologien:

| Bereich | Technologie |
|---|---|
| Sprache | Kotlin |
| Benutzeroberfläche | Jetpack Compose |
| Architektur | MVVM |
| Karte | MapLibre |
| Datenformat | JSON und GeoJSON |
| Build-System | Gradle |

Im Android-Emulator greift die App auf das lokale Backend über folgende Adresse zu:

http://10.0.2.2:8000

### Android-App bauen

cd C:\Users\migue\Projects\OSMATE\android-app
.\gradlew.bat clean :app:assembleDebug --no-configuration-cache --rerun-tasks

Die erzeugte Debug-APK liegt unter:

android-app/app/build/outputs/apk/debug/app-debug.apk

### Android-Tests

cd C:\Users\migue\Projects\OSMATE\android-app
.\gradlew.bat :app:testDebugUnitTest --no-configuration-cache --rerun-tasks

## Docker

Das Backend kann auch über Docker Compose gestartet werden.

cd C:\Users\migue\Projects\OSMATE
docker compose up --build

Danach ist das Backend unter folgender Adresse erreichbar:

http://127.0.0.1:8000

## Projektstruktur

Wichtige Projektbereiche:

| Pfad | Beschreibung |
|---|---|
| backend | FastAPI-Backend |
| android-app | Android-Anwendung |
| docs | Technische Dokumentation |
| scripts | Hilfsskripte |
| docker-compose.yml | Docker-Compose-Konfiguration |

## Dokumentation

Weitere technische Dokumentation befindet sich im Ordner docs.

| Datei | Inhalt |
|---|---|
| docs/backend.md | Backend-Architektur und API |
| docs/docker.md | Docker-Nutzung |
| docs/api_examples.md | API-Beispiele |
| docs/android.md | Android-App, Datenfluss und Build |

## Lokaler Demo-Ablauf

1. Backend starten
2. Android-Emulator starten
3. Android-App öffnen
4. In der App Backend prüfen auswählen
5. Schnellbeispiel auswählen
6. Suche ausführen
7. Karte, Ergebnisliste und Detailansicht demonstrieren

Empfohlene Demo-Suchanfragen:

- Finde Cafes mit Terrasse, Ort: Berlin Alexanderplatz, Radius: 1000
- Finde Spaetis in der Naehe, Ort: Berlin Kreuzberg, Radius: 1200
- Finde Spielplaetze mit Wasserspielen, Ort: Berlin Mitte, Radius: 1500
- Finde Baeckereien, Ort: Berlin Mitte, Radius: 1000
- Finde Ladestationen, Ort: Berlin Alexanderplatz, Radius: 1000

## Agentische Komponente

Der aktuelle Agent ist regelbasiert. Er analysiert die natürliche Anfrage, erkennt eine unterstützte Kategorie, ergänzt optionale OpenStreetMap-Tags und erzeugt daraus einen strukturierten AgentPlan.

Der AgentPlan enthält unter anderem:

- Intent
- Kategorie
- OpenStreetMap-Tags
- räumlichen Filter
- Radius
- Konfidenzwert
- Warnhinweise
- Empfehlung für möglichen LLM-Fallback

Diese Struktur ermöglicht später eine Erweiterung durch ein LLM oder LangGraph, ohne die gesamte Backend-Architektur neu aufzubauen.

## Bekannte Einschränkungen

Die aktuelle Version ist ein funktionsfähiger Prototyp. Folgende Einschränkungen bestehen noch:

- Regelbasierter Agent statt vollständiger LLM-Integration
- Keine produktive Serverbereitstellung
- Kein echtes GPS des Android-Geräts
- Keine Offline-Karten
- Keine Authentifizierung
- Keine vollständige Mehrsprachigkeit
- Abhängigkeit von Overpass API
- Abhängigkeit von Nominatim für Geocoding
- Qualität der Ergebnisse hängt von OpenStreetMap-Daten ab

## Ausblick

Mögliche Erweiterungen:

- Integration des Gerätestandorts
- LLM-basierte Anfrageinterpretation
- LangGraph-basierte Agentenorchestrierung
- RAG über OpenStreetMap-Wiki
- Offline-Kartenmodus
- Sprachsuche
- Favoritenfunktion
- Routenfunktion
- Release-Build mit Signierung
- Deployment des Backends auf einem Server

## Status

Der aktuelle Stand umfasst ein lauffähiges Backend, eine funktionsfähige Android-App, Tests, Docker-Unterstützung und technische Dokumentation.

Das Projekt ist damit als demonstrierbarer Prototyp für mobile räumliche Suche mit OpenStreetMap nutzbar.