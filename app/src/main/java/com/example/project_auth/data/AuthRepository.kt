package com.example.project_auth.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val userFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun signOut() = auth.signOut()
}

private suspend fun <T> Task<T>.await() {
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(Unit) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }
}
