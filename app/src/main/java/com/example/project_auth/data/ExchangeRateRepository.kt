package com.example.project_auth.data

import com.example.project_auth.data.remote.ExchangeRateClient
import com.example.project_auth.data.remote.ExchangeRateService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class UnknownCurrencyException(code: String) : Exception("No exchange rate for $code")

/**
 * Exchange rates against the US dollar. The provider updates them once a day, so they are kept
 * in memory for [ttlMillis] and shared by everything that asks, instead of requested per screen.
 */
class ExchangeRateRepository(
    private val service: ExchangeRateService = ExchangeRateClient.service,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val lock = Mutex()
    private var rates: Map<String, Double>? = null
    private var fetchedAt = 0L

    /** How many units of [currency] equal 1 USD. */
    suspend fun unitsPerUsd(currency: String): Result<Double> = try {
        val rate = currentRates()[currency.uppercase()]
        if (rate != null && rate > 0) Result.success(rate) else Result.failure(UnknownCurrencyException(currency))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun currentRates(): Map<String, Double> = lock.withLock {
        rates?.takeIf { now() - fetchedAt < ttlMillis }
            ?: fetch().also {
                rates = it
                fetchedAt = now()
            }
    }

    private suspend fun fetch(): Map<String, Double> {
        val response = service.latestUsdRates()
        check(response.result == "success") { "Exchange rate provider answered ${response.result}" }
        return checkNotNull(response.rates) { "Exchange rate provider sent no rates" }
    }

    companion object {
        private const val DEFAULT_TTL_MILLIS = 6 * 60 * 60 * 1000L

        /** One instance for the whole app, so the cache is shared between screens. */
        val shared: ExchangeRateRepository by lazy { ExchangeRateRepository() }
    }
}
