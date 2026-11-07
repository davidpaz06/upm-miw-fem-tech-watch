package com.example.project_auth.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.io.InterruptedIOException

/**
 * Allows at most [maxRequests] requests in any window of [windowMillis]. Callers reserve a slot
 * and are told how long to wait for it, so a burst is spread out instead of being rejected.
 */
class SlidingWindowThrottle(
    private val maxRequests: Int,
    private val windowMillis: Long,
    private val now: () -> Long = System::currentTimeMillis,
) {
    /** Slot times, oldest first. A slot in the future belongs to a caller that is still waiting. */
    private val slots = ArrayDeque<Long>()

    /** Reserves the next free slot and returns how many milliseconds to wait before using it. */
    @Synchronized
    fun reserve(): Long {
        val time = now()
        while (slots.isNotEmpty() && slots.first() <= time - windowMillis) slots.removeFirst()

        if (slots.size < maxRequests) {
            slots.addLast(time)
            return 0
        }
        // Full: the slot that frees up for this request is the one maxRequests places back.
        val freeAt = slots[slots.size - maxRequests] + windowMillis
        slots.addLast(freeAt)
        return freeAt - time
    }
}

/** Holds each call back until the [throttle] grants it a slot. */
class ThrottleInterceptor(private val throttle: SlidingWindowThrottle) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var remaining = throttle.reserve()
        while (remaining > 0) {
            if (chain.call().isCanceled()) throw IOException("Canceled while waiting for a request slot")
            val step = minOf(remaining, POLL_MILLIS)
            try {
                Thread.sleep(step)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw InterruptedIOException("Interrupted while waiting for a request slot")
            }
            remaining -= step
        }
        return chain.proceed(chain.request())
    }

    private companion object {
        const val POLL_MILLIS = 200L
    }
}
