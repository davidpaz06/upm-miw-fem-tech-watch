package com.example.project_auth.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SnapshotDaysTest {

    private fun snapshot(symbol: String, timestamp: Long, price: Double = 1.0) =
        QuoteSnapshot(symbol = symbol, price = price, timestamp = timestamp)

    // 2026-11-06 14:30 UTC (market open) and 21:00 UTC (close).
    private val openTime = 1793975400L
    private val closeTime = 1793998800L

    @Test
    fun documentIdIsTheSymbolAndTheUtcDate() {
        assertEquals("AAPL_2026-11-06", snapshot("AAPL", openTime).documentId())
    }

    @Test
    fun theOpenAndTheCloseOfADayShareOneDocument() {
        assertEquals(snapshot("AAPL", openTime).documentId(), snapshot("AAPL", closeTime).documentId())
    }

    @Test
    fun differentDaysAndSymbolsGetDifferentDocuments() {
        val ids = setOf(
            snapshot("AAPL", openTime).documentId(),
            snapshot("AAPL", openTime + 86_400).documentId(),
            snapshot("MSFT", openTime).documentId(),
        )
        assertEquals(3, ids.size)
    }

    @Test
    fun latestPerDayKeepsTheMostRecentEntryOfEachDay() {
        val result = listOf(
            snapshot("AAPL", closeTime, price = 2.0),
            snapshot("AAPL", openTime, price = 1.0),
            snapshot("AAPL", openTime + 86_400, price = 3.0),
        ).latestPerDay()
        assertEquals(listOf(2.0, 3.0), result.map { it.price })
    }

    @Test
    fun latestPerDayDoesNotMixSymbolsAndSortsByTime() {
        val result = listOf(
            snapshot("MSFT", openTime + 86_400),
            snapshot("AAPL", openTime),
            snapshot("MSFT", openTime),
        ).latestPerDay()
        assertEquals(listOf("AAPL", "MSFT", "MSFT"), result.map { it.symbol })
        assertEquals(listOf(openTime, openTime, openTime + 86_400), result.map { it.timestamp })
    }
}
