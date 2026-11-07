package com.example.project_auth.domain

import com.example.project_auth.data.CachedQuote
import com.example.project_auth.data.remote.Quote
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteFreshnessTest {

    private val now = 1_000_000L
    private val quote = Quote(1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1L)

    private fun saved(ageMillis: Long) = CachedQuote(quote, savedAt = now - ageMillis)

    @Test
    fun aQuoteSavedJustNowIsFresh() {
        assertTrue(saved(0).isFresh(now))
        assertTrue(saved(59_999).isFresh(now))
    }

    @Test
    fun aQuoteIsStaleOnceTheLimitIsReached() {
        assertFalse(saved(60_000).isFresh(now))
        assertFalse(saved(10 * 60_000).isFresh(now))
    }

    @Test
    fun aQuoteSavedInTheFutureIsNotTrusted() {
        // The device clock may have moved back since it was saved.
        assertFalse(saved(-1).isFresh(now))
    }

    @Test
    fun everySymbolMustBeFresh() {
        val cached = mapOf("A" to saved(1_000), "B" to saved(2_000))
        assertTrue(areAllFresh(cached, listOf("A", "B"), now))
        assertFalse(areAllFresh(cached, listOf("A", "B", "C"), now))
        assertFalse(areAllFresh(cached + ("B" to saved(90_000)), listOf("A", "B"), now))
    }

    @Test
    fun noSymbolsIsNeverFresh() {
        assertFalse(areAllFresh(emptyMap(), emptyList(), now))
    }
}
