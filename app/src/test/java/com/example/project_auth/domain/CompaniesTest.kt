package com.example.project_auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompaniesTest {

    @Test
    fun everySymbolAppearsOnlyOnce() {
        val duplicates = Companies.all.groupBy { it.symbol }.filterValues { it.size > 1 }.keys
        assertTrue("Duplicated symbols: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun everyNameAppearsOnlyOnce() {
        val duplicates = Companies.all.groupBy { it.name }.filterValues { it.size > 1 }.keys
        assertTrue("Duplicated names: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun noSectorHasMoreThanTwentyCompanies() {
        Sector.entries.forEach { sector ->
            val count = Companies.bySector(sector).size
            assertTrue("$sector has $count companies", count in 1..20)
        }
    }

    @Test
    fun everySectorIsFilledToTheLimit() {
        Sector.entries.forEach { assertEquals(20, Companies.bySector(it).size) }
    }

    @Test
    fun findLooksUpBySymbol() {
        assertEquals("Apple", Companies.find("AAPL")?.name)
    }
}
