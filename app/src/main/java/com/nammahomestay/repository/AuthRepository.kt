package com.nammahomestay.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.nammahomestay.data.HostProfile
import kotlinx.coroutines.tasks.await

/**
 * Handles Firebase Authentication (email + password).
 * All functions are suspend — call them from a ViewModel coroutine.
 */
class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /** Returns the currently signed-in user, or null if not signed in. */
    val currentUser: FirebaseUser? get() = auth.currentUser

    val isLoggedIn: Boolean get() = auth.currentUser != null

    /**
     * Sign in with email & password.
     * Returns Result.success(uid) or Result.failure(exception).
     */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new host with email & password.
     * After creating the Auth user, a skeleton HostProfile document is written to Firestore.
     */
    suspend fun register(
        email: String,
        password: String,
        fullName: String
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            // Create an initial profile document
            val profile = HostProfile(
                id = uid,
                email = email,
                fullName = fullName
            )
            db.collection("hosts").document(uid).set(profile).await()

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Signs out the current user. */
    fun logout() {
        auth.signOut()
    }

    /** Deletes the current Firebase Auth user account. */
    suspend fun deleteCurrentAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
