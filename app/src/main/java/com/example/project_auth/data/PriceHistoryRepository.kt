package com.example.project_auth.data

import com.example.project_auth.data.remote.YahooChartClient
import com.example.project_auth.data.remote.YahooChartResponse
import com.example.project_auth.data.remote.YahooChartService
import kotlin.coroutines.cancellation.CancellationException

class NoHistoryException(symbol: String) : Exception("No price history for $symbol")

/** Daily price history of a symbol, one [QuoteSnapshot] per trading day. */
class PriceHistoryRepository(private val service: YahooChartService = YahooChartClient.service) {

    /**
     * The most recent [count] daily entries, oldest first. Three months of bars are requested
     * (about 60 trading days) so the first entry returned still has a previous close to compare with.
     */
    suspend fun getHistory(symbol: String, count: Int = DEFAULT_COUNT): Result<List<QuoteSnapshot>> =
        try {
            val snapshots = service.chart(symbol, range = RANGE, interval = INTERVAL).toSnapshots(symbol, count)
            if (snapshots.isEmpty()) Result.failure(NoHistoryException(symbol)) else Result.success(snapshots)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    companion object {
        const val DEFAULT_COUNT = 50
        private const val RANGE = "3mo"
        private const val INTERVAL = "1d"
    }
}

/** Days without a close are skipped; the change of each day is measured against the previous valid close. */
fun YahooChartResponse.toSnapshots(symbol: String, count: Int): List<QuoteSnapshot> {
    val result = chart?.result?.firstOrNull() ?: return emptyList()
    val timestamps = result.timestamp ?: return emptyList()
    val series = result.indicators?.quote?.firstOrNull() ?: return emptyList()
    val closes = series.close ?: return emptyList()

    val snapshots = ArrayList<QuoteSnapshot>(timestamps.size)
    var previousClose: Double? = null
    timestamps.forEachIndexed { index, timestamp ->
        val close = closes.getOrNull(index) ?: return@forEachIndexed
        val change = previousClose?.let { close - it } ?: 0.0
        snapshots += QuoteSnapshot(
            symbol = symbol,
            price = close,
            change = change,
            changePercent = previousClose?.takeIf { it != 0.0 }?.let { change / it * 100 } ?: 0.0,
            high = series.high?.getOrNull(index) ?: close,
            low = series.low?.getOrNull(index) ?: close,
            timestamp = timestamp,
        )
        previousClose = close
    }
    return snapshots.takeLast(count)
}
