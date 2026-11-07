package com.example.project_auth.domain

/** How many stale quotes are requested straight away; the rest wait until their rows come into view. */
const val QUOTE_EAGER_COUNT = 10

/** How many waiting quotes are requested each time the list scrolls near them. */
const val QUOTE_BATCH_SIZE = 10

/** How many rows below the last visible one are already worth requesting. */
const val QUOTE_LOOKAHEAD = 1

/** Which of the stale symbols are requested now and which wait for their rows to be seen. */
data class QuotePlan(val now: List<String>, val later: List<String>)

/**
 * Splits [stale] (kept in the order the rows are shown in [displayOrder]) into the quotes to
 * request now and the ones to request lazily. A symbol with no quote at all is always requested
 * now, since its row would be empty; waiting only applies to rows that already show a saved
 * quote. With [all] set (sorting by price needs every price) nothing waits.
 */
fun planQuoteLoad(
    displayOrder: List<String>,
    stale: Set<String>,
    hasQuote: (String) -> Boolean,
    all: Boolean = false,
    eagerCount: Int = QUOTE_EAGER_COUNT,
): QuotePlan {
    val ordered = displayOrder.filter { it in stale }
    if (all) return QuotePlan(ordered, emptyList())
    val firstRows = ordered.take(eagerCount).toSet()
    val now = ordered.filter { it in firstRows || !hasQuote(it) }
    return QuotePlan(now, ordered.filter { it !in now })
}

/**
 * The waiting symbols to request now that the row at [lastVisible] is on screen: the next
 * [batchSize] of them, as soon as the first one is within [lookahead] rows of the visible area.
 */
fun nextQuoteBatch(
    displayOrder: List<String>,
    waiting: Set<String>,
    lastVisible: Int,
    lookahead: Int = QUOTE_LOOKAHEAD,
    batchSize: Int = QUOTE_BATCH_SIZE,
): List<String> {
    val ordered = displayOrder.filter { it in waiting }
    val first = ordered.firstOrNull() ?: return emptyList()
    if (displayOrder.indexOf(first) > lastVisible + lookahead) return emptyList()
    return ordered.take(batchSize)
}
