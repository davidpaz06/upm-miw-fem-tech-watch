package com.example.project_auth.domain

/**
 * A company's market capitalization as Finnhub reports it, plus its US dollar equivalent.
 * Amounts are in millions. [usdMillions] and [unitsPerUsd] are null while the exchange rate
 * is still loading or when it could not be obtained.
 */
data class MarketCap(
    val localMillions: Double,
    val currency: String,
    val usdMillions: Double?,
    /** How many units of [currency] equal 1 USD. */
    val unitsPerUsd: Double?,
) {
    val isUsd: Boolean get() = currency == USD

    companion object {
        const val USD = "USD"

        fun inUsd(millions: Double) = MarketCap(millions, USD, usdMillions = millions, unitsPerUsd = 1.0)

        /** Converts with [unitsPerUsd] of [currency] per dollar. */
        fun converted(localMillions: Double, currency: String, unitsPerUsd: Double) =
            MarketCap(localMillions, currency, usdMillions = localMillions / unitsPerUsd, unitsPerUsd = unitsPerUsd)

        /** Before the rate is known: the amount in its own currency, with no dollar equivalent. */
        fun unconverted(localMillions: Double, currency: String) =
            MarketCap(localMillions, currency, usdMillions = null, unitsPerUsd = null)
    }
}
