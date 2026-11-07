package com.example.project_auth.domain

import com.example.project_auth.data.QuoteSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySyncTest {

    private val day = 86_400L

    private fun entry(dayNumber: Int, price: Double) =
        QuoteSnapshot(symbol = "TEST", price = price, timestamp = dayNumber * day + 52_200)

    @Test
    fun everythingIsSavedWhenNothingIsStored() {
        val history = listOf(entry(1, 10.0), entry(2, 11.0))
        assertEquals(history, entriesToSave(history, stored = emptyList()))
    }

    @Test
    fun nothingIsSavedWhenFirestoreAlreadyHasTheSamePrices() {
        val history = listOf(entry(1, 10.0), entry(2, 11.0))
        assertTrue(entriesToSave(history, stored = history).isEmpty())
    }

    @Test
    fun onlyNewDaysAreSaved() {
        val stored = listOf(entry(1, 10.0), entry(2, 11.0))
        val history = stored + entry(3, 12.0)
        assertEquals(listOf(entry(3, 12.0)), entriesToSave(history, stored))
    }

    @Test
    fun aDayWhosePriceChangedIsSavedAgain() {
        val stored = listOf(entry(1, 10.0), entry(2, 11.0))
        val history = listOf(entry(1, 10.0), entry(2, 11.5))
        assertEquals(listOf(entry(2, 11.5)), entriesToSave(history, stored))
    }

    @Test
    fun aStoredEntryWithADifferentTimeOfDayStillCountsAsTheSameDay() {
        val stored = listOf(QuoteSnapshot(symbol = "TEST", price = 10.0, timestamp = 1 * day + 75_600))
        assertTrue(entriesToSave(listOf(entry(1, 10.0)), stored).isEmpty())
    }
}
