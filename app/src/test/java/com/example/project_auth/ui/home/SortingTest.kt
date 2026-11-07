package com.example.project_auth.ui.home

import com.example.project_auth.data.remote.Quote
import com.example.project_auth.domain.Company
import com.example.project_auth.domain.Sector
import org.junit.Assert.assertEquals
import org.junit.Test

class SortingTest {

    private fun item(symbol: String, name: String, price: Double?) = CompanyQuote(
        Company(symbol, name, Sector.CLOUD),
        price?.let { Quote(it, 0.0, 0.0, it, it, it, it, 1L) },
    )

    // The ticker order (A, B, C, D) is deliberately the opposite of the name order.
    private val items = listOf(
        item("AAA", "Zeta", 30.0),
        item("BBB", "beta", 10.0),
        item("CCC", "Alfa", null),
        item("DDD", "Gamma", 20.0),
    )

    private fun List<CompanyQuote>.names() = map { it.company.name }

    @Test
    fun sortsByCompanyNameNotByTicker() {
        val result = items.sortedWith(SortOrder(SortField.NAME, ascending = true))
        assertEquals(listOf("beta", "Gamma", "Zeta", "Alfa"), result.names())
    }

    @Test
    fun nameDescendingReversesTheOrder() {
        val result = items.sortedWith(SortOrder(SortField.NAME, ascending = false))
        assertEquals(listOf("Zeta", "Gamma", "beta", "Alfa"), result.names())
    }

    @Test
    fun priceAscendingPutsMissingQuotesLast() {
        val result = items.sortedWith(SortOrder(SortField.PRICE, ascending = true))
        assertEquals(listOf("beta", "Gamma", "Zeta", "Alfa"), result.names())
    }

    @Test
    fun priceDescendingStillPutsMissingQuotesLast() {
        val result = items.sortedWith(SortOrder(SortField.PRICE, ascending = false))
        assertEquals(listOf("Zeta", "Gamma", "beta", "Alfa"), result.names())
    }

    @Test
    fun selectingTheActiveFieldFlipsDirectionAndAnotherFieldResetsIt() {
        val byName = SortOrder(SortField.NAME, ascending = true)
        assertEquals(SortOrder(SortField.NAME, ascending = false), byName.select(SortField.NAME))
        assertEquals(SortOrder(SortField.PRICE, ascending = true), byName.select(SortField.PRICE))
        assertEquals(
            SortOrder(SortField.NAME, ascending = true),
            SortOrder(SortField.PRICE, ascending = false).select(SortField.NAME),
        )
    }
}
