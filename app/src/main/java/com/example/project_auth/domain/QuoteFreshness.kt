package com.example.project_auth.domain

import com.example.project_auth.data.CachedQuote

/** How long a saved quote is reused instead of asking Finnhub again. Pull-to-refresh uses a shorter limit. */
const val QUOTE_FRESH_MILLIS = 60_000L

/** A pull-to-refresh skips quotes fetched less than this long ago: they would barely differ. */
const val QUOTE_REFRESH_MIN_MILLIS = 20_000L

fun CachedQuote.isFresh(now: Long, ttlMillis: Long = QUOTE_FRESH_MILLIS): Boolean =
    now - savedAt in 0 until ttlMillis

/** True when every one of [symbols] has a saved quote that is still fresh. */
fun areAllFresh(
    cached: Map<String, CachedQuote>,
    symbols: Collection<String>,
    now: Long,
    ttlMillis: Long = QUOTE_FRESH_MILLIS,
): Boolean = symbols.isNotEmpty() && symbols.all { cached[it]?.isFresh(now, ttlMillis) == true }
