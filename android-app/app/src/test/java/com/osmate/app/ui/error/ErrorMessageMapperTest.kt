package com.osmate.app.ui.error

import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMessageMapperTest {
    @Test
    fun connectionErrorReturnsBackendMessage() {
        val message = ErrorMessageMapper.fromThrowable(
            RuntimeException("Connection refused"),
        )

        assertTrue(message.contains("Backend-Dienst"))
    }

    @Test
    fun timeoutErrorReturnsTimeoutMessage() {
        val message = ErrorMessageMapper.fromThrowable(
            RuntimeException("SocketTimeoutException"),
        )

        assertTrue(message.contains("zu lange gedauert"))
    }

    @Test
    fun unknownHostErrorReturnsNetworkMessage() {
        val message = ErrorMessageMapper.fromThrowable(
            RuntimeException("UnknownHostException"),
        )

        assertTrue(message.contains("Netzwerkproblem"))
    }

    @Test
    fun planningErrorReturnsSearchPlanMessage() {
        val message = ErrorMessageMapper.fromThrowable(
            RuntimeException("HTTP 422 planning_error"),
        )

        assertTrue(message.contains("Suchplan"))
    }

    @Test
    fun overpassErrorReturnsOpenStreetMapMessage() {
        val message = ErrorMessageMapper.fromThrowable(
            RuntimeException("HTTP 502 overpass_error"),
        )

        assertTrue(message.contains("OpenStreetMap-Abfrage"))
    }

    @Test
    fun rateLimitErrorReturnsRateLimitMessage() {
        val message = ErrorMessageMapper.fromThrowable(
            RuntimeException("HTTP 429 rate limit"),
        )

        assertTrue(message.contains("zu viele Anfragen"))
    }

    @Test
    fun emptyErrorReturnsUnknownErrorMessage() {
        val message = ErrorMessageMapper.fromThrowable(
            RuntimeException(""),
        )

        assertTrue(message.contains("unbekannter Fehler"))
    }
}