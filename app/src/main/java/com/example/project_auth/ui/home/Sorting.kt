package com.example.project_auth.ui.home

import androidx.annotation.StringRes
import com.example.project_auth.R

enum class SortField(@StringRes val label: Int) {
    NAME(R.string.sort_name),
    PRICE(R.string.sort_price),
}

data class SortOrder(val field: SortField = SortField.NAME, val ascending: Boolean = true) {

    /** Tapping the active field flips the direction; tapping another one starts ascending. */
    fun select(selected: SortField): SortOrder =
        if (selected == field) copy(ascending = !ascending) else SortOrder(selected, ascending = true)
}

/**
 * Sorts in memory, never in the query. Name means the company name, not the ticker.
 * Companies without a quote always go last, whichever the direction.
 */
fun List<CompanyQuote>.sortedWith(order: SortOrder): List<CompanyQuote> {
    val (withQuote, withoutQuote) = partition { it.quote != null }
    val sorted = when (order.field) {
        SortField.NAME -> sortedByName(withQuote, order.ascending)
        SortField.PRICE -> withQuote.sortedBy { it.quote!!.current }.let { if (order.ascending) it else it.reversed() }
    }
    return sorted + sortedByName(withoutQuote, ascending = true)
}

private fun sortedByName(items: List<CompanyQuote>, ascending: Boolean): List<CompanyQuote> {
    val sorted = items.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.company.name })
    return if (ascending) sorted else sorted.reversed()
}
