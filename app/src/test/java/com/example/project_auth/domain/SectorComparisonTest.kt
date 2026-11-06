package com.example.project_auth.domain

import com.example.project_auth.data.QuoteSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SectorComparisonTest {

    private fun snapshot(symbol: String, price: Double, timestamp: Long) =
        QuoteSnapshot(symbol = symbol, price = price, timestamp = timestamp)

    @Test
    fun sectorsWithoutHistoryHaveNoAverage() {
        val summaries = compareSectors(emptyList())
        assertEquals(Sector.entries.size, summaries.size)
        summaries.forEach {
            assertNull(it.averageChangePercent)
            assertEquals(0, it.companiesWithData)
        }
    }

    @Test
    fun averagesTheTotalChangeOfCompaniesInTheSameSector() {
        val summaries = compareSectors(
            listOf(
                // MSFT +10%, AMZN -10% -> Cloud average 0%
                snapshot("MSFT", 100.0, 1), snapshot("MSFT", 110.0, 2),
                snapshot("AMZN", 100.0, 1), snapshot("AMZN", 90.0, 2),
                // NVDA +20% -> AI average 20%
                snapshot("NVDA", 100.0, 1), snapshot("NVDA", 120.0, 2),
            ),
        )
        val cloud = summaries.first { it.sector == Sector.CLOUD }
        val ai = summaries.first { it.sector == Sector.AI }
        assertEquals(0.0, cloud.averageChangePercent!!, 1e-9)
        assertEquals(2, cloud.companiesWithData)
        assertEquals(20.0, ai.averageChangePercent!!, 1e-9)
        assertEquals(1, ai.companiesWithData)
    }

    @Test
    fun ignoresCompaniesWithASingleSnapshot() {
        val summaries = compareSectors(listOf(snapshot("MSFT", 100.0, 1)))
        val cloud = summaries.first { it.sector == Sector.CLOUD }
        assertNull(cloud.averageChangePercent)
        assertEquals(0, cloud.companiesWithData)
    }

    @Test
    fun sortsByTimestampBeforeComparing() {
        val summaries = compareSectors(
            listOf(snapshot("MSFT", 110.0, 2), snapshot("MSFT", 100.0, 1)),
        )
        assertEquals(10.0, summaries.first { it.sector == Sector.CLOUD }.averageChangePercent!!, 1e-9)
    }
}
