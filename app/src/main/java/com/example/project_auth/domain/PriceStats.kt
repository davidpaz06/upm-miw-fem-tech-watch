package com.example.project_auth.domain

import com.example.project_auth.data.QuoteSnapshot

data class PriceStats(
    val count: Int,
    val max: Double,
    val min: Double,
    val average: Double,
    /** Last price minus first price. Zero with a single snapshot. */
    val totalChange: Double,
    /** Null when there is no earlier price to compare against. */
    val totalChangePercent: Double?,
)

/** Statistics over snapshots sorted oldest first, or null if there are none. */
fun List<QuoteSnapshot>.toPriceStats(): PriceStats? {
    if (isEmpty()) return null
    val prices = map { it.price }
    val first = prices.first()
    val change = prices.last() - first
    return PriceStats(
        count = size,
        max = prices.max(),
        min = prices.min(),
        average = prices.average(),
        totalChange = change,
        totalChangePercent = if (size > 1 && first != 0.0) change / first * 100 else null,
    )
}
