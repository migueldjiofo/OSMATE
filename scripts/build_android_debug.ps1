$ErrorActionPreference = "Stop"

# Projektpfade werden relativ zum Skriptordner bestimmt.
$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptRoot
$AndroidDirectory = Join-Path $ProjectRoot "android-app"
$GradleWrapper = Join-Path $AndroidDirectory "gradlew.bat"
$ApkPath = Join-Path $AndroidDirectory "app\build\outputs\apk\debug\app-debug.apk"

if (-not (Test-Path $AndroidDirectory)) {
    throw "Android-Verzeichnis wurde nicht gefunden: $AndroidDirectory"
}

if (-not (Test-Path $GradleWrapper)) {
    throw "Gradle Wrapper wurde nicht gefunden: $GradleWrapper"
}

Set-Location $AndroidDirectory

Write-Host ""
Write-Host "Android Debug-APK wird gebaut..." -ForegroundColor Green
Write-Host ""

& $GradleWrapper clean :app:assembleDebug --no-configuration-cache --rerun-tasks

if (-not (Test-Path $ApkPath)) {
    throw "APK wurde nicht erzeugt: $ApkPath"
}

Write-Host ""
Write-Host "Build erfolgreich." -ForegroundColor Green
Write-Host "APK: $ApkPath" -ForegroundColor Cyan
Write-Host ""