package com.example.project_auth.data

import com.example.project_auth.data.remote.Quote
import com.example.project_auth.domain.QUOTE_FRESH_MILLIS

/**
 * The quotes a screen is showing, in memory, with the device time each one was last fetched.
 * Starts from the locally saved quotes, so a screen has something to show before the first
 * answer, and knows which symbols are stale enough to ask again.
 *
 * A symbol already being fetched is never requested twice, so two overlapping loads (a tab
 * switch during a refresh, a scroll during a load) share the request instead of repeating it.
 * Meant to be used from a single thread, such as the main one: the maps are not synchronised.
 */
class QuoteLoader(
    private val fetchQuotes: suspend (List<String>) -> Map<String, Result<Quote>>,
    private val loadSaved: (Collection<String>) -> Map<String, CachedQuote>,
    private val saveQuotes: (Map<String, Quote>) -> Unit,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val quotes = mutableMapOf<String, Quote>()
    private val fetchedAt = mutableMapOf<String, Long>()
    private val failed = mutableSetOf<String>()
    private val inFlight = mutableSetOf<String>()

    fun quoteOf(symbol: String): Quote? = quotes[symbol]

    fun hasQuote(symbol: String): Boolean = symbol in quotes

    /** Loads the saved quotes of the symbols this loader does not know yet. */
    fun restore(symbols: Collection<String>) {
        val unknown = symbols.filter { it !in quotes }
        loadSaved(unknown).forEach { (symbol, saved) ->
            quotes[symbol] = saved.quote
            fetchedAt[symbol] = saved.savedAt
        }
    }

    /** The symbols whose quote is older than the freshness limit, or all of them if [force] is set. */
    fun staleOf(symbols: Collection<String>, force: Boolean = false): Set<String> {
        val current = now()
        return symbols.filter { force || !isFresh(fetchedAt[it], current) }.toSet()
    }

    /**
     * Fetches [symbols] (minus any already on the way), keeps and saves the ones that arrive
     * and returns them. A symbol that fails keeps its previous quote and is remembered as failed.
     */
    suspend fun fetch(symbols: Collection<String>): Map<String, Quote> {
        val requested = symbols.filter { inFlight.add(it) }
        if (requested.isEmpty()) return emptyMap()
        try {
            val results = fetchQuotes(requested)
            val fresh = results.mapNotNull { (symbol, result) -> result.getOrNull()?.let { symbol to it } }.toMap()
            val time = now()
            fresh.forEach { (symbol, quote) ->
                quotes[symbol] = quote
                fetchedAt[symbol] = time
                failed.remove(symbol)
            }
            failed += requested.filter { it !in fresh }
            saveQuotes(fresh)
            return fresh
        } finally {
            inFlight.removeAll(requested.toSet())
        }
    }

    /** Device time of the oldest quote shown in place of a fresh one that could not be fetched. */
    fun offlineSince(symbols: Collection<String>): Long? =
        symbols.filter { it in failed }.mapNotNull { fetchedAt[it] }.minOrNull()

    private fun isFresh(fetched: Long?, current: Long): Boolean =
        fetched != null && current - fetched in 0 until QUOTE_FRESH_MILLIS
}
