package com.example.project_auth.data

import com.example.project_auth.data.remote.Quote

/**
 * One stored price point. Every field has a default because Firestore needs a
 * no-argument constructor to rebuild the object. [timestamp] is the Finnhub quote
 * time in epoch seconds.
 */
data class QuoteSnapshot(
    val symbol: String = "",
    val price: Double = 0.0,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val timestamp: Long = 0L,
)

fun Quote.toSnapshot(symbol: String) = QuoteSnapshot(
    symbol = symbol,
    price = current,
    change = change ?: 0.0,
    changePercent = changePercent ?: 0.0,
    high = high,
    low = low,
    timestamp = timestamp,
)
