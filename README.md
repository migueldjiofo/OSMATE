# OSMATE

**OSMATE – Ein agentischer räumlicher Assistent für OpenStreetMap**

OSMATE ist eine mobile Geoanwendung, die natürlichsprachliche Suchanfragen in räumliche Abfragen übersetzt und passende Orte auf Grundlage von OpenStreetMap-Daten findet, visualisiert und für die Routenplanung nutzbar macht.

Das Projekt wird im Rahmen des Moduls **Mobile Geoanwendungen** entwickelt.

---

## Projektziel

OSMATE soll Nutzerinnen und Nutzern ermöglichen, räumliche Suchanfragen in natürlicher Sprache zu formulieren, zum Beispiel:

- „Finde Cafés mit Terrasse in meiner Nähe.“
- „Zeige Supermärkte im Umkreis von zwei Kilometern.“
- „Finde einen Parkplatz in der Nähe.“
- „Berechne eine Route zum ausgewählten Ort.“

Die Anwendung verarbeitet die Anfrage, erstellt daraus einen strukturierten Suchplan und ruft relevante Geodaten über OpenStreetMap-Dienste ab.

---

## Aktueller Entwicklungsstand

### Backend

Das Backend basiert auf **FastAPI** und umfasst derzeit:

- Verarbeitung natürlichsprachlicher Suchanfragen
- agentische Analyse der Suchintention
- Erstellung strukturierter Suchpläne
- Generierung von Overpass-Abfragen
- Kommunikation mit der Overpass API
- Kommunikation mit dem Nominatim-Dienst
- Umwandlung der Ergebnisse in GeoJSON
- räumliche Suche nach Points of Interest
- Caching von Suchergebnissen
- Validierung und Selbstkorrektur von Anfragen
- Routing-Unterstützung
- Health-Check-Endpunkt
- automatisierte Tests

### Android-Anwendung

Die Android-Anwendung enthält derzeit:

- moderne Suchoberfläche
- Kartenansicht mit MapLibre
- OpenStreetMap-Kartenstile
- Standard- und Humanitarian-Kartenstil
- Ergebnisdarstellung auf der Karte
- Clusterbildung für nahe Suchergebnisse
- Darstellung der Anzahl von Ergebnissen in Clustern
- Premium-Kartenmarker für einzelne Suchergebnisse
- hervorgehobenen ausgewählten Ort mit Halo
- Darstellung der aktuellen Nutzerposition
- Routenvisualisierung mit kontrastreichem Außenrand
- Zoom-Steuerung
- optionaler 3D-Kartenmodus
- Bottom Sheet mit Ergebnis-, Routen- und Agentenansicht
- automatische Zentrierung auf Suchergebnisse und ausgewählte Orte

---

## Technologie-Stack

### Backend

- Python 3.11
- FastAPI
- Uvicorn
- Pydantic
- HTTPX
- Pytest
- Overpass API
- Nominatim
- GeoJSON
- Docker

### Android

- Kotlin
- Jetpack Compose
- Android SDK
- MapLibre Native
- Material Design 3
- Gradle

### Daten und Dienste

- OpenStreetMap
- Overpass API
- Nominatim
- GeoJSON

---

## Projektstruktur

```text
OSMATE/
├── android-app/
│   ├── app/
│   │   └── src/main/java/com/osmate/app/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── backend/
│   ├── app/
│   │   ├── agent/
│   │   ├── core/
│   │   ├── routers/
│   │   └── services/
│   ├── tests/
│   ├── Dockerfile
│   └── requirements.txt
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## Architektur

OSMATE verwendet eine Client-Server-Architektur.

```text
Android-Anwendung
        |
        | HTTP / JSON
        v
FastAPI-Backend
        |
        ├── Agentische Anfrageanalyse
        ├── Suchplanung und Validierung
        ├── Overpass-Abfrage
        ├── Nominatim-Abfrage
        ├── GeoJSON-Verarbeitung
        └── Routing
        |
        v
OpenStreetMap-Dienste
```

Die Android-Anwendung übernimmt die Benutzerinteraktion und Visualisierung. Das Backend verarbeitet die Suchanfragen, kommuniziert mit externen Geodatendiensten und liefert strukturierte Ergebnisse zurück.

---

## Agentischer Ablauf

Eine Anfrage wird vereinfacht in folgenden Schritten verarbeitet:

1. Eingabe einer natürlichsprachlichen Suchanfrage
2. Erkennung der Suchintention
3. Extraktion räumlicher und thematischer Parameter
4. Erstellung eines strukturierten Agentenplans
5. Validierung des Plans
6. Generierung einer Overpass-Abfrage
7. Abruf der OpenStreetMap-Daten
8. Umwandlung in GeoJSON
9. Darstellung der Ergebnisse in der Android-Anwendung
10. optionale Auswahl und Routenberechnung

---

## Backend lokal starten

```powershell
cd "C:\Users\migue\Projects\OSMATE\backend"
.\.venv\Scripts\Activate.ps1
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

API-Dokumentation:

```text
http://127.0.0.1:8000/docs
```

---

## Tests ausführen

```powershell
cd "C:\Users\migue\Projects\OSMATE\backend"
.\.venv\Scripts\Activate.ps1
pytest
```

Alternativ:

```powershell
.\run_tests.ps1
```

---

## Android-Anwendung kompilieren

```powershell
cd "C:\Users\migue\Projects\OSMATE\android-app"
.\gradlew.bat clean assembleDebug
```

Die Debug-APK befindet sich normalerweise unter:

```text
android-app\app\build\outputs\apk\debug\app-debug.apk
```

---

## Aktuelle Kartenfunktionen

- Rasterkarten auf Basis von OpenStreetMap
- alternativer Humanitarian-Kartenstil
- GeoJSON-Suchergebnisse
- Cluster für nahe Ergebnisse
- Clusterzähler
- Premium-Kartenmarker
- hervorgehobener ausgewählter Ort mit Halo
- Nutzerstandort
- Routenlinie mit weißem Außenrand
- Kameraanimationen
- automatische Zentrierung
- Zoom-Steuerung
- 3D-Neigung der Kamera

---

## Geplante Weiterentwicklung

- kategoriebasierte Marker und Symbole
- interaktive Ergebnis-Popups
- intelligentes Kamera-Fitting
- automatische Anpassung der Kamera an komplette Routen
- optimierte Kartenanimationen
- verbesserte Fehlerbehandlung bei Overpass-Ausfällen
- GPS-basierte Navigation
- Neuberechnung von Routen
- Offline-Funktionalität
- Erweiterung des agentischen Assistenten
- Erweiterung der Tests

---

## Datenschutz und Sicherheit

- Zugangsdaten und Umgebungsvariablen werden nicht im Repository gespeichert.
- Lokale `.env`-Dateien sind über `.gitignore` ausgeschlossen.
- Standortinformationen werden nur für die jeweilige Anfrage verwendet.
- Externe Geodatendienste werden über definierte Backend-Dienste angesprochen.

---

## Entwicklungsstatus

OSMATE befindet sich in aktiver Entwicklung.

Der aktuelle Schwerpunkt liegt auf:

1. Stabilisierung der Kartenkomponente
2. Verbesserung der Ergebnisvisualisierung
3. Weiterentwicklung der Routenfunktionen
4. Erweiterung des agentischen Suchassistenten
5. Vorbereitung der Projektdokumentation

---

## Autor

**Miguel Hernandez Djiofo Tchinda**

Projekt im Rahmen des Moduls:

**Mobile Geoanwendungen – Sommersemester 2026**

---

## Lizenz

Dieses Projekt wurde für akademische und demonstrative Zwecke entwickelt.

OpenStreetMap-Daten unterliegen der Open Database License der OpenStreetMap Foundation.
