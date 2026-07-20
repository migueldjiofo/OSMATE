package com.osmate.app.ui.error

object ErrorMessageMapper {
    fun fromThrowable(error: Throwable): String {
        val rawMessage = error.message.orEmpty()
        val message = rawMessage.lowercase()

        return when {
            message.contains("failed to connect") ||
                message.contains("connection refused") ||
                message.contains("connectexception") -> {
                "Der Backend-Dienst ist nicht erreichbar. Bitte starte den FastAPI-Server und prüfe http://10.0.2.2:8000."
            }

            message.contains("timeout") ||
                message.contains("timed out") ||
                message.contains("sockettimeoutexception") -> {
                "Die Anfrage hat zu lange gedauert. Bitte versuche es erneut oder reduziere den Radius."
            }

            message.contains("unable to resolve host") ||
                message.contains("unknownhostexception") -> {
                "Es besteht ein Netzwerkproblem. Bitte prüfe die Internetverbindung des Emulators."
            }

            message.contains("http 422") ||
                message.contains("planning_error") -> {
                "Die Suchanfrage konnte nicht in einen gültigen Suchplan übersetzt werden. Bitte formuliere sie einfacher."
            }

            message.contains("geocoding") ||
                message.contains("nominatim") -> {
                "Der eingegebene Ort konnte nicht zuverlässig gefunden werden. Bitte verwende einen genaueren Ortsnamen."
            }

            message.contains("overpass") ||
                message.contains("http 502") ||
                message.contains("http 504") -> {
                "Die OpenStreetMap-Abfrage konnte momentan nicht ausgeführt werden. Bitte versuche es später erneut oder reduziere den Radius."
            }

            message.contains("http 429") ||
                message.contains("rate") -> {
                "Es wurden zu viele Anfragen gestellt. Bitte warte kurz und versuche es erneut."
            }

            rawMessage.isBlank() -> {
                "Es ist ein unbekannter Fehler aufgetreten. Bitte versuche es erneut."
            }

            else -> {
                "Die Anfrage konnte nicht verarbeitet werden. Bitte prüfe Suchanfrage, Ort und Radius."
            }
        }
    }
}