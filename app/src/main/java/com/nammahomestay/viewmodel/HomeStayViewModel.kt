package com.nammahomestay.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammahomestay.data.Enquiry
import com.nammahomestay.data.HostProfile
import com.nammahomestay.data.LocalPlace
import com.nammahomestay.data.MenuItem
import com.nammahomestay.data.Room
import com.nammahomestay.repository.HomeStayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeStayViewModel : ViewModel() {

    private val repo = HomeStayRepository()

    // ── Profile ──────────────────────────────────────────────────────────
    private val _profile = MutableStateFlow<HostProfile?>(null)
    val profile: StateFlow<HostProfile?> = _profile

    private val _profileSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val profileSaveState: StateFlow<SaveState> = _profileSaveState

    // ── Menu ─────────────────────────────────────────────────────────────
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    private val _menuSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val menuSaveState: StateFlow<SaveState> = _menuSaveState

    // ── Enquiries ────────────────────────────────────────────────────────
    private val _enquiries = MutableStateFlow<List<Enquiry>>(emptyList())
    val enquiries: StateFlow<List<Enquiry>> = _enquiries

    // ── Local Guide ──────────────────────────────────────────────────────
    private val _guides = MutableStateFlow<List<LocalPlace>>(emptyList())
    val guides: StateFlow<List<LocalPlace>> = _guides

    private val _guideSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val guideSaveState: StateFlow<SaveState> = _guideSaveState

    // ── Rooms ────────────────────────────────────────────────────────────
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    private val _roomSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val roomSaveState: StateFlow<SaveState> = _roomSaveState

    // ══════════════════════════════════════════════════════════════════════
    //  Initialise real-time listeners when a host logs in
    // ══════════════════════════════════════════════════════════════════════
    fun init(uid: String) {
        viewModelScope.launch { repo.getProfileFlow(uid).collect { _profile.value = it } }
        viewModelScope.launch { repo.getMenuFlow(uid).collect { _menuItems.value = it } }
        viewModelScope.launch { repo.getEnquiriesFlow(uid).collect { _enquiries.value = it } }
        viewModelScope.launch { repo.getGuidesFlow(uid).collect { _guides.value = it } }
        viewModelScope.launch { repo.getRoomsFlow(uid).collect { _rooms.value = it } }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Profile
    // ══════════════════════════════════════════════════════════════════════
    fun saveProfile(uid: String, profile: HostProfile) {
        viewModelScope.launch {
            _profileSaveState.value = SaveState.Loading
            val result = repo.saveProfile(uid, profile)
            _profileSaveState.value = if (result.isSuccess) SaveState.Success
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Save failed")
        }
    }

    fun updateAvailability(uid: String, available: Boolean, rate: String,
                           seasonalRate: String = "", blockedDates: List<String> = emptyList()) {
        viewModelScope.launch {
            repo.updateProfileFields(uid, mapOf(
                "available" to available,
                "nightlyRate" to rate,
                "seasonalRate" to seasonalRate,
                "blockedDates" to blockedDates
            ))
        }
    }

    // ── AI Description generation (FR-02) ─────────────────────────────
    private val _aiState = MutableStateFlow<SaveState>(SaveState.Idle)
    val aiState: StateFlow<SaveState> = _aiState

    fun generateAiDescription(uid: String, stayName: String, village: String, amenities: String) {
        viewModelScope.launch {
            _aiState.value = SaveState.Loading
            try {
                val prompt = "Write a warm 3-sentence description for a rural Indian home-stay called '$stayName' " +
                        "located in $village. Amenities: $amenities. Make it welcoming and authentic."
                val result = repo.callGeminiApi(prompt)
                if (result.isSuccess) {
                    val description = result.getOrThrow()
                    repo.updateProfileFields(uid, mapOf("aiDescription" to description))
                    _aiState.value = SaveState.Success
                } else {
                    _aiState.value = SaveState.Error(result.exceptionOrNull()?.message ?: "AI call failed")
                }
            } catch (e: Exception) {
                _aiState.value = SaveState.Error(e.message ?: "AI call failed")
            }
        }
    }

    // ── AI Dish name + description (FR-03) ────────────────────────────
    private val _dishAiState = MutableStateFlow<SaveState>(SaveState.Idle)
    val dishAiState: StateFlow<SaveState> = _dishAiState

    private val _aiDishSuggestion = MutableStateFlow<Pair<String,String>?>(null)
    val aiDishSuggestion: StateFlow<Pair<String,String>?> = _aiDishSuggestion

    fun generateDishSuggestion(mealType: String) {
        viewModelScope.launch {
            _dishAiState.value = SaveState.Loading
            try {
                 val prompt = "Suggest a typical Indian rural home-cooked $mealType dish name and a one-sentence " +
                        "description. Respond ONLY as JSON: {\"name\":\"...\",\"description\":\"...\"}"
                val result = repo.callGeminiApi(prompt)
                if (result.isSuccess) {
                    val json = org.json.JSONObject(result.getOrThrow().trim())
                    _aiDishSuggestion.value = Pair(
                        json.optString("name", ""),
                        json.optString("description", "")
                    )
                    _dishAiState.value = SaveState.Success
                } else {
                    _dishAiState.value = SaveState.Error("AI unavailable")
                }
            } catch (e: Exception) {
                _dishAiState.value = SaveState.Error(e.message ?: "AI failed")
            }
        }
    }

    fun resetAiState() { _aiState.value = SaveState.Idle }
    fun resetDishAiState() { _dishAiState.value = SaveState.Idle; _aiDishSuggestion.value = null }

    fun resetProfileSaveState() { _profileSaveState.value = SaveState.Idle }

    // ══════════════════════════════════════════════════════════════════════
    //  Menu
    // ══════════════════════════════════════════════════════════════════════
    fun addMenuItem(uid: String, item: MenuItem) {
        viewModelScope.launch {
            _menuSaveState.value = SaveState.Loading
            val result = repo.addMenuItem(uid, item)
            _menuSaveState.value = if (result.isSuccess) SaveState.Success
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Add failed")
        }
    }

    fun updateMenuItem(uid: String, item: MenuItem) {
        viewModelScope.launch {
            _menuSaveState.value = SaveState.Loading
            val result = repo.updateMenuItem(uid, item)
            _menuSaveState.value = if (result.isSuccess) SaveState.Success
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Update failed")
        }
    }

    fun deleteMenuItem(uid: String, itemId: String) {
        viewModelScope.launch { repo.deleteMenuItem(uid, itemId) }
    }

    fun resetMenuSaveState() { _menuSaveState.value = SaveState.Idle }

    // ══════════════════════════════════════════════════════════════════════
    //  Enquiries
    // ══════════════════════════════════════════════════════════════════════
    fun markEnquiryRead(enquiryId: String) {
        viewModelScope.launch { repo.updateEnquiryStatus(enquiryId, "read") }
    }

    fun markEnquiryReplied(enquiryId: String) {
        viewModelScope.launch { repo.updateEnquiryStatus(enquiryId, "replied") }
    }

    fun markEnquiryClosed(enquiryId: String) {
        viewModelScope.launch { repo.updateEnquiryStatus(enquiryId, "closed") }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Local Guide
    // ══════════════════════════════════════════════════════════════════════
    fun addLocalPlace(uid: String, place: LocalPlace) {
        viewModelScope.launch {
            _guideSaveState.value = SaveState.Loading
            val result = repo.addLocalPlace(uid, place)
            _guideSaveState.value = if (result.isSuccess) SaveState.Success
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Add failed")
        }
    }

    fun updateLocalPlace(uid: String, place: LocalPlace) {
        viewModelScope.launch {
            _guideSaveState.value = SaveState.Loading
            val result = repo.updateLocalPlace(uid, place)
            _guideSaveState.value = if (result.isSuccess) SaveState.Success
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Update failed")
        }
    }

    fun deleteLocalPlace(uid: String, placeId: String) {
        viewModelScope.launch { repo.deleteLocalPlace(uid, placeId) }
    }

    fun resetGuideSaveState() { _guideSaveState.value = SaveState.Idle }

    // ══════════════════════════════════════════════════════════════════════
    //  Rooms
    // ══════════════════════════════════════════════════════════════════════
    fun addRoom(uid: String, room: Room) {
        viewModelScope.launch {
            _roomSaveState.value = SaveState.Loading
            val result = repo.addRoom(uid, room)
            _roomSaveState.value = if (result.isSuccess) SaveState.Success
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Add failed")
        }
    }

    fun updateRoom(uid: String, room: Room) {
        viewModelScope.launch {
            _roomSaveState.value = SaveState.Loading
            val result = repo.updateRoom(uid, room)
            _roomSaveState.value = if (result.isSuccess) SaveState.Success
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Update failed")
        }
    }

    fun deleteRoom(uid: String, roomId: String) {
        viewModelScope.launch { repo.deleteRoom(uid, roomId) }
    }

    fun resetRoomSaveState() { _roomSaveState.value = SaveState.Idle }
}

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
