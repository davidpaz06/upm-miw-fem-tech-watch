package com.example.project_auth.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class SlidingWindowThrottleTest {

    private var clock = 0L
    private val throttle = SlidingWindowThrottle(maxRequests = 2, windowMillis = 1_000, now = { clock })

    @Test
    fun requestsWithinTheLimitAreNotDelayed() {
        assertEquals(0, throttle.reserve())
        assertEquals(0, throttle.reserve())
    }

    @Test
    fun aRequestOverTheLimitWaitsUntilTheOldestLeavesTheWindow() {
        throttle.reserve()
        clock = 300
        throttle.reserve()
        clock = 400
        // The oldest request (t=0) leaves the window at t=1000.
        assertEquals(600, throttle.reserve())
    }

    @Test
    fun aBurstIsSpreadOverSuccessiveWindows() {
        assertEquals(0, throttle.reserve())
        assertEquals(0, throttle.reserve())
        assertEquals(1_000, throttle.reserve())
        assertEquals(1_000, throttle.reserve())
        assertEquals(2_000, throttle.reserve())
    }

    @Test
    fun slotsFreeUpAsTimePasses() {
        throttle.reserve()
        throttle.reserve()
        clock = 1_000
        assertEquals(0, throttle.reserve())
    }
}
