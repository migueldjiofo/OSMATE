# Android-Anwendung

Die Android-Anwendung von OSMATE stellt die mobile Benutzeroberfläche des Projekts bereit. Sie ermöglicht räumliche Suchanfragen in natürlicher Sprache, sendet diese an das FastAPI-Backend und visualisiert die Ergebnisse aus OpenStreetMap auf einer interaktiven Karte.

## Ziel der Android-Komponente

Die mobile Anwendung verfolgt drei zentrale Ziele:

1. Eingabe natürlicher räumlicher Suchanfragen
2. Kommunikation mit dem OSMATE-Backend
3. Kartographische Visualisierung der Suchergebnisse

Beispielhafte Suchanfragen:

- Finde Cafes mit Terrasse
- Finde Spaetis in der Naehe
- Finde Spielplaetze mit Wasserspielen
- Finde Baeckereien in Berlin Mitte
- Finde Ladestationen in Berlin Alexanderplatz

## Technologiestack

| Bereich | Technologie |
|---|---|
| Sprache | Kotlin |
| Benutzeroberfläche | Jetpack Compose |
| Architektur | MVVM |
| Kartenbibliothek | MapLibre |
| Backend-Kommunikation | HTTP ueber HttpURLConnection |
| Datenformat | JSON und GeoJSON |
| Build-System | Gradle |

## Architektur

Die Anwendung ist nach einer einfachen MVVM-Struktur aufgebaut. MVVM steht für Model-View-ViewModel. Dadurch werden Benutzeroberfläche, Zustand und Datenzugriff voneinander getrennt.

Wichtige Bestandteile:

- MainActivity.kt: Einstiegspunkt der Android-App
- SearchScreen.kt: Hauptoberfläche der Suche
- SearchViewModel.kt: Verwaltung des UI-Zustands
- SearchRepository.kt: Schnittstelle zwischen ViewModel und API-Client
- OsmateApiClient.kt: HTTP-Kommunikation mit dem Backend
- OsmateMapView.kt: Kartendarstellung mit MapLibre
- SearchResultList.kt: Ergebnisliste
- SelectedResultDetail.kt: Detailansicht eines ausgewählten Ergebnisses
- SearchInputValidator.kt: Validierung der Eingaben
- ErrorMessageMapper.kt: Umwandlung technischer Fehler in verständliche Hinweise

## Datenfluss

Der Datenfluss der Anwendung ist wie folgt aufgebaut:

Benutzereingabe -> SearchScreen -> SearchViewModel -> SearchRepository -> OsmateApiClient -> FastAPI-Backend -> AgentPlan -> Overpass-Abfrage -> GeoJSON-Ergebnis -> Android-Karte und Ergebnisliste

## Backend-Anbindung

Die Android-App kommuniziert im Emulator mit dem lokalen Backend über folgende Basisadresse:

http://10.0.2.2:8000

Diese Adresse wird im Android-Emulator verwendet, um auf den lokalen Entwicklungsrechner zuzugreifen. Das Backend selbst läuft lokal auf:

http://127.0.0.1:8000

Wichtige Endpunkte:

| Endpunkt | Zweck |
|---|---|
| /api/v1/health | Prüfung, ob das Backend erreichbar ist |
| /api/v1/search | Ausführung der vollständigen Suche |
| /api/v1/search/plan | Erstellung eines AgentPlan |
| /api/v1/search/overpass-query | Erzeugung einer Overpass-Abfrage |

## Kartenvisualisierung

Die Kartenkomponente basiert auf MapLibre. Als Hintergrundkarte werden OpenStreetMap-Rasterkacheln verwendet.

Die App stellt zwei Arten von Markierungen dar:

| Darstellung | Bedeutung |
|---|---|
| Blaue Punkte | Suchergebnisse |
| Oranger Punkt | Ausgewähltes Ergebnis |

Nach einer neuen Suche wird die Karte auf die Ergebnisregion zentriert. Bei Auswahl eines Ergebnisses zoomt die Karte auf diesen Ort.

## Ergebnisliste

Die Suchergebnisse werden zusätzlich unterhalb der Karte als Liste angezeigt. Pro Ergebnis werden folgende Informationen dargestellt:

- Titel oder generierter Ersatzname
- Primärer OpenStreetMap-Tag
- Zusätzliche Tags
- Öffnungszeiten, falls verfügbar
- Koordinaten, falls verfügbar

Wenn ein Ergebnis ausgewählt wird, erscheint eine Detailkarte mit einem Link zu OpenStreetMap.

## Eingabevalidierung

Die Android-App validiert die Benutzereingaben vor dem Senden an das Backend.

| Feld | Regel |
|---|---|
| Suchanfrage | darf nicht leer sein |
| Ort | darf nicht leer sein |
| Radius | muss zwischen 100 und 5000 Metern liegen |

Ungültige Eingaben werden direkt in der App mit verständlichen Hinweisen angezeigt.

## Fehlerbehandlung

Technische Fehler werden in verständliche Meldungen übersetzt.

| Technischer Fall | Benutzerhinweis |
|---|---|
| Backend nicht erreichbar | Der Backend-Dienst ist nicht erreichbar |
| Timeout | Die Anfrage hat zu lange gedauert |
| Netzwerkproblem | Internetverbindung des Emulators prüfen |
| Overpass-Fehler | OpenStreetMap-Abfrage konnte nicht ausgeführt werden |
| Ungültige Suchanfrage | Suchanfrage einfacher formulieren |

## Schnellbeispiele

Die App enthält Schnellbeispiele, um die Demonstration zu vereinfachen:

- Cafés mit Terrasse
- Spätis
- Spielplätze

Diese Beispiele füllen Suchanfrage, Ort und Radius automatisch aus.

## Build

Die Debug-Version der Android-App kann mit folgendem Befehl gebaut werden:

cd C:\Users\migue\Projects\OSMATE\android-app
.\gradlew.bat clean :app:assembleDebug --no-configuration-cache --rerun-tasks

Die erzeugte APK liegt anschließend unter:

android-app/app/build/outputs/apk/debug/app-debug.apk

## Lokaler Testablauf

Für eine vollständige lokale Demonstration müssen Backend und Android-App gemeinsam ausgeführt werden.

1. Backend starten
2. Android-App im Emulator starten
3. In der App Backend prüfen auswählen
4. Schnellbeispiel auswählen
5. Suche ausführen
6. Karte, Liste und Detailansicht prüfen

Backend starten:

cd C:\Users\migue\Projects\OSMATE\backend
.\.venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000

## Aktueller Funktionsumfang

Die Android-App unterstützt aktuell:

- Eingabe natürlicher Suchanfragen
- Schnellbeispiele
- Validierung der Eingaben
- Verbindung zum lokalen FastAPI-Backend
- Darstellung von GeoJSON-Ergebnissen auf einer Karte
- Ergebnisliste
- Auswahl einzelner Ergebnisse
- Detailkarte für ausgewählte Ergebnisse
- Link zu OpenStreetMap
- Verständliche Fehlermeldungen

## Bekannte Einschränkungen

Die aktuelle Version ist ein funktionsfähiger Prototyp. Folgende Einschränkungen bestehen noch:

- Kein echtes GPS des Nutzers
- Keine Offline-Karten
- Keine Authentifizierung
- Keine produktive Serverbereitstellung
- Keine vollständige Mehrsprachigkeit
- Regelbasierter Agent statt vollständiger LLM-Integration
- Abhängigkeit von Overpass API und Nominatim
- Qualität der Ergebnisse hängt von OpenStreetMap-Daten ab

## Ausblick

Mögliche Erweiterungen sind:

- Integration des Gerätestandorts
- LLM-basierte Anfrageinterpretation
- Offline-Kartenmodus
- Erweiterte Filter
- Routenfunktion
- Sprachsuche
- Speicherung favorisierter Orte
- Release-Build mit Signierung