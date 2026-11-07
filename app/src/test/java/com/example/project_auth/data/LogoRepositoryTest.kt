package com.example.project_auth.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.concurrent.atomic.AtomicInteger

class LogoRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val urlRequests = AtomicInteger()
    private val downloads = AtomicInteger()
    private var clock = 0L

    @After
    fun tearDown() = scope.cancel()

    private fun repository(
        logoUrl: suspend (String) -> String? = { "https://logos/$it.png" },
        download: suspend (String) -> ByteArray? = { byteArrayOf(1, 2, 3) },
    ) = LogoRepository(
        directory = folder.newFolder("logos"),
        logoUrl = { urlRequests.incrementAndGet(); logoUrl(it) },
        download = { downloads.incrementAndGet(); download(it) },
        scope = scope,
        now = { clock },
    )

    @Test
    fun downloadsTheLogoOnceAndThenReadsItFromDisk() = runBlocking {
        val repository = repository()
        val first = repository.getLogo("AAPL")
        val second = repository.getLogo("AAPL")
        assertNotNull(first)
        assertEquals(first, second)
        assertEquals(1, urlRequests.get())
        assertEquals(1, downloads.get())
    }

    @Test
    fun callersAskingForTheSameSymbolShareOneDownload() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val repository = repository(download = { gate.await(); byteArrayOf(1) })
        val callers = List(5) { async { repository.getLogo("MSFT") } }
        gate.complete(Unit)
        val files = callers.awaitAll()
        assertTrue(files.all { it != null && it == files.first() })
        assertEquals(1, urlRequests.get())
        assertEquals(1, downloads.get())
    }

    @Test
    fun aCancelledCallerDoesNotCancelTheDownload() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        val repository = repository(download = { started.complete(Unit); gate.await(); byteArrayOf(1) })

        // The row asks for the logo, then scrolls off the screen before it arrives.
        val row = launch { repository.getLogo("NVDA") }
        started.await()
        row.cancelAndJoin()

        gate.complete(Unit)
        // The row comes back: it gets the finished download instead of starting another.
        val file = repository.getLogo("NVDA")
        assertTrue(file!!.exists())
        assertEquals(1, urlRequests.get())
        assertEquals(1, downloads.get())
    }

    @Test
    fun aSymbolWithoutALogoIsNotRetriedUntilTheCooldownEnds() = runBlocking {
        val repository = repository(logoUrl = { null })
        assertNull(repository.getLogo("ICF"))
        assertNull(repository.getLogo("ICF"))
        assertEquals(1, urlRequests.get())

        clock = 5 * 60 * 1000L
        assertNull(repository.getLogo("ICF"))
        assertEquals(2, urlRequests.get())
    }

    @Test
    fun aFailedDownloadIsRetriedAfterTheCooldown() = runBlocking {
        var succeed = false
        val repository = repository(download = { if (succeed) byteArrayOf(9) else null })
        assertNull(repository.getLogo("IBM"))
        assertNull(repository.getLogo("IBM"))
        assertEquals(1, downloads.get())

        succeed = true
        clock = 5 * 60 * 1000L
        assertNotNull(repository.getLogo("IBM"))
        assertEquals(2, downloads.get())
    }

    @Test
    fun anEmptyDownloadIsTreatedAsAFailure() = runBlocking {
        val repository = repository(download = { byteArrayOf() })
        assertNull(repository.getLogo("AMD"))
    }
}
