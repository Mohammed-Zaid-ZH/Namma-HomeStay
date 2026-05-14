package com.nammahomestay.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nammahomestay.R
import com.nammahomestay.data.HostProfile
import com.nammahomestay.data.LocalPlace
import android.content.Intent
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.nammahomestay.ui.components.MultiImagePicker
import com.nammahomestay.ui.components.SingleImagePicker
import com.nammahomestay.ui.components.uploadImageToFirebase
import com.nammahomestay.ui.theme.*
import com.nammahomestay.viewmodel.HomeStayViewModel
import com.nammahomestay.viewmodel.SaveState
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════
//  LOCAL GUIDE SCREEN
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalGuideScreen(
    navController: NavController,
    homeStayViewModel: HomeStayViewModel,
    uid: String
) {
    val guides by homeStayViewModel.guides.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_local_guide)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            val guidesCount by remember {
                derivedStateOf { guides.size }
            }
            Button(
                onClick = { if (guidesCount < 15) navController.navigate("add_place") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (guidesCount < 15) Terracotta else Color.Gray)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (guidesCount < 15) stringResource(R.string.btn_add_nearby_place) else "Limit reached (15/15)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (guides.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No places added yet.\nAdd waterfalls, viewpoints, farms nearby!",
                        color = EarthBrown.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(guides, key = { it.id }) { place ->
                        PlaceCard(
                            place = place,
                            onEdit = { navController.navigate("add_place?placeId=${place.id}") },
                            onDelete = { homeStayViewModel.deleteLocalPlace(uid, place.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceCard(place: LocalPlace, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Photo (if present)
            if (place.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = place.photoUrl,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )
            }
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (place.category) {
                        "Waterfall" -> "💧"; "Viewpoint" -> "🏔️"; "Farm" -> "🌾"
                        "Temple" -> "🛕"; "Beach" -> "🏖️"; "Market" -> "🛒"
                        else -> "📍"
                    },
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(place.name, fontWeight = FontWeight.Bold, color = EarthBrown)
                    Text(place.category, fontSize = 12.sp, color = Color.Gray)
                    if (place.distanceKm.isNotBlank()) {
                        Text("${place.distanceKm} km away", fontSize = 12.sp, color = EarthGreen)
                    }
                    if (place.note.isNotBlank()) {
                        Text(place.note, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, null, tint = Color.LightGray)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray)
                        }
                    }
                    if (place.name.isNotBlank()) {
                        val context = LocalContext.current
                        TextButton(
                            onClick = {
                                val uri = Uri.parse("geo:0,0?q=${Uri.encode(place.name)}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val webIntent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://maps.google.com/?q=${Uri.encode(place.name)}"))
                                    context.startActivity(webIntent)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Map, null, tint = Terracotta, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Maps", fontSize = 11.sp, color = Terracotta)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  ADD/EDIT PLACE SCREEN
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    navController: NavController,
    homeStayViewModel: HomeStayViewModel,
    uid: String,
    placeId: String? = null
) {
    val guides by homeStayViewModel.guides.collectAsState()
    val editingPlace = remember(placeId, guides) {
        guides.find { it.id == placeId }
    }

    var placeName by remember(editingPlace) { mutableStateOf(editingPlace?.name ?: "") }
    var category by remember(editingPlace) { mutableStateOf(editingPlace?.category ?: "Waterfall") }
    var distanceKm by remember(editingPlace) { mutableStateOf(editingPlace?.distanceKm ?: "") }
    var note by remember(editingPlace) { mutableStateOf(editingPlace?.note ?: "") }
    var localError by remember { mutableStateOf("") }

    // Image state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember(editingPlace) { mutableStateOf(editingPlace?.photoUrl ?: "") }
    var isUploadingImage by remember { mutableStateOf(false) }

    val saveState by homeStayViewModel.guideSaveState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            homeStayViewModel.resetGuideSaveState()
            navController.popBackStack()
        }
    }

    val categories = listOf("Waterfall", "Viewpoint", "Farm", "Temple", "Beach", "Market")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (placeId == null) stringResource(R.string.title_add_nearby_place) else "Edit Place") },
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
            // ── Place Photo ────────────────────────────────────────────
            Text("Place Photo", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
            SingleImagePicker(
                label = if (currentImageUrl.isEmpty()) "Add Photo" else "Change Photo",
                currentUrl = currentImageUrl,
                localUri = imageUri,
                onUriPicked = { uri -> imageUri = if (uri == Uri.EMPTY) null else uri },
                onClearUrl = { currentImageUrl = "" },
                imageSize = 120.dp
            )

            // ── Fields ─────────────────────────────────────────────────
            OutlinedTextField(
                value = placeName,
                onValueChange = { placeName = it },
                label = { Text(stringResource(R.string.label_place_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = distanceKm,
                onValueChange = { distanceKm = it },
                label = { Text("Distance (km)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.label_description)) },
                modifier = Modifier.fillMaxWidth()
            )

            // ── Category chips ─────────────────────────────────────────
            Text("Category", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
            categories.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Terracotta,
                                selectedLabelColor = White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (localError.isNotEmpty()) {
                Text(localError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            val isBusy = saveState is SaveState.Loading || isUploadingImage
            Button(
                onClick = {
                    if (placeName.isBlank()) { localError = "Please enter place name"; return@Button }
                    localError = ""
                    scope.launch {
                        var uploadedUrl = currentImageUrl
                        if (imageUri != null) {
                            isUploadingImage = true
                            val result = uploadImageToFirebase(uid, "guides", imageUri!!)
                            isUploadingImage = false
                            if (result.isSuccess) {
                                uploadedUrl = result.getOrDefault("")
                            } else {
                                localError = "Image upload failed, try again"
                                return@launch
                            }
                        }
                        val place = LocalPlace(
                            id = placeId ?: "",
                            name = placeName.trim(),
                            category = category,
                            distanceKm = distanceKm.trim(),
                            note = note.trim(),
                            photoUrl = uploadedUrl
                        )
                        if (placeId == null) {
                            homeStayViewModel.addLocalPlace(uid, place)
                        } else {
                            homeStayViewModel.updateLocalPlace(uid, place)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                enabled = !isBusy
            ) {
                if (isBusy) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Text(if (isUploadingImage) "Uploading photo…" else "Saving…")
                    }
                } else {
                    Text(if (placeId == null) stringResource(R.string.btn_save_place) else "Save Changes", fontSize = 16.sp)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  EDIT PROFILE SCREEN  (now with stay photo gallery)
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    homeStayViewModel: HomeStayViewModel,
    uid: String
) {
    val profileState by homeStayViewModel.profile.collectAsState()
    val saveState by homeStayViewModel.profileSaveState.collectAsState()
    val scope = rememberCoroutineScope()

    var fullName by remember(profileState) { mutableStateOf(profileState?.fullName ?: "") }
    var phone by remember(profileState) { mutableStateOf(profileState?.phone ?: "") }
    var stayName by remember(profileState) { mutableStateOf(profileState?.stayName ?: "") }
    var village by remember(profileState) { mutableStateOf(profileState?.village ?: "") }
    var district by remember(profileState) { mutableStateOf(profileState?.district ?: "") }
    var state by remember(profileState) { mutableStateOf(profileState?.state ?: "") }
    var pin by remember(profileState) { mutableStateOf(profileState?.pin ?: "") }

    // Amenities
    var hasBathroom by remember(profileState) { mutableStateOf(profileState?.hasAttachedBathroom ?: false) }
    var hasHotWater by remember(profileState) { mutableStateOf(profileState?.hasHotWater ?: false) }
    var hasMosNet by remember(profileState) { mutableStateOf(profileState?.hasMosquitoNet ?: false) }
    var hasFanAc by remember(profileState) { mutableStateOf(profileState?.hasFanAc ?: false) }
    var isVegKitchen by remember(profileState) { mutableStateOf(profileState?.isVegetarianKitchen ?: false) }
    var hasParking by remember(profileState) { mutableStateOf(profileState?.hasParking ?: false) }
    // Verification checklist
    var cleanlinessRating by remember(profileState) { mutableIntStateOf(profileState?.cleanlinessRating ?: 0) }
    var hasToiletHygiene by remember(profileState) { mutableStateOf(profileState?.hasToiletHygiene ?: false) }
    var hasSafeDrinkingWater by remember(profileState) { mutableStateOf(profileState?.hasSafeDrinkingWater ?: false) }
    var hasFirstAidKit by remember(profileState) { mutableStateOf(profileState?.hasFirstAidKit ?: false) }

    // Stay photos
    var savedPhotoUrls by remember(profileState) {
        mutableStateOf(profileState?.profilePhotos ?: emptyList())
    }
    var localPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploadingPhotos by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf("") }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            homeStayViewModel.resetProfileSaveState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_edit_profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Stay Photos Section ───────────────────────────────────
            item {
                Text("Stay Photos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EarthBrown)
                Text(
                    "Add photos of your rooms, common areas, surroundings",
                    fontSize = 12.sp, color = Color.Gray
                )
            }
            item {
                MultiImagePicker(
                    label = "Add",
                    savedUrls = savedPhotoUrls,
                    localUris = localPhotoUris,
                    onUriAdded = { uri -> localPhotoUris = localPhotoUris + uri },
                    onSavedUrlRemoved = { url -> savedPhotoUrls = savedPhotoUrls - url },
                    onLocalUriRemoved = { uri -> localPhotoUris = localPhotoUris - uri },
                    imageSize = 110.dp
                )
                if (uploadError.isNotEmpty()) {
                    Text(uploadError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }

            item { HorizontalDivider() }

            // ── Personal Info ─────────────────────────────────────────
            item {
                Text("Personal Info", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EarthBrown)
            }
            item {
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it },
                    label = { Text(stringResource(R.string.label_full_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.label_phone_number)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = stayName, onValueChange = { stayName = it },
                    label = { Text("HomeStay Name") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }

            item { HorizontalDivider() }

            // ── Location ──────────────────────────────────────────────
            item {
                Text("Location", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EarthBrown)
            }
            item {
                OutlinedTextField(
                    value = village, onValueChange = { village = it },
                    label = { Text("Village / Town") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = district, onValueChange = { district = it },
                    label = { Text("District") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = state, onValueChange = { state = it },
                    label = { Text("State") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = pin, onValueChange = { pin = it },
                    label = { Text("PIN Code") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }

            item { HorizontalDivider() }

            // ── Amenities ─────────────────────────────────────────────
            item {
                Text("Amenities", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EarthBrown)
            }
            item {
                AmenityRow("Attached Bathroom", hasBathroom) { hasBathroom = it }
                AmenityRow("Hot Water", hasHotWater) { hasHotWater = it }
                AmenityRow("Mosquito Net", hasMosNet) { hasMosNet = it }
                AmenityRow("Fan / AC", hasFanAc) { hasFanAc = it }
                AmenityRow("Vegetarian Kitchen", isVegKitchen) { isVegKitchen = it }
                AmenityRow("Parking Available", hasParking) { hasParking = it }
            }

            item { HorizontalDivider() }

            // ── Verification Checklist (FR-02) ────────────────────────
            item {
                Text("Verification Checklist", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EarthBrown)
                Text("Required before your listing goes live", fontSize = 12.sp, color = Color.Gray)
            }
            item {
                // Cleanliness Rating 1–5
                Text("Cleanliness Rating", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        FilterChip(
                            selected = cleanlinessRating == star,
                            onClick = { cleanlinessRating = star },
                            label = { Text("$star ★", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Terracotta,
                                selectedLabelColor = White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                AmenityRow("Toilet Hygiene Maintained", hasToiletHygiene) { hasToiletHygiene = it }
                AmenityRow("Safe Drinking Water Available", hasSafeDrinkingWater) { hasSafeDrinkingWater = it }
                AmenityRow("First-Aid Kit Present", hasFirstAidKit) { hasFirstAidKit = it }
            }

            if (saveState is SaveState.Error) {
                item {
                    Text(
                        (saveState as SaveState.Error).message,
                        color = MaterialTheme.colorScheme.error, fontSize = 13.sp
                    )
                }
            }

            item {
                val isBusy = saveState is SaveState.Loading || isUploadingPhotos
                Button(
                    onClick = {
                        scope.launch {
                            uploadError = ""
                            // Upload any new local URIs
                            val newUrls = mutableListOf<String>()
                            if (localPhotoUris.isNotEmpty()) {
                                isUploadingPhotos = true
                                for (uri in localPhotoUris) {
                                    val result = uploadImageToFirebase(uid, "profile", uri)
                                    if (result.isSuccess) {
                                        newUrls.add(result.getOrDefault(""))
                                    } else {
                                        uploadError = "Some photos failed to upload"
                                    }
                                }
                                isUploadingPhotos = false
                            }
                            val allPhotos = savedPhotoUrls + newUrls
                            val updated = (profileState ?: HostProfile()).copy(
                                id = uid,
                                fullName = fullName.trim(),
                                phone = phone.trim(),
                                stayName = stayName.trim(),
                                village = village.trim(),
                                district = district.trim(),
                                state = state.trim(),
                                pin = pin.trim(),
                                profilePhotos = allPhotos,
                                hasAttachedBathroom = hasBathroom,
                                hasHotWater = hasHotWater,
                                hasMosquitoNet = hasMosNet,
                                hasFanAc = hasFanAc,
                                isVegetarianKitchen = isVegKitchen,
                                hasParking = hasParking,
                                cleanlinessRating = cleanlinessRating,
                                hasToiletHygiene = hasToiletHygiene,
                                hasSafeDrinkingWater = hasSafeDrinkingWater,
                                hasFirstAidKit = hasFirstAidKit
                            )
                            homeStayViewModel.saveProfile(uid, updated)
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
                            Text(if (isUploadingPhotos) "Uploading photos…" else "Saving…")
                        }
                    } else {
                        Text(stringResource(R.string.btn_update_profile), fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AmenityRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = Terracotta, checkedTrackColor = Terracotta.copy(alpha = 0.4f))
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  AVAILABILITY & PRICING SCREEN  (unchanged)
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    navController: NavController,
    homeStayViewModel: HomeStayViewModel,
    uid: String
) {
    val profile by homeStayViewModel.profile.collectAsState()

    var isAvailable by remember(profile) { mutableStateOf(profile?.available ?: true) }
    var rate by remember(profile) { mutableStateOf(profile?.nightlyRate ?: "") }
    var seasonalRate by remember(profile) { mutableStateOf(profile?.seasonalRate ?: "") }
    var blockedDates by remember(profile) { mutableStateOf(profile?.blockedDates ?: emptyList<String>()) }
    var newBlockedDate by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_pricing_status)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.label_host_discovery), fontWeight = FontWeight.SemiBold)
                        Text(
                            if (isAvailable) "Your stay is visible to travellers"
                            else "Your stay is hidden from travellers",
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
            }

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it; saved = false },
                label = { Text(stringResource(R.string.label_price_per_night)) },
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = seasonalRate,
                onValueChange = { seasonalRate = it; saved = false },
                label = { Text("Seasonal / Festival Rate (₹) — optional") },
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                placeholder = { Text("e.g. higher rate during Diwali / Christmas") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Blocked dates display
            if (blockedDates.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Blocked Dates", fontWeight = FontWeight.SemiBold, color = EarthBrown)
                        Spacer(modifier = Modifier.height(6.dp))
                        blockedDates.forEach { date ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(date, fontSize = 13.sp)
                                IconButton(
                                    onClick = { blockedDates = blockedDates - date },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Add blocked date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newBlockedDate,
                    onValueChange = { newBlockedDate = it },
                    label = { Text("Block a Date (dd/MM/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val d = newBlockedDate.trim()
                        if (d.isNotBlank() && !blockedDates.contains(d)) {
                            blockedDates = blockedDates + d
                            newBlockedDate = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Terracotta, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, null, tint = White)
                }
            }

            if (saved) {
                Text("✓ Changes saved!", color = EarthGreen, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    homeStayViewModel.updateAvailability(uid, isAvailable, rate.trim(), seasonalRate.trim(), blockedDates)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EarthBrown)
            ) {
                Text(stringResource(R.string.btn_save_changes), fontSize = 16.sp)
            }
        }
    }
}
