package com.example.project_auth.data

import com.example.project_auth.data.remote.CompanyProfile
import com.example.project_auth.data.remote.FinnhubClient
import com.example.project_auth.data.remote.FinnhubService
import com.example.project_auth.data.remote.Quote
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException

class UnknownSymbolException(symbol: String) : Exception("Unknown symbol: $symbol")

class StockRepository(private val service: FinnhubService = FinnhubClient.service) {

    suspend fun getQuote(symbol: String): Result<Quote> = safeCall {
        val quote = service.getQuote(symbol)
        // Finnhub answers 200 with all zeros for symbols it does not know.
        if (quote.current == 0.0 && quote.timestamp == 0L) throw UnknownSymbolException(symbol)
        quote
    }

    suspend fun getProfile(symbol: String): Result<CompanyProfile> = safeCall {
        val profile = service.getProfile(symbol)
        if (profile.ticker == null) throw UnknownSymbolException(symbol)
        profile
    }

    /** Fetches every symbol in parallel; one failure does not discard the others. */
    suspend fun getQuotes(symbols: List<String>): Map<String, Result<Quote>> = coroutineScope {
        symbols.map { symbol -> async { symbol to getQuote(symbol) } }.awaitAll().toMap()
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
