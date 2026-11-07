package com.example.project_auth.data

import android.content.Context
import android.content.SharedPreferences
import com.example.project_auth.data.remote.Quote
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/** A quote as last fetched, with the device time ([savedAt], epoch millis) it was stored. */
data class CachedQuote(
    @SerializedName("quote") val quote: Quote,
    @SerializedName("savedAt") val savedAt: Long,
)

/**
 * Local copy of the latest quote of each symbol, so the lists still show data
 * when Finnhub cannot be reached. Quotes are market data, not per-user, so they
 * are shared by every account on the device.
 */
class QuoteCache(private val prefs: SharedPreferences, private val gson: Gson = Gson()) {

    fun load(symbols: Collection<String>): Map<String, CachedQuote> =
        symbols.mapNotNull { symbol ->
            val json = prefs.getString(symbol, null) ?: return@mapNotNull null
            // A corrupt or outdated entry is treated as missing rather than crashing.
            runCatching { gson.fromJson(json, CachedQuote::class.java) }.getOrNull()
                ?.let { symbol to it }
        }.toMap()

    fun save(quotes: Map<String, Quote>) {
        if (quotes.isEmpty()) return
        val now = System.currentTimeMillis()
        prefs.edit().apply {
            quotes.forEach { (symbol, quote) -> putString(symbol, gson.toJson(CachedQuote(quote, now))) }
        }.apply()
    }

    companion object {
        private const val FILE_NAME = "quote_cache"

        lateinit var instance: QuoteCache
            private set

        /** Called once from [com.example.project_auth.TechWatchApp]. */
        fun init(context: Context) {
            instance = QuoteCache(context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE))
        }
    }
}
