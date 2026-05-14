package com.nammahomestay.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// ── Host profile stored in Firestore: hosts/{hostId} ──────────────────────
data class HostProfile(
    @DocumentId val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val village: String = "",
    val district: String = "",
    val state: String = "",
    val pin: String = "",
    val profilePhotos: List<String> = emptyList(),
    val stayName: String = "",
    val nightlyRate: String = "",
    val available: Boolean = true,
    // Amenities
    val hasAttachedBathroom: Boolean = false,
    val hasHotWater: Boolean = false,
    val hasMosquitoNet: Boolean = false,
    val hasFanAc: Boolean = false,
    val isVegetarianKitchen: Boolean = false,
    val hasParking: Boolean = false,
    // Verification checklist
    val cleanlinessRating: Int = 0,       // 1–5
    val hasToiletHygiene: Boolean = false,
    val hasSafeDrinkingWater: Boolean = false,
    val hasFirstAidKit: Boolean = false,
    val aiDescription: String = "",
    val seasonalRate: String = "",
    val blockedDates: List<String> = emptyList(),
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// ── Room: hosts/{hostId}/rooms/{roomId} ────────────────────────────────────
data class Room(
    @DocumentId val id: String = "",
    val name: String = "",              // e.g. "Deluxe Room", "Forest Cottage"
    val description: String = "",
    val pricePerNight: String = "",
    val capacity: Int = 2,             // max guests
    val imageUrls: List<String> = emptyList(),
    val available: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

// ── Menu item: hosts/{hostId}/menu/{menuId} ────────────────────────────────
data class MenuItem(
    @DocumentId val id: String = "",
    val dishName: String = "",
    val description: String = "",
    val mealType: String = "Lunch",      // Breakfast | Lunch | Dinner
    val price: String = "",
    val imageUrl: String = "",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

// ── Enquiry: enquiries/{enquiryId} ─────────────────────────────────────────
data class Enquiry(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val travellerName: String = "",
    val travellerPhone: String = "",
    val message: String = "",
    val status: String = "new",          // new | read | replied | closed
    @ServerTimestamp val createdAt: Timestamp? = null
)

// ── Local Place: hosts/{hostId}/guides/{placeId} ───────────────────────────
data class LocalPlace(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "Waterfall",  // Waterfall|Viewpoint|Farm|Temple|Beach|Market
    val distanceKm: String = "",
    val note: String = "",
    val photoUrl: String = ""
)