param(
    [string]$HostAddress = "127.0.0.1",
    [int]$Port = 8000
)

$ErrorActionPreference = "Stop"

# Projektpfade werden relativ zum Skriptordner bestimmt.
$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptRoot
$BackendDirectory = Join-Path $ProjectRoot "backend"
$VirtualEnvironmentScript = Join-Path $BackendDirectory ".venv\Scripts\Activate.ps1"

if (-not (Test-Path $BackendDirectory)) {
    throw "Backend-Verzeichnis wurde nicht gefunden: $BackendDirectory"
}

if (-not (Test-Path $VirtualEnvironmentScript)) {
    throw "Virtuelle Python-Umgebung wurde nicht gefunden: $VirtualEnvironmentScript"
}

Set-Location $BackendDirectory

# Virtuelle Umgebung aktivieren.
. $VirtualEnvironmentScript

Write-Host ""
Write-Host "OSMATE Backend wird gestartet..." -ForegroundColor Green
Write-Host "Adresse: http://$HostAddress`:$Port" -ForegroundColor Cyan
Write-Host "API-Dokumentation: http://$HostAddress`:$Port/docs" -ForegroundColor Cyan
Write-Host ""

python -m uvicorn app.main:app --reload --host $HostAddress --port $Port