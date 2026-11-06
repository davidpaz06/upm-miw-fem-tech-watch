package com.example.project_auth.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * Keeps each company logo in the app's private storage. The logo is downloaded the
 * first time it is asked for and read from disk afterwards.
 */
class LogoRepository private constructor(
    context: Context,
    private val stocks: StockRepository = StockRepository(),
    // Not the Finnhub client: logos live on another host and must not receive the API key.
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val directory = File(context.filesDir, "logos").apply { mkdirs() }
    private val downloadLock = Mutex()

    /** Symbols that failed this session, so a missing logo is not retried on every recomposition. */
    private val failed = ConcurrentHashMap.newKeySet<String>()

    /** The stored logo for [symbol], or null if it is not available (yet). */
    suspend fun getLogo(symbol: String): File? = withContext(Dispatchers.IO) {
        val file = fileFor(symbol)
        if (file.exists()) return@withContext file
        if (symbol in failed) return@withContext null

        downloadLock.withLock {
            // Another caller may have finished the download while this one waited.
            if (file.exists()) return@withLock file
            download(symbol, file).also { if (it == null) failed += symbol }
        }
    }

    private suspend fun download(symbol: String, target: File): File? {
        val url = stocks.getProfile(symbol).getOrNull()?.logo?.takeIf { it.isNotBlank() } ?: return null
        return try {
            httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
                val bytes = response.body?.bytes()
                if (!response.isSuccessful || bytes == null || bytes.isEmpty()) return null
                // Write to a temporary file first so an interrupted download never leaves a broken logo.
                val temporary = File(directory, "${target.name}.part")
                temporary.writeBytes(bytes)
                if (temporary.renameTo(target)) target else null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private fun fileFor(symbol: String) = File(directory, symbol)

    companion object {
        @Volatile
        private var instance: LogoRepository? = null

        fun get(context: Context): LogoRepository = instance ?: synchronized(this) {
            instance ?: LogoRepository(context.applicationContext).also { instance = it }
        }
    }
}
