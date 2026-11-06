package com.example.project_auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Layout in Firestore, always under the signed-in user:
 * `users/{uid}/quotes/{symbol}_{timestamp}` and `users/{uid}/watchlist/{symbol}`.
 */
class FirestoreRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    private fun userDocument() = db.collection(USERS).document(
        checkNotNull(auth.currentUser?.uid) { "No signed-in user" },
    )

    /**
     * The document id is derived from the quote time, so fetching the same quote
     * twice (for example when the market is closed) overwrites instead of duplicating.
     */
    suspend fun saveSnapshots(snapshots: List<QuoteSnapshot>): Result<Unit> = runCatching {
        if (snapshots.isEmpty()) return@runCatching
        val quotes = userDocument().collection(QUOTES)
        val batch = db.batch()
        snapshots.forEach { batch.set(quotes.document("${it.symbol}_${it.timestamp}"), it) }
        batch.commit().await()
    }

    /** Emits the favourite symbols now and again after every change. */
    fun watchlistSymbols(): Flow<Set<String>> = callbackFlow {
        val registration = userDocument().collection(WATCHLIST)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.map { it.id }?.toSet().orEmpty())
            }
        awaitClose { registration.remove() }
    }

    suspend fun addToWatchlist(symbol: String): Result<Unit> = runCatching {
        val entry = mapOf("symbol" to symbol, "addedAt" to System.currentTimeMillis())
        userDocument().collection(WATCHLIST).document(symbol).set(entry).await()
        Unit
    }

    suspend fun removeFromWatchlist(symbol: String): Result<Unit> = runCatching {
        userDocument().collection(WATCHLIST).document(symbol).delete().await()
        Unit
    }

    private companion object {
        const val USERS = "users"
        const val QUOTES = "quotes"
        const val WATCHLIST = "watchlist"
    }
}
