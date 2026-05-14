package com.nammahomestay.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nammahomestay.R
import com.nammahomestay.data.Enquiry
import com.nammahomestay.data.MenuItem
import com.nammahomestay.data.Room
import com.nammahomestay.ui.theme.*
import com.nammahomestay.viewmodel.AuthViewModel
import com.nammahomestay.viewmodel.HomeStayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    homeStayViewModel: HomeStayViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val uid = authViewModel.currentUid ?: ""

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_main_app),
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown
                    )
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.content_desc_logout),
                            tint = Terracotta
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = White, tonalElevation = 8.dp) {
                val items = listOf(
                    Triple(stringResource(R.string.nav_home), Icons.Default.Home, 0),
                    Triple(stringResource(R.string.nav_menu), Icons.Default.Restaurant, 1),
                    Triple(stringResource(R.string.nav_enquiries), Icons.AutoMirrored.Filled.Message, 2),
                    Triple(stringResource(R.string.nav_settings), Icons.Default.Settings, 3)
                )
                items.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Terracotta,
                            selectedTextColor = Terracotta,
                            indicatorColor = EarthCream
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(EarthCream)
        ) {
            when (selectedTab) {
                0 -> HomeScreenContent(navController, homeStayViewModel, uid)
                1 -> MenuScreenContent(navController, homeStayViewModel, uid)
                2 -> EnquiryScreenContent(homeStayViewModel)
                3 -> SettingsScreenContent(navController)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  HOME TAB
// ══════════════════════════════════════════════════════════════════════
@Composable
fun HomeScreenContent(
    navController: NavController,
    vm: HomeStayViewModel,
    uid: String = ""
) {
    val profile by vm.profile.collectAsState()
    val rooms by vm.rooms.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            // Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = EarthBrown)
            ) {
                Column {
                    // Stay photos horizontal strip
                    val photos = profile?.profilePhotos ?: emptyList()
                    if (photos.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        ) {
                            items(photos) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(260.dp)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                                .background(EarthBrown),
                        )
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            profile?.stayName?.ifBlank { "Your HomeStay" } ?: "Your HomeStay",
                            color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            listOfNotNull(profile?.village, profile?.district)
                                .joinToString(", ")
                                .ifBlank { "Karnataka" },
                            color = White.copy(alpha = 0.6f), fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val available = profile?.available ?: true
                            Badge(
                                containerColor = if (available) EarthGreen else Color.Gray
                            ) {
                                Text(
                                    if (available) "Available ✓" else "Not Available",
                                    color = White,
                                    modifier = Modifier.padding(4.dp),
                                    fontSize = 11.sp
                                )
                            }
                            if (profile?.nightlyRate?.isNotBlank() == true) {
                                Badge(containerColor = White.copy(alpha = 0.15f)) {
                                    Text(
                                        "₹${profile?.nightlyRate}/night",
                                        color = White,
                                        modifier = Modifier.padding(4.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            Text(
                stringResource(R.string.title_stay_details),
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EarthBrown
            )
        }

        profile?.let { p ->
            if (p.fullName.isNotBlank()) {
                item { DetailListItem("Host", p.fullName, Icons.Default.Person) }
            }
            if (p.phone.isNotBlank()) {
                item { DetailListItem("Phone", p.phone, Icons.Default.Phone) }
            }
            if (p.nightlyRate.isNotBlank()) {
                item { DetailListItem("Rate", "₹${p.nightlyRate} per night", Icons.Default.CurrencyRupee) }
            }
            val amenities = buildList {
                if (p.hasAttachedBathroom) add("Attached Bathroom")
                if (p.hasHotWater) add("Hot Water")
                if (p.hasFanAc) add("Fan/AC")
                if (p.isVegetarianKitchen) add("Veg Kitchen")
                if (p.hasParking) add("Parking")
            }
            if (amenities.isNotEmpty()) {
                item { DetailListItem("Amenities", amenities.joinToString(" · "), Icons.Default.CheckCircle) }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (profile?.aiDescription?.isNotBlank() == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Terracotta)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Description", fontWeight = FontWeight.Bold, color = EarthBrown)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(profile?.aiDescription ?: "", fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            } else {
                val aiState by vm.aiState.collectAsState()
                Button(
                    onClick = {
                        val p = profile
                        if (p != null && uid.isNotBlank()) {
                            val amenities = buildList {
                                if (p.hasAttachedBathroom) add("attached bathroom")
                                if (p.hasHotWater) add("hot water")
                                if (p.hasFanAc) add("fan/AC")
                                if (p.isVegetarianKitchen) add("vegetarian kitchen")
                                if (p.hasParking) add("parking")
                            }.joinToString(", ").ifBlank { "basic amenities" }
                            vm.generateAiDescription(uid, p.stayName.ifBlank { "HomeStay" }, p.village.ifBlank { "Karnataka" }, amenities)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(2.dp, EarthBrown.copy(alpha = 0.1f)),
                    enabled = aiState !is com.nammahomestay.viewmodel.SaveState.Loading
                ) {
                    if (aiState is com.nammahomestay.viewmodel.SaveState.Loading) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            Text("Generating AI Description…", color = EarthBrown)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Terracotta)
                            Text(stringResource(R.string.btn_generate_ai), color = EarthBrown, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ── Rooms Section ──────────────────────────────────────────────
        if (rooms.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rooms",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EarthBrown
                    )
                    TextButton(onClick = { navController.navigate("rooms") }) {
                        Text("Manage", color = Terracotta, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(rooms) { room ->
                HomeRoomCard(room = room, onEdit = { navController.navigate("add_room?roomId=${room.id}") })
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { navController.navigate("rooms") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Terracotta.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Hotel, null, tint = Terracotta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add rooms to your stay", color = Terracotta, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DetailListItem(label: String, value: String, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(value, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null, tint = Terracotta) },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
    )
}

@Composable
fun HomeRoomCard(room: Room, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column {
            // Photo strip
            if (room.imageUrls.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(room.imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(if (room.imageUrls.size == 1) 600.dp else 240.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(EarthCream),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Hotel, null,
                        tint = EarthBrown.copy(alpha = 0.25f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Details
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        room.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = EarthBrown,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }
                if (room.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(room.description, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (room.pricePerNight.isNotBlank()) {
                        Badge(containerColor = EarthGreen.copy(alpha = 0.12f)) {
                            Text(
                                "₹${room.pricePerNight} / night",
                                color = EarthGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Badge(containerColor = EarthCream) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.People, null, tint = EarthBrown, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            val guestText = if (room.capacity == 1) "guest" else "guests"
                            Text(
                                "${room.capacity} $guestText",
                                color = EarthBrown,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (!room.available) {
                        Badge(containerColor = Color.Gray.copy(alpha = 0.12f)) {
                            Text(
                                "Not available",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  MENU TAB
// ══════════════════════════════════════════════════════════════════════
@Composable
fun MenuScreenContent(
    navController: NavController,
    vm: HomeStayViewModel,
    uid: String
) {
    val menuItems by vm.menuItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.title_local_menu),
                fontSize = 24.sp, fontWeight = FontWeight.Bold, color = EarthBrown
            )
            Button(
                onClick = { navController.navigate("add_menu") },
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Text(stringResource(R.string.btn_add_item))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (menuItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Restaurant, null,
                        tint = EarthBrown.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No dishes yet. Add your first dish!", color = EarthBrown.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(menuItems, key = { it.id }) { item ->
                    MenuItemCard(
                        item = item,
                        onEdit = { navController.navigate("add_menu?itemId=${item.id}") },
                        onDelete = { vm.deleteMenuItem(uid, item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EarthCream),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.dishName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Text(
                        text = when (item.mealType) {
                            "Breakfast" -> "🌅"
                            "Dinner" -> "🌙"
                            else -> "☀️"
                        },
                        fontSize = 32.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.dishName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EarthBrown)
                if (item.description.isNotBlank()) {
                    Text(item.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.price.isNotBlank()) {
                        Text("₹${item.price}", fontWeight = FontWeight.Bold, color = EarthGreen, fontSize = 13.sp)
                    }
                    Badge(containerColor = EarthCream) {
                        Text(item.mealType, color = EarthBrown, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = Color.LightGray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.LightGray)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  ENQUIRIES TAB
// ══════════════════════════════════════════════════════════════════════
@Composable
fun EnquiryScreenContent(vm: HomeStayViewModel) {
    val enquiries by vm.enquiries.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            stringResource(R.string.title_pending_enquiries),
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = EarthBrown
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (enquiries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Filled.Message, null,
                        tint = EarthBrown.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No enquiries yet.\nYour listing needs to be shared with travellers.",
                        color = EarthBrown.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(enquiries, key = { it.id }) { enquiry ->
                    EnquiryCard(
                        enquiry = enquiry,
                        onCall = {
                            vm.markEnquiryRead(enquiry.id)
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${enquiry.travellerPhone}")
                            }
                            context.startActivity(intent)
                        },
                        onReply = { vm.markEnquiryReplied(enquiry.id) },
                        onClose = { vm.markEnquiryClosed(enquiry.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EnquiryCard(enquiry: Enquiry, onCall: () -> Unit, onReply: () -> Unit, onClose: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(EarthCream, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        enquiry.travellerName.firstOrNull()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(enquiry.travellerName, fontWeight = FontWeight.Bold)
                    if (enquiry.travellerPhone.isNotBlank()) {
                        Text(enquiry.travellerPhone, fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                val statusColor = when (enquiry.status) {
                    "new" -> Terracotta
                    "read" -> EarthGreen
                    else -> Color.Gray
                }
                Text(
                    enquiry.status.uppercase(),
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            if (enquiry.message.isNotBlank()) {
                Text(
                    enquiry.message,
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Show date if available
            enquiry.createdAt?.let { ts ->
                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                Text(
                    sdf.format(ts.toDate()),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EarthBrown)
                ) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_call_traveller))
                }
                if (enquiry.status != "replied" && enquiry.status != "closed") {
                    OutlinedButton(onClick = onReply) {
                        Text("Replied")
                    }
                }
                if (enquiry.status != "closed") {
                    OutlinedButton(onClick = onClose) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  SETTINGS TAB
// ══════════════════════════════════════════════════════════════════════
@Composable
fun SettingsScreenContent(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.title_account_settings),
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = EarthBrown
        )

        SettingsItem(stringResource(R.string.item_edit_profile), Icons.Default.Person) {
            navController.navigate("edit_profile")
        }
        SettingsItem("Manage Rooms", Icons.Default.Hotel) {
            navController.navigate("rooms")
        }
        SettingsItem(stringResource(R.string.item_manage_places), Icons.Default.Map) {
            navController.navigate("local_guide")
        }
        SettingsItem(stringResource(R.string.item_availability), Icons.Default.CalendarToday) {
            navController.navigate("availability")
        }
        SettingsItem("Notification Preferences", Icons.Default.Notifications) {
            navController.navigate("notifications")
        }
        SettingsItem("Delete Account", Icons.Default.DeleteForever) {
            navController.navigate("delete_account")
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = White),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = EarthBrown)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = EarthBrown, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
