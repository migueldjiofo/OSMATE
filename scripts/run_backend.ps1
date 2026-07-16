$ErrorActionPreference = "Stop"

# Backend-Umgebung vorbereiten
# Préparer l'environnement backend
& "$PSScriptRoot\setup_backend.ps1"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $ProjectRoot "backend"
$PythonExe = Join-Path $BackendDir ".venv\Scripts\python.exe"

Set-Location $BackendDir

# Lokalen Entwicklungsserver starten
# Démarrer le serveur local de développement
& $PythonExe -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
