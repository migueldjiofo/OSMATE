# OSMATE - Ein agentenbasierter raeumlicher Assistent fuer OpenStreetMap

**Name:** Miguel Djiofo  
**Matrikelnummer:** 880634  
**Modul:** Mobile Geoanwendungen (SS 2026)  
**Projekt:** OSMATE  

## Kurzbeschreibung

OSMATE ist eine Android-Anwendung zur sprachbasierten Suche nach raeumlichen Objekten aus OpenStreetMap. Nutzerinnen und Nutzer koennen Suchanfragen in natuerlicher Sprache formulieren, zum Beispiel "Finde Cafes mit Terrasse" oder "Finde Apotheken in Berlin-Mitte". Die Anfrage wird durch ein FastAPI-Backend verarbeitet, in einen strukturierten AgentPlan uebersetzt und anschliessend in eine Overpass-QL-Abfrage fuer OpenStreetMap ueberfuehrt.

Die Ergebnisse werden als GeoJSON zurueckgegeben und in der Android-App mit MapLibre auf einer interaktiven Karte visualisiert. Zusaetzlich werden erkannte Kategorien, OpenStreetMap-Tags, Trefferanzahl, Suchradius und Hinweise des regelbasierten Agenten angezeigt.

## Technische Komponenten

- Android-App mit Kotlin und Jetpack Compose
- Kartenvisualisierung mit MapLibre
- Backend mit Python 3.11 und FastAPI
- OpenStreetMap-Daten ueber Overpass API
- Ortsaufloesung ueber Nominatim
- GeoJSON-Ausgabe
- Routing-Erweiterung ueber OSRM beziehungsweise direkten Fallback
- Tests mit pytest
- Docker-Setup fuer den Backend-Service
- Optionale Erweiterungsstruktur fuer Gemini und LangGraph

## Agentenlogik

Die aktuelle Version verwendet einen stabilen regelbasierten Agenten. Dieser erkennt unterstuetzte Kategorien wie Cafe, Spaeti, Apotheke, Baeckerei, Restaurant, Spielplatz, Supermarkt und weitere OpenStreetMap-Kategorien. Zusaetzlich werden optionale Tags wie Terrasse, WLAN, Barrierefreiheit oder veganes Angebot erkannt.

Die Architektur ist fuer eine spaetere Erweiterung mit Gemini beziehungsweise LangGraph vorbereitet. Damit kann der bestehende regelbasierte Agent kuenftig durch einen LLM-basierten oder hybriden Agenten ergaenzt werden.

## Funktionsumfang

- Backend-Health-Check aus der Android-App
- Suche nach OpenStreetMap-Objekten ueber natuerliche Sprache
- Uebersetzung der Anfrage in AgentPlan und Overpass-QL
- Validierung und einfache Self-Correction der Overpass-QL-Abfrage
- Anzeige der Ergebnisse auf einer MapLibre-Karte
- Ergebnisliste mit Detailinformationen
- Auswahl eines Kartenobjekts
- Routing-Grundlage mit Distanz- und Dauerberechnung
- Dockerisierter Backend-Service
- Automatisierte Backend-Tests

## Bekannte Einschraenkungen

Die Suche ueber einen manuell eingegebenen Ort ist stabil. Die GPS-basierte Suche ueber "Meine Position" wurde begonnen, ist aber noch nicht vollstaendig finalisiert. Die LLM-Integration mit Gemini und LangGraph ist als optionale Erweiterungsarchitektur vorbereitet und nicht zwingend fuer die stabile MVP-Demonstration erforderlich.

## GitHub-Link

https://github.com/migueldjiofo/OSMATE

## Ausfuehrbarer Code

Die ausfuehrbare Android-Datei befindet sich im Abgabeordner:

submission/OSMATE-debug.apk

Das Backend kann lokal mit folgendem Befehl gestartet werden:

cd backend
.\.venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000