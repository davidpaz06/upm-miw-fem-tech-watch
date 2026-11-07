package com.example.project_auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteBatchesTest {

    private val rows = (1..20).map { "S$it" }

    @Test
    fun onlyTheFirstRowsAreRequestedNow() {
        val plan = planQuoteLoad(rows, rows.toSet(), hasQuote = { true })
        assertEquals(rows.take(10), plan.now)
        assertEquals(rows.drop(10), plan.later)
    }

    @Test
    fun theOrderFollowsTheDisplayNotTheSetOfStaleSymbols() {
        val reversed = rows.reversed()
        val plan = planQuoteLoad(reversed, rows.toSet(), hasQuote = { true })
        assertEquals(reversed.take(10), plan.now)
    }

    @Test
    fun freshSymbolsAreNotRequestedAtAll() {
        val stale = rows.drop(5).toSet()
        val plan = planQuoteLoad(rows, stale, hasQuote = { true })
        assertEquals(rows.drop(5).take(10), plan.now)
        assertEquals(rows.drop(15), plan.later)
    }

    @Test
    fun aRowWithoutAnyQuoteNeverWaits() {
        val plan = planQuoteLoad(rows, rows.toSet(), hasQuote = { it != "S15" })
        assertTrue("S15" in plan.now)
        assertEquals(11, plan.now.size)
        assertEquals(9, plan.later.size)
    }

    @Test
    fun requestingEverythingLeavesNothingWaiting() {
        val plan = planQuoteLoad(rows, rows.toSet(), hasQuote = { true }, all = true)
        assertEquals(rows, plan.now)
        assertTrue(plan.later.isEmpty())
    }

    @Test
    fun nothingStaleMeansNothingToDo() {
        val plan = planQuoteLoad(rows, emptySet(), hasQuote = { true })
        assertTrue(plan.now.isEmpty() && plan.later.isEmpty())
    }

    @Test
    fun theNextBatchIsRequestedWhenTheWaitingRowsGetClose() {
        val waiting = rows.drop(10).toSet()
        // Row 6 is the last visible: the first waiting row (index 10) is still more than 3 rows away.
        assertTrue(nextQuoteBatch(rows, waiting, lastVisible = 6).isEmpty())
        assertEquals(rows.drop(10), nextQuoteBatch(rows, waiting, lastVisible = 7))
    }

    @Test
    fun aBatchHasAtMostTheConfiguredSize() {
        val batch = nextQuoteBatch(rows, rows.drop(2).toSet(), lastVisible = 5, batchSize = 4)
        assertEquals(rows.drop(2).take(4), batch)
    }

    @Test
    fun nothingWaitingMeansNoBatch() {
        assertTrue(nextQuoteBatch(rows, emptySet(), lastVisible = 19).isEmpty())
    }
}
