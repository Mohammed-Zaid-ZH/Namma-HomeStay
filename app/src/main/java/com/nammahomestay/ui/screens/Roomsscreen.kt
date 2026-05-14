package com.nammahomestay.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nammahomestay.data.Room
import com.nammahomestay.ui.components.MultiImagePicker
import com.nammahomestay.ui.components.uploadImageToFirebase
import com.nammahomestay.ui.theme.*
import com.nammahomestay.viewmodel.HomeStayViewModel
import com.nammahomestay.viewmodel.SaveState
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════
//  ROOMS LIST SCREEN
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    navController: NavController,
    homeStayViewModel: HomeStayViewModel,
    uid: String
) {
    val rooms by homeStayViewModel.rooms.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Rooms") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Button(
                onClick = { navController.navigate("add_room") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Room")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (rooms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Hotel, null,
                            tint = EarthBrown.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No rooms yet.\nAdd your first room with photos, price & capacity!",
                            color = EarthBrown.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(rooms, key = { it.id }) { room ->
                        RoomCard(
                            room = room,
                            onEdit = { navController.navigate("add_room?roomId=${room.id}") },
                            onDelete = { homeStayViewModel.deleteRoom(uid, room.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoomCard(room: Room, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // Photo strip
            if (room.imageUrls.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(room.imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(if (room.imageUrls.size == 1) 400.dp else 200.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(EarthCream),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Hotel, null, tint = EarthBrown.copy(alpha = 0.3f), modifier = Modifier.size(36.dp))
                }
            }

            // Info
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(room.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EarthBrown)
                        if (room.description.isNotBlank()) {
                            Text(room.description, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
                        }
                    }
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, null, tint = Color.LightGray)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (room.pricePerNight.isNotBlank()) {
                        Badge(containerColor = EarthGreen.copy(alpha = 0.15f)) {
                            Text(
                                "₹${room.pricePerNight}/night",
                                color = EarthGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Badge(containerColor = EarthCream) {
                        Icon(Icons.Default.People, null, tint = EarthBrown, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${room.capacity} guests",
                            color = EarthBrown,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp, top = 2.dp, bottom = 2.dp)
                        )
                    }
                    if (!room.available) {
                        Badge(containerColor = Color.Gray.copy(alpha = 0.15f)) {
                            Text("Not available", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Room?") },
            text = { Text("\"${room.name}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  ADD ROOM SCREEN
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomScreen(
    navController: NavController,
    homeStayViewModel: HomeStayViewModel,
    uid: String,
    roomId: String? = null
) {
    val rooms by homeStayViewModel.rooms.collectAsState()
    val editingRoom = remember(roomId, rooms) {
        rooms.find { it.id == roomId }
    }

    var roomName by remember(editingRoom) { mutableStateOf(editingRoom?.name ?: "") }
    var description by remember(editingRoom) { mutableStateOf(editingRoom?.description ?: "") }
    var pricePerNight by remember(editingRoom) { mutableStateOf(editingRoom?.pricePerNight ?: "") }
    var capacity by remember(editingRoom) { mutableStateOf(editingRoom?.capacity ?: 2) }
    var isAvailable by remember(editingRoom) { mutableStateOf(editingRoom?.available ?: true) }
    var localError by remember { mutableStateOf("") }

    // Images
    var savedPhotoUrls by remember(editingRoom) { mutableStateOf(editingRoom?.imageUrls ?: emptyList()) }
    var localImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploadingImages by remember { mutableStateOf(false) }

    val saveState by homeStayViewModel.roomSaveState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            homeStayViewModel.resetRoomSaveState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (roomId == null) "Add New Room" else "Edit Room") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Room Photos ────────────────────────────────────────────
            Text("Room Photos", style = MaterialTheme.typography.labelLarge, color = EarthBrown, fontWeight = FontWeight.Bold)
            Text("Add multiple photos — bed, bathroom, view, etc.", fontSize = 12.sp, color = Color.Gray)

            MultiImagePicker(
                label = "Add",
                savedUrls = savedPhotoUrls,
                localUris = localImageUris,
                onUriAdded = { uri -> localImageUris = localImageUris + uri },
                onSavedUrlRemoved = { url -> savedPhotoUrls = savedPhotoUrls - url },
                onLocalUriRemoved = { uri -> localImageUris = localImageUris - uri },
                imageSize = 110.dp
            )

            HorizontalDivider()

            // ── Room Details ───────────────────────────────────────────
            Text("Room Details", style = MaterialTheme.typography.labelLarge, color = EarthBrown, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Room Name (e.g. Forest Cottage, Deluxe Room)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pricePerNight,
                onValueChange = { pricePerNight = it },
                label = { Text("Price per Night") },
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Capacity stepper ───────────────────────────────────────
            Text("Guest Capacity", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (capacity > 1) capacity-- },
                    modifier = Modifier
                        .size(40.dp)
                        .background(EarthCream, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Remove, null, tint = EarthBrown)
                }
                Text(
                    "$capacity guests",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = EarthBrown,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                IconButton(
                    onClick = { if (capacity < 20) capacity++ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(EarthCream, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, null, tint = EarthBrown)
                }
            }

            // ── Availability toggle ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Available for booking", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isAvailable) "This room is visible to travellers" else "Hidden from travellers",
                        fontSize = 12.sp, color = Color.Gray
                    )
                }
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EarthGreen,
                        checkedTrackColor = EarthGreen.copy(alpha = 0.4f)
                    )
                )
            }

            if (localError.isNotEmpty()) {
                Text(localError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            if (saveState is SaveState.Error) {
                Text((saveState as SaveState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            val isBusy = saveState is SaveState.Loading || isUploadingImages
            Button(
                onClick = {
                    if (roomName.isBlank()) { localError = "Please enter a room name"; return@Button }
                    localError = ""
                    scope.launch {
                        val uploadedUrls = mutableListOf<String>()
                        if (localImageUris.isNotEmpty()) {
                            isUploadingImages = true
                            for (uri in localImageUris) {
                                val result = uploadImageToFirebase(uid, "rooms", uri)
                                if (result.isSuccess) {
                                    result.getOrNull()?.let { uploadedUrls.add(it) }
                                } else {
                                    localError = "Some images failed to upload, try again"
                                    isUploadingImages = false
                                    return@launch
                                }
                            }
                            isUploadingImages = false
                        }
                        val allPhotos = savedPhotoUrls + uploadedUrls
                        val room = Room(
                            id = roomId ?: "",
                            name = roomName.trim(),
                            description = description.trim(),
                            pricePerNight = pricePerNight.trim(),
                            capacity = capacity,
                            imageUrls = allPhotos,
                            available = isAvailable
                        )
                        if (roomId == null) {
                            homeStayViewModel.addRoom(uid, room)
                        } else {
                            homeStayViewModel.updateRoom(uid, room)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EarthBrown),
                enabled = !isBusy
            ) {
                if (isBusy) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Text(if (isUploadingImages) "Uploading photos…" else "Saving room…")
                    }
                } else {
                    Text(if (roomId == null) "Save Room" else "Save Changes", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
