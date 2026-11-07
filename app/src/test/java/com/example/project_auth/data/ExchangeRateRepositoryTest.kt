package com.example.project_auth.data

import com.example.project_auth.data.remote.ExchangeRateService
import com.example.project_auth.data.remote.ExchangeRatesResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExchangeRateRepositoryTest {

    private class FakeService(var response: ExchangeRatesResponse) : ExchangeRateService {
        var calls = 0
        override suspend fun latestUsdRates(): ExchangeRatesResponse {
            calls++
            return response
        }
    }

    private val ok = ExchangeRatesResponse("success", mapOf("USD" to 1.0, "TWD" to 32.0, "JPY" to 150.0))
    private var clock = 0L

    private fun repository(service: FakeService) =
        ExchangeRateRepository(service, ttlMillis = 1_000, now = { clock })

    @Test
    fun returnsTheRateOfACurrency() = runBlocking {
        assertEquals(32.0, repository(FakeService(ok)).unitsPerUsd("TWD").getOrThrow(), 0.0)
    }

    @Test
    fun matchesTheCurrencyCodeIgnoringCase() = runBlocking {
        assertEquals(150.0, repository(FakeService(ok)).unitsPerUsd("jpy").getOrThrow(), 0.0)
    }

    @Test
    fun keepsTheRatesInMemoryUntilTheyExpire() = runBlocking {
        val service = FakeService(ok)
        val repository = repository(service)
        repository.unitsPerUsd("TWD")
        clock = 999
        repository.unitsPerUsd("JPY")
        assertEquals(1, service.calls)
        clock = 1_000
        repository.unitsPerUsd("JPY")
        assertEquals(2, service.calls)
    }

    @Test
    fun anUnknownCurrencyIsAFailure() = runBlocking {
        val result = repository(FakeService(ok)).unitsPerUsd("XYZ")
        assertTrue(result.exceptionOrNull() is UnknownCurrencyException)
    }

    @Test
    fun aFailedAnswerFromTheProviderIsAFailure() = runBlocking {
        val bad = FakeService(ExchangeRatesResponse("error", null))
        assertTrue(repository(bad).unitsPerUsd("TWD").isFailure)
    }
}
