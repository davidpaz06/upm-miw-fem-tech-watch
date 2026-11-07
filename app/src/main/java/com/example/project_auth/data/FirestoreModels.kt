package com.example.project_auth.data

import com.example.project_auth.data.remote.Quote
import java.time.LocalDate

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

private const val SECONDS_PER_DAY = 86_400L

// Extensions rather than properties: Firestore would write a property getter as a field.

/** The UTC day of the snapshot as a day number; US markets open and close within one UTC day. */
fun QuoteSnapshot.dayKey(): Long = timestamp / SECONDS_PER_DAY

/** One document per symbol and day, so the same day from Finnhub and from the history is one entry. */
fun QuoteSnapshot.documentId(): String = "${symbol}_${LocalDate.ofEpochDay(dayKey())}"

/** Keeps the most recent snapshot of each symbol and day, oldest first. */
fun List<QuoteSnapshot>.latestPerDay(): List<QuoteSnapshot> =
    groupBy { it.symbol to it.dayKey() }
        .map { (_, sameDay) -> sameDay.maxBy { it.timestamp } }
        .sortedBy { it.timestamp }

fun Quote.toSnapshot(symbol: String) = QuoteSnapshot(
    symbol = symbol,
    price = current,
    change = change ?: 0.0,
    changePercent = changePercent ?: 0.0,
    high = high,
    low = low,
    timestamp = timestamp,
)
