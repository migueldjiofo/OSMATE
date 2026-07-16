$ErrorActionPreference = "Stop"

# Projektpfade vorbereiten
# Préparer les chemins du projet
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $ProjectRoot "backend"
$VenvDir = Join-Path $BackendDir ".venv"
$PythonExe = Join-Path $VenvDir "Scripts\python.exe"

Set-Location $BackendDir

# Virtuelle Python-Umgebung erstellen, falls sie noch nicht existiert
# Créer l'environnement virtuel Python s'il n'existe pas encore
if (-Not (Test-Path $PythonExe)) {
    if (Get-Command py -ErrorAction SilentlyContinue) {
        py -3.11 -m venv $VenvDir
    }
    else {
        python -m venv $VenvDir
    }
}

# Abhaengigkeiten installieren
# Installer les dépendances
& $PythonExe -m pip install --upgrade pip
& $PythonExe -m pip install -r requirements.txt
