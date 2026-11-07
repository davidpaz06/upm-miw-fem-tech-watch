package com.example.project_auth.data

import com.example.project_auth.data.remote.YahooChartResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceHistoryMappingTest {

    // Shape of a real Yahoo response, trimmed: the third day has no price (null).
    private val json = """
        {"chart":{"result":[{
          "meta":{"currency":"USD","symbol":"TEST"},
          "timestamp":[1000,2000,3000,4000],
          "indicators":{"quote":[{
            "high":[11.0,12.5,null,13.0],
            "low":[9.0,10.0,null,11.0],
            "close":[10.0,12.0,null,11.4]
          }]}
        }],"error":null}}
    """.trimIndent()

    private fun parse(text: String = json) = Gson().fromJson(text, YahooChartResponse::class.java)

    @Test
    fun skipsDaysWithoutAPriceAndKeepsOrder() {
        val result = parse().toSnapshots("TEST", count = 50)
        assertEquals(listOf(1000L, 2000L, 4000L), result.map { it.timestamp })
        assertEquals(listOf(10.0, 12.0, 11.4), result.map { it.price })
    }

    @Test
    fun changeIsMeasuredAgainstThePreviousValidClose() {
        val result = parse().toSnapshots("TEST", count = 50)
        assertEquals(0.0, result[0].change, 1e-9)
        assertEquals(2.0, result[1].change, 1e-9)
        assertEquals(20.0, result[1].changePercent, 1e-9)
        // The day after the missing one compares with 12.0, not with the null.
        assertEquals(-0.6, result[2].change, 1e-9)
        assertEquals(-5.0, result[2].changePercent, 1e-9)
    }

    @Test
    fun keepsOnlyTheLastEntriesRequested() {
        val result = parse().toSnapshots("TEST", count = 2)
        assertEquals(listOf(2000L, 4000L), result.map { it.timestamp })
        // The first kept entry still has its change computed from the day before it.
        assertEquals(2.0, result.first().change, 1e-9)
    }

    @Test
    fun copiesHighLowAndSymbol() {
        val first = parse().toSnapshots("TEST", count = 50).first()
        assertEquals("TEST", first.symbol)
        assertEquals(11.0, first.high, 1e-9)
        assertEquals(9.0, first.low, 1e-9)
    }

    @Test
    fun anEmptyOrMalformedResponseGivesNoEntries() {
        assertTrue(parse("""{"chart":{"result":null,"error":{"code":"Not Found"}}}""").toSnapshots("X", 50).isEmpty())
        assertTrue(parse("{}").toSnapshots("X", 50).isEmpty())
    }
}
