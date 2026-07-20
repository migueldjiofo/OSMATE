package com.osmate.app.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchInputValidatorTest {
    @Test
    fun validInputReturnsNormalizedValues() {
        val result = SearchInputValidator.validate(
            query = "  Finde Cafes mit Terrasse  ",
            placeName = "  Berlin Alexanderplatz  ",
            radiusText = "1000",
        )

        assertEquals("Finde Cafes mit Terrasse", result.query)
        assertEquals("Berlin Alexanderplatz", result.placeName)
        assertEquals(1000, result.radiusM)
        assertNull(result.errorMessage)
    }

    @Test
    fun emptyQueryReturnsError() {
        val result = SearchInputValidator.validate(
            query = "   ",
            placeName = "Berlin",
            radiusText = "1000",
        )

        assertEquals("Bitte gib eine Suchanfrage ein.", result.errorMessage)
        assertNull(result.radiusM)
    }

    @Test
    fun emptyPlaceNameReturnsError() {
        val result = SearchInputValidator.validate(
            query = "Finde Cafes",
            placeName = "   ",
            radiusText = "1000",
        )

        assertEquals("Bitte gib einen Ort ein.", result.errorMessage)
        assertNull(result.radiusM)
    }

    @Test
    fun radiusBelowMinimumReturnsError() {
        val result = SearchInputValidator.validate(
            query = "Finde Cafes",
            placeName = "Berlin",
            radiusText = "20",
        )

        assertEquals("Der Radius muss zwischen 100 und 5000 Metern liegen.", result.errorMessage)
        assertNull(result.radiusM)
    }

    @Test
    fun radiusAboveMaximumReturnsError() {
        val result = SearchInputValidator.validate(
            query = "Finde Cafes",
            placeName = "Berlin",
            radiusText = "7000",
        )

        assertEquals("Der Radius muss zwischen 100 und 5000 Metern liegen.", result.errorMessage)
        assertNull(result.radiusM)
    }

    @Test
    fun nonNumericRadiusReturnsError() {
        val result = SearchInputValidator.validate(
            query = "Finde Cafes",
            placeName = "Berlin",
            radiusText = "abc",
        )

        assertTrue(result.errorMessage!!.contains("Radius"))
        assertNull(result.radiusM)
    }
}