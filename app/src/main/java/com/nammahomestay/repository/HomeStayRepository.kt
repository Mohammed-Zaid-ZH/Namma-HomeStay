package com.nammahomestay.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.nammahomestay.data.Enquiry
import com.nammahomestay.data.HostProfile
import com.nammahomestay.data.LocalPlace
import com.nammahomestay.data.MenuItem
import com.nammahomestay.data.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Single source of truth for all Firestore operations.
 * Uses Kotlin Flow for real-time listeners and suspend functions for writes.
 */
class HomeStayRepository {

    private val db = FirebaseFirestore.getInstance()

    // ── Helper paths ────────────────────────────────────────────────────
    private fun hostDoc(uid: String) = db.collection("hosts").document(uid)
    private fun menuCol(uid: String) = hostDoc(uid).collection("menu")
    private fun guidesCol(uid: String) = hostDoc(uid).collection("guides")
    private fun roomsCol(uid: String) = hostDoc(uid).collection("rooms")
    private fun enquiriesCol() = db.collection("enquiries")

    // ══════════════════════════════════════════════════════════════════════
    //  HOST PROFILE
    // ══════════════════════════════════════════════════════════════════════

    /** Real-time stream of the host's profile. */
    fun getProfileFlow(uid: String): Flow<HostProfile?> = callbackFlow {
        val listener = hostDoc(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toObject<HostProfile>())
        }
        awaitClose { listener.remove() }
    }

    /** Save / update the host profile document. */
    suspend fun saveProfile(uid: String, profile: HostProfile): Result<Unit> {
        return try {
            hostDoc(uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Partially update a few fields without overwriting the whole document. */
    suspend fun updateProfileFields(uid: String, fields: Map<String, Any>): Result<Unit> {
        return try {
            hostDoc(uid).update(fields).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MENU ITEMS
    // ══════════════════════════════════════════════════════════════════════

    /** Real-time stream of active menu items, newest first. */
    fun getMenuFlow(uid: String): Flow<List<MenuItem>> = callbackFlow {
        val listener = menuCol(uid)
            .whereEqualTo("active", true)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects<MenuItem>() ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    /** Add a new menu item. Returns the generated document ID. */
    suspend fun addMenuItem(uid: String, item: MenuItem): Result<String> {
        return try {
            val ref = menuCol(uid).add(item).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Update an existing menu item. */
    suspend fun updateMenuItem(uid: String, item: MenuItem): Result<Unit> {
        return try {
            menuCol(uid).document(item.id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Soft-delete: set active = false. */
    suspend fun deleteMenuItem(uid: String, itemId: String): Result<Unit> {
        return try {
            menuCol(uid).document(itemId).update("active", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ENQUIRIES
    // ══════════════════════════════════════════════════════════════════════

    /** Real-time stream of all enquiries for this host. */
    fun getEnquiriesFlow(uid: String): Flow<List<Enquiry>> = callbackFlow {
        val listener = enquiriesCol()
            .whereEqualTo("hostId", uid)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects<Enquiry>() ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    /** Update the status of an enquiry (read / replied / closed). */
    suspend fun updateEnquiryStatus(enquiryId: String, status: String): Result<Unit> {
        return try {
            enquiriesCol().document(enquiryId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOCAL GUIDE SPOTS
    // ══════════════════════════════════════════════════════════════════════

    /** Real-time stream of local guide spots. */
    fun getGuidesFlow(uid: String): Flow<List<LocalPlace>> = callbackFlow {
        val listener = guidesCol(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toObjects<LocalPlace>() ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    /** Add a new place to the guide. */
    suspend fun addLocalPlace(uid: String, place: LocalPlace): Result<String> {
        return try {
            val ref = guidesCol(uid).add(place).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Update an existing local place. */
    suspend fun updateLocalPlace(uid: String, place: LocalPlace): Result<Unit> {
        return try {
            guidesCol(uid).document(place.id).set(place).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Delete a guide spot. */
    suspend fun deleteLocalPlace(uid: String, placeId: String): Result<Unit> {
        return try {
            guidesCol(uid).document(placeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ROOMS
    // ══════════════════════════════════════════════════════════════════════

    /** Real-time stream of all rooms for this host. */
    fun getRoomsFlow(uid: String): Flow<List<Room>> = callbackFlow {
        val listener = roomsCol(uid)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects<Room>() ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    /** Add a new room. */
    suspend fun addRoom(uid: String, room: Room): Result<String> {
        return try {
            val ref = roomsCol(uid).add(room).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Update an existing room document. */
    suspend fun updateRoom(uid: String, room: Room): Result<Unit> {
        return try {
            roomsCol(uid).document(room.id).set(room).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Delete a room. */
    suspend fun deleteRoom(uid: String, roomId: String): Result<Unit> {
        return try {
            roomsCol(uid).document(roomId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Gemini Free API (FR-02, FR-03) ────────────────────────────────
    // Uses Google Gemini 1.5 Flash free tier (1500 req/day, no cost)
    // Replace GEMINI_API_KEY with your actual key from aistudio.google.com
    private val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
    private val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$GEMINI_API_KEY"

    suspend fun callGeminiApi(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GEMINI_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            val body = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }.toString()

            OutputStreamWriter(conn.outputStream).use { it.write(body) }

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val root = JSONObject(response)
            val text = root
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            Result.success(text.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}