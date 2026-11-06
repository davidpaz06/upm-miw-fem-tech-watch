package com.example.project_auth.domain

import com.example.project_auth.data.QuoteSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceStatsTest {

    private fun snapshots(vararg prices: Double) = prices.mapIndexed { index, price ->
        QuoteSnapshot(symbol = "TEST", price = price, timestamp = index.toLong())
    }

    @Test
    fun emptyListHasNoStats() {
        assertNull(emptyList<QuoteSnapshot>().toPriceStats())
    }

    @Test
    fun singleSnapshotHasNoPercentChange() {
        val stats = snapshots(100.0).toPriceStats()!!
        assertEquals(1, stats.count)
        assertEquals(0.0, stats.totalChange, 0.0)
        assertNull(stats.totalChangePercent)
    }

    @Test
    fun computesMaxMinAverageAndTotalChange() {
        val stats = snapshots(100.0, 120.0, 90.0, 110.0).toPriceStats()!!
        assertEquals(120.0, stats.max, 0.0)
        assertEquals(90.0, stats.min, 0.0)
        assertEquals(105.0, stats.average, 0.0)
        assertEquals(10.0, stats.totalChange, 0.0)
        assertEquals(10.0, stats.totalChangePercent!!, 1e-9)
    }
}
