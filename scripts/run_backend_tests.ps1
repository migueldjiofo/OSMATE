$ErrorActionPreference = "Stop"

# Backend-Umgebung vorbereiten
# Préparer l'environnement backend
& "$PSScriptRoot\setup_backend.ps1"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $ProjectRoot "backend"
$PythonExe = Join-Path $BackendDir ".venv\Scripts\python.exe"

Set-Location $BackendDir

# Automatisierte Tests ausfuehren
# Exécuter les tests automatisés
& $PythonExe -m pytest
