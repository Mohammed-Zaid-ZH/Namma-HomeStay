package com.nammahomestay.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nammahomestay.R
import com.nammahomestay.data.MenuItem
import com.nammahomestay.ui.components.SingleImagePicker
import com.nammahomestay.ui.components.uploadImageToFirebase
import com.nammahomestay.ui.theme.*
import com.nammahomestay.viewmodel.HomeStayViewModel
import com.nammahomestay.viewmodel.SaveState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuScreen(
    navController: NavController,
    homeStayViewModel: HomeStayViewModel,
    uid: String,
    itemId: String? = null
) {
    val menuItems by homeStayViewModel.menuItems.collectAsState()
    val editingItem = remember(itemId, menuItems) {
        menuItems.find { it.id == itemId }
    }

    var dishName by remember(editingItem) { mutableStateOf(editingItem?.dishName ?: "") }
    var description by remember(editingItem) { mutableStateOf(editingItem?.description ?: "") }
    var price by remember(editingItem) { mutableStateOf(editingItem?.price ?: "") }
    var selectedMealType by remember(editingItem) { mutableStateOf(editingItem?.mealType ?: "Lunch") }
    var localError by remember { mutableStateOf("") }

    // Image state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember(editingItem) { mutableStateOf(editingItem?.imageUrl ?: "") }
    var isUploadingImage by remember { mutableStateOf(false) }

    val saveState by homeStayViewModel.menuSaveState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            homeStayViewModel.resetMenuSaveState()
            navController.popBackStack()
        }
    }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) stringResource(R.string.title_add_new_dish) else "Edit Dish") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Dish Photo ────────────────────────────────────────────────
            Text("Dish Photo", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
            SingleImagePicker(
                label = if (currentImageUrl.isEmpty()) "Add Photo" else "Change Photo",
                currentUrl = currentImageUrl,
                localUri = imageUri,
                onUriPicked = { uri ->
                    imageUri = if (uri == Uri.EMPTY) null else uri
                },
                onClearUrl = { currentImageUrl = "" },
                imageSize = 120.dp
            )

            // ── Fields ────────────────────────────────────────────────────
            OutlinedTextField(
                value = dishName,
                onValueChange = { dishName = it },
                label = { Text(stringResource(R.string.label_dish_name)) },
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
                value = price,
                onValueChange = { price = it },
                label = { Text(stringResource(R.string.label_price_rupees)) },
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Meal type selector ────────────────────────────────────────
            Text("Meal Type", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mealTypes.forEach { type ->
                    FilterChip(
                        selected = selectedMealType == type,
                        onClick = { selectedMealType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Terracotta,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            // ── Errors ────────────────────────────────────────────────────
            if (localError.isNotEmpty()) {
                Text(localError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            if (saveState is SaveState.Error) {
                Text(
                    (saveState as SaveState.Error).message,
                    color = MaterialTheme.colorScheme.error, fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save button ────────────────────────────────────────────
            val isBusy = saveState is SaveState.Loading || isUploadingImage
            Button(
                onClick = {
                    if (dishName.isBlank()) {
                        localError = "Please enter a dish name"
                        return@Button
                    }
                    localError = ""
                    scope.launch {
                        // Upload image first if one was picked
                        var uploadedUrl = currentImageUrl
                        if (imageUri != null) {
                            isUploadingImage = true
                            val result = uploadImageToFirebase(uid, "menu", imageUri!!)
                            isUploadingImage = false
                            if (result.isSuccess) {
                                uploadedUrl = result.getOrDefault("")
                            } else {
                                localError = "Image upload failed, try again"
                                return@launch
                            }
                        }
                        val item = MenuItem(
                            id = itemId ?: "",
                            dishName = dishName.trim(),
                            description = description.trim(),
                            price = price.trim(),
                            mealType = selectedMealType,
                            imageUrl = uploadedUrl,
                            active = true
                        )
                        if (itemId == null) {
                            homeStayViewModel.addMenuItem(uid, item)
                        } else {
                            homeStayViewModel.updateMenuItem(uid, item)
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
                        Text(if (isUploadingImage) "Uploading photo…" else "Saving…", fontSize = 16.sp)
                    }
                } else {
                    Text(if (itemId == null) stringResource(R.string.btn_publish_dish) else "Save Changes", fontSize = 18.sp)
                }
            }
        }
    }
}
