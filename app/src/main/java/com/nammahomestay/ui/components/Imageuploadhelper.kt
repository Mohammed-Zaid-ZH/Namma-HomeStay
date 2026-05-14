package com.nammahomestay.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nammahomestay.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Uploads a single image URI to Firebase Storage and returns the download URL.
 * Path: images/{uid}/{folder}/{randomUUID}
 */
suspend fun uploadImageToFirebase(uid: String, folder: String, uri: Uri): Result<String> {
    return try {
        val ref = Firebase.storage.reference
            .child("images/$uid/$folder/${UUID.randomUUID()}")
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        Result.success(url)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ── Single image picker (for dish / local place) ──────────────────────────────
@Composable
fun SingleImagePicker(
    label: String = "Add Photo",
    currentUrl: String,          // existing URL already saved (empty if none)
    localUri: Uri?,              // locally picked but not yet saved
    onUriPicked: (Uri) -> Unit,
    onClearUrl: () -> Unit,      // called when user removes the existing saved URL
    imageSize: Dp = 100.dp
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onUriPicked(it) } }

    val hasImage = localUri != null || currentUrl.isNotBlank()

    Box(
        modifier = Modifier
            .size(imageSize)
            .clip(RoundedCornerShape(16.dp))
            .background(EarthCream)
            .border(1.5.dp, EarthBrown.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            AsyncImage(
                model = localUri ?: currentUrl,
                contentDescription = "Selected image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
            )
            // Remove button
            IconButton(
                onClick = {
                    if (localUri != null) onUriPicked(Uri.EMPTY) else onClearUrl()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, null, tint = EarthBrown.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                Text(label, fontSize = 10.sp, color = EarthBrown.copy(alpha = 0.5f))
            }
        }
    }
}

// ── Multi-image gallery picker (for homestay room photos / profile photos) ────
@Composable
fun MultiImagePicker(
    label: String = "Add Photos",
    savedUrls: List<String>,           // already-uploaded URLs
    localUris: List<Uri>,              // locally picked URIs pending upload
    onUriAdded: (Uri) -> Unit,
    onSavedUrlRemoved: (String) -> Unit,
    onLocalUriRemoved: (Uri) -> Unit,
    imageSize: Dp = 100.dp
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> uris.forEach { onUriAdded(it) } }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Already-saved images
        items(savedUrls) { url ->
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onSavedUrlRemoved(url) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        // Locally picked, not yet saved
        items(localUris) { uri ->
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onLocalUriRemoved(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        // "Add more" button
        item {
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EarthCream)
                    .border(1.5.dp, EarthBrown.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, null, tint = EarthBrown.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                    Text(label, fontSize = 10.sp, color = EarthBrown.copy(alpha = 0.5f))
                }
            }
        }
    }
}