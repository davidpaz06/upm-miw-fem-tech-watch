package com.example.project_auth.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * Keeps each company logo in the app's private storage. A logo is downloaded the first time it
 * is asked for and read from disk afterwards.
 *
 * Downloads run in the repository's own [scope], not in the caller's, so a row scrolling off the
 * screen does not cancel a download that is half done (and spend a request for nothing). Callers
 * asking for the same symbol while it downloads share that one download.
 */
class LogoRepository(
    private val directory: File,
    /** The URL of the logo image of a symbol, or null if there is none. */
    private val logoUrl: suspend (symbol: String) -> String?,
    /** The bytes at a URL, or null if it could not be fetched. */
    private val download: suspend (url: String) -> ByteArray?,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val now: () -> Long = System::currentTimeMillis,
    maxConcurrentDownloads: Int = MAX_CONCURRENT_DOWNLOADS,
) {
    private val inFlight = ConcurrentHashMap<String, Deferred<File?>>()
    private val retryAfter = ConcurrentHashMap<String, Long>()
    private val permits = Semaphore(maxConcurrentDownloads)

    init {
        directory.mkdirs()
    }

    /** The stored logo for [symbol], or null if it is not available (yet). */
    suspend fun getLogo(symbol: String): File? {
        val file = File(directory, symbol)
        if (file.exists()) return file
        if ((retryAfter[symbol] ?: 0L) > now()) return null

        // Lazy so the download only starts once it is registered, and a later caller can join it.
        val request = scope.async(start = CoroutineStart.LAZY) {
            try {
                fetch(symbol, file)
            } finally {
                inFlight.remove(symbol)
            }
        }
        val running = inFlight.putIfAbsent(symbol, request)
        if (running == null) request.start() else request.cancel()
        return (running ?: request).await()
    }

    private suspend fun fetch(symbol: String, target: File): File? = permits.withPermit {
        if (target.exists()) return@withPermit target
        val saved = try {
            val url = logoUrl(symbol)?.takeIf { it.isNotBlank() }
            val bytes = url?.let { download(it) }
            if (bytes == null || bytes.isEmpty()) null else save(bytes, target)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        // A symbol with no logo, or a failed download, is not retried for a while.
        if (saved == null) retryAfter[symbol] = now() + RETRY_COOLDOWN_MILLIS
        saved
    }

    /** Written to a temporary file first so an interrupted download never leaves a broken logo. */
    private fun save(bytes: ByteArray, target: File): File? {
        val temporary = File(directory, "${target.name}.part")
        temporary.writeBytes(bytes)
        return if (temporary.renameTo(target)) target else null
    }

    companion object {
        private const val MAX_CONCURRENT_DOWNLOADS = 4
        private const val RETRY_COOLDOWN_MILLIS = 5 * 60 * 1000L

        @Volatile
        private var instance: LogoRepository? = null

        fun get(context: Context): LogoRepository = instance ?: synchronized(this) {
            instance ?: create(context.applicationContext).also { instance = it }
        }

        private fun create(context: Context): LogoRepository {
            val stocks = StockRepository()
            // Not the Finnhub client: logos live on another host and must not receive the API key.
            val httpClient = OkHttpClient()
            return LogoRepository(
                directory = File(context.filesDir, "logos"),
                logoUrl = { symbol -> stocks.getProfile(symbol).getOrNull()?.logo },
                download = { url ->
                    httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
                        if (response.isSuccessful) response.body?.bytes() else null
                    }
                },
            )
        }
    }
}
