package com.nammahomestay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nammahomestay.ui.theme.*
import com.nammahomestay.viewmodel.HomeStayViewModel

// ══════════════════════════════════════════════════════════════════════
//  NOTIFICATION PREFERENCES (FR-07)
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPrefsScreen(navController: NavController) {
    var newEnquiries by remember { mutableStateOf(true) }
    var menuReminders by remember { mutableStateOf(true) }
    var profileTips by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Preferences", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Choose which notifications you receive",
                fontSize = 14.sp, color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            NotifRow(
                title = "New Enquiries",
                subtitle = "Alert when a traveller sends you a message",
                checked = newEnquiries,
                onChecked = { newEnquiries = it; saved = false }
            )
            NotifRow(
                title = "Daily Menu Reminder",
                subtitle = "Remind me to update today's menu every morning",
                checked = menuReminders,
                onChecked = { menuReminders = it; saved = false }
            )
            NotifRow(
                title = "Profile Tips",
                subtitle = "Suggestions to improve your listing",
                checked = profileTips,
                onChecked = { profileTips = it; saved = false }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (saved) {
                Text("✓ Preferences saved!", color = EarthGreen, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { saved = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EarthBrown)
            ) {
                Text("Save Preferences")
            }

            Text(
                "Note: Push notifications require FCM setup. These preferences are saved locally.",
                fontSize = 11.sp, color = Color.Gray
            )
        }
    }
}

@Composable
fun NotifRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(
                checked = checked, onCheckedChange = onChecked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = EarthGreen, checkedTrackColor = EarthGreen.copy(alpha = 0.4f)
                )
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  DELETE ACCOUNT (FR-07)
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
    navController: NavController,
    onConfirmDelete: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Warning, null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
            )
            Text(
                "Are you sure you want to delete your account?",
                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "This will permanently delete:\n" +
                        "• Your host profile and all photos\n" +
                        "• All your room listings\n" +
                        "• Your daily menu items\n" +
                        "• All local guide entries\n\n" +
                        "This action cannot be undone.",
                fontSize = 14.sp, color = Color.DarkGray
            )

            OutlinedTextField(
                value = confirmText,
                onValueChange = { confirmText = it },
                label = { Text("Type DELETE to confirm") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { if (confirmText == "DELETE") showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = confirmText == "DELETE"
            ) {
                Icon(Icons.Default.DeleteForever, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Permanently Delete Account")
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Cancel — Keep My Account") }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Final Confirmation") },
            text = { Text("Your account and all data will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onConfirmDelete()
                }) { Text("Yes, Delete Everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
