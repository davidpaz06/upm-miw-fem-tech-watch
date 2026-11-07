package com.example.project_auth.data

import com.example.project_auth.data.remote.Quote
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteLoaderTest {

    private var clock = 1_000_000L
    private val requests = mutableListOf<List<String>>()
    private val saved = mutableMapOf<String, CachedQuote>()
    private val written = mutableListOf<Map<String, Quote>>()
    private var unavailable = emptySet<String>()
    private var gate: CompletableDeferred<Unit>? = null

    private fun quote(price: Double) = Quote(price, 0.0, 0.0, price, price, price, price, 1L)

    private fun loader() = QuoteLoader(
        fetchQuotes = { symbols ->
            requests += symbols
            gate?.await()
            symbols.associateWith {
                if (it in unavailable) Result.failure(Exception("down")) else Result.success(quote(10.0))
            }
        },
        loadSaved = { symbols -> saved.filterKeys { it in symbols } },
        saveQuotes = { written += it },
        now = { clock },
    )

    @Test
    fun startsFromTheSavedQuotes() {
        saved["A"] = CachedQuote(quote(5.0), clock - 5_000)
        val loader = loader()
        loader.restore(listOf("A", "B"))
        assertEquals(5.0, loader.quoteOf("A")!!.current, 0.0)
        assertFalse(loader.hasQuote("B"))
    }

    @Test
    fun aSavedQuoteLessThanAMinuteOldIsNotStale() {
        saved["A"] = CachedQuote(quote(5.0), clock - 5_000)
        saved["B"] = CachedQuote(quote(5.0), clock - 120_000)
        val loader = loader()
        loader.restore(listOf("A", "B", "C"))
        assertEquals(setOf("B", "C"), loader.staleOf(listOf("A", "B", "C")))
        // A pull-to-refresh asks again for anything older than 20 s, but not for a 5 s old quote.
        assertEquals(setOf("B", "C"), loader.staleOf(listOf("A", "B", "C"), force = true))
    }

    @Test
    fun aPullToRefreshSkipsQuotesFetchedSecondsAgo() {
        saved["A"] = CachedQuote(quote(5.0), clock - 19_999)
        saved["B"] = CachedQuote(quote(5.0), clock - 20_000)
        val loader = loader()
        loader.restore(listOf("A", "B"))
        assertEquals(setOf("B"), loader.staleOf(listOf("A", "B"), force = true))
    }

    @Test
    fun restoringDoesNotOverwriteWhatIsAlreadyInMemory() = runBlocking {
        val loader = loader()
        loader.fetch(listOf("A"))
        saved["A"] = CachedQuote(quote(1.0), clock - 500_000)
        loader.restore(listOf("A"))
        assertEquals(10.0, loader.quoteOf("A")!!.current, 0.0)
    }

    @Test
    fun aFetchedQuoteIsKeptSavedAndFresh() = runBlocking {
        val loader = loader()
        val fresh = loader.fetch(listOf("A", "B"))
        assertEquals(setOf("A", "B"), fresh.keys)
        assertEquals(listOf(fresh), written)
        assertTrue(loader.staleOf(listOf("A", "B")).isEmpty())
        clock += 60_000
        assertEquals(setOf("A", "B"), loader.staleOf(listOf("A", "B")))
    }

    @Test
    fun aFailedSymbolKeepsItsOldQuoteAndIsReportedAsOffline() = runBlocking {
        saved["A"] = CachedQuote(quote(5.0), clock - 90_000)
        val loader = loader()
        loader.restore(listOf("A", "B"))
        unavailable = setOf("A")
        val fresh = loader.fetch(listOf("A", "B"))
        assertEquals(setOf("B"), fresh.keys)
        assertEquals(5.0, loader.quoteOf("A")!!.current, 0.0)
        assertEquals(clock - 90_000, loader.offlineSince(listOf("A", "B")))
        assertTrue("A" in loader.staleOf(listOf("A")))
    }

    @Test
    fun aSymbolThatRecoversIsNoLongerOffline() = runBlocking {
        saved["A"] = CachedQuote(quote(5.0), clock - 90_000)
        val loader = loader()
        loader.restore(listOf("A"))
        unavailable = setOf("A")
        loader.fetch(listOf("A"))
        unavailable = emptySet()
        loader.fetch(listOf("A"))
        assertNull(loader.offlineSince(listOf("A")))
    }

    @Test
    fun aSymbolAlreadyOnTheWayIsNotRequestedAgain() = runBlocking {
        val loader = loader()
        gate = CompletableDeferred()
        val first = async(start = CoroutineStart.UNDISPATCHED) { loader.fetch(listOf("A", "B")) }
        val second = loader.fetch(listOf("B"))
        assertTrue(second.isEmpty())
        val third = async(start = CoroutineStart.UNDISPATCHED) { loader.fetch(listOf("B", "C")) }
        gate!!.complete(Unit)
        first.await()
        third.await()
        assertEquals(listOf(listOf("A", "B"), listOf("C")), requests)
    }

    @Test
    fun aCancelledFetchFreesItsSymbolsForTheNextOne() = runBlocking {
        val loader = loader()
        gate = CompletableDeferred()
        val job = async(start = CoroutineStart.UNDISPATCHED) { loader.fetch(listOf("A")) }
        job.cancelAndJoin()
        gate!!.complete(Unit)
        assertEquals(setOf("A"), loader.fetch(listOf("A")).keys)
    }
}
