package com.example.project_auth.domain

import com.example.project_auth.data.QuoteSnapshot
import com.example.project_auth.data.dayKey
import kotlin.math.abs

/**
 * The entries of a freshly fetched [history] that Firestore does not have yet, or has with a
 * different price (the current day changes while the market is open). Writing only those keeps
 * opening the detail from rewriting 50 documents every time.
 */
fun entriesToSave(
    history: List<QuoteSnapshot>,
    stored: List<QuoteSnapshot>,
    priceTolerance: Double = 0.005,
): List<QuoteSnapshot> {
    val storedByDay = stored.associateBy { it.dayKey() }
    return history.filter { entry ->
        val existing = storedByDay[entry.dayKey()]
        existing == null || abs(existing.price - entry.price) > priceTolerance
    }
}
