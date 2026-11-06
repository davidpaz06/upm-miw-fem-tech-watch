package com.example.project_auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    private companion object {
        const val USERS = "users"
        const val QUOTES = "quotes"
    }
}
