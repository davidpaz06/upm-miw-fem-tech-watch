package com.example.project_auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketCapTest {

    @Test
    fun dollarAmountsAreLeftAsTheyAre() {
        val cap = MarketCap.inUsd(3_940_412.0)
        assertTrue(cap.isUsd)
        assertEquals(3_940_412.0, cap.usdMillions!!, 0.0)
        assertEquals(1.0, cap.unitsPerUsd!!, 0.0)
    }

    @Test
    fun convertsALocalAmountToDollars() {
        // TSMC: 58,607,154 million TWD at 32.84 TWD per dollar.
        val cap = MarketCap.converted(58_607_154.0, "TWD", 32.84)
        assertFalse(cap.isUsd)
        assertEquals(1_784_627.0, cap.usdMillions!!, 1.0)
        assertEquals(32.84, cap.unitsPerUsd!!, 0.0)
    }

    @Test
    fun anUnconvertedAmountHasNoDollarEquivalent() {
        val cap = MarketCap.unconverted(1_955_127.0, "INR")
        assertNull(cap.usdMillions)
        assertNull(cap.unitsPerUsd)
    }
}
