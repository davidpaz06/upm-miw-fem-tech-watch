package com.example.project_auth.domain

import com.example.project_auth.data.remote.CompanyProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TradingCurrencyTest {

    private fun profile(exchange: String?, currency: String?) =
        CompanyProfile(
            name = "Test", ticker = "TEST", logo = null, exchange = exchange,
            industry = null, marketCap = 1.0, currency = currency,
        )

    // Values below are the ones Finnhub returned for the real companies.

    @Test
    fun usListingsAreInDollars() {
        assertEquals("USD", profile("NASDAQ NMS - GLOBAL MARKET", "USD").tradingCurrency())
        assertEquals("USD", profile("NEW YORK STOCK EXCHANGE", "USD").tradingCurrency())
    }

    @Test
    fun foreignHomeExchangesUseTheirOwnCurrency() {
        assertEquals("TWD", profile("TAIWAN STOCK EXCHANGE", "TWD").tradingCurrency())
        assertEquals("INR", profile("NATIONAL STOCK EXCHANGE OF INDIA", "INR").tradingCurrency())
        assertEquals("JPY", profile("TOKYO STOCK EXCHANGE-TOKYO PRO MARKET", "JPY").tradingCurrency())
    }

    @Test
    fun theExchangeWinsOverAWrongReportingCurrency() {
        // Lenovo: reports in USD, market cap is in Hong Kong dollars.
        assertEquals("HKD", profile("HONG KONG EXCHANGES AND CLEARING LTD", "USD").tradingCurrency())
        // Xiaomi: reports in CNY, trades in Hong Kong.
        assertEquals("HKD", profile("HONG KONG EXCHANGES AND CLEARING LTD", "CNY").tradingCurrency())
        // Logitech: reports in USD, market cap is in Swiss francs.
        assertEquals("CHF", profile("SWISS EXCHANGE", "USD").tradingCurrency())
    }

    @Test
    fun anUnknownExchangeFallsBackToTheProfileCurrency() {
        assertEquals("GBP", profile("SOME NEW EXCHANGE", "gbp").tradingCurrency())
    }

    @Test
    fun nothingKnownGivesNull() {
        assertNull(profile(null, null).tradingCurrency())
        assertNull(profile("SOME NEW EXCHANGE", " ").tradingCurrency())
    }
}
