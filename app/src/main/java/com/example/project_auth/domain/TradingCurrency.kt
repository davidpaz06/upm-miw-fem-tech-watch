package com.example.project_auth.domain

import com.example.project_auth.data.remote.CompanyProfile

/**
 * Finnhub gives the market capitalization in the currency of the company's home exchange, but
 * its `currency` field is the reporting currency, and the two differ: Lenovo reports in USD and
 * trades in Hong Kong dollars, Logitech reports in USD and trades in Swiss francs. Comparing
 * market cap with price and share count confirmed the exchange is the reliable guide, so the
 * exchange is looked up first and the `currency` field is only a fallback.
 */
private val CURRENCY_BY_EXCHANGE = listOf(
    "NASDAQ" to "USD",
    "NEW YORK" to "USD",
    "NYSE" to "USD",
    "AMEX" to "USD",
    "TAIWAN" to "TWD",
    "INDIA" to "INR",
    "BOMBAY" to "INR",
    "TOKYO" to "JPY",
    "HONG KONG" to "HKD",
    "SWISS" to "CHF",
    "SHANGHAI" to "CNY",
    "SHENZHEN" to "CNY",
    "KOREA" to "KRW",
    "TORONTO" to "CAD",
    "AUSTRALIAN" to "AUD",
    "SINGAPORE" to "SGD",
    "FRANKFURT" to "EUR",
    "XETRA" to "EUR",
    "EURONEXT" to "EUR",
    "PARIS" to "EUR",
    "AMSTERDAM" to "EUR",
    "MILAN" to "EUR",
    "MADRID" to "EUR",
)

/** ISO code of the currency [CompanyProfile.marketCap] is expressed in, or null if unknown. */
fun CompanyProfile.tradingCurrency(): String? {
    val name = exchange?.uppercase().orEmpty()
    return CURRENCY_BY_EXCHANGE.firstOrNull { (keyword, _) -> keyword in name }?.second
        ?: currency?.takeIf { it.isNotBlank() }?.uppercase()
}
