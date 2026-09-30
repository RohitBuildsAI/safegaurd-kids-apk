package com.example.ui.screens.parent

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.SafeGuardUiState

@Composable
fun PrivacyCenterScreen(
    state: SafeGuardUiState,
    currentUser: com.google.firebase.auth.FirebaseUser? = null,
    onSignOut: () -> Unit = {},
    onToggleAllowUninstall: (String, Boolean) -> Unit = { _, _ -> },
    onExportData: () -> Unit,
    onClearExportData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val child = state.activeChild
    val context = androidx.compose.ui.platform.LocalContext.current
    var isDeviceAdminActive by remember {
        mutableStateOf(com.example.data.security.UninstallProtectionManager.isDeviceAdminActive(context))
    }
    val adminLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        isDeviceAdminActive = com.example.data.security.UninstallProtectionManager.isDeviceAdminActive(context)
    }

    var showPinChangeDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var pinChangeSuccess by remember { mutableStateOf<String?>(null) }
    var showRevokeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Privacy & Transparency Center",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SafeGuard Kids operates under strict ethical guidelines. No hidden surveillance, no keylogging.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Encryption & Security Badge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SafeGuardBlue.copy(alpha = 0.08f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SafeGuardBlue.copy(alpha = 0.3f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SafeGuardBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "End-to-End Encrypted Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "AES-256-GCM encryption with SHA-256 cryptographic audit verification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // What information is collected and why
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Data Collection Transparency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Allowed Items
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
                            Column {
                                Text("App Usage & Screen Time", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Used solely to enforce parent-defined healthy daily limits and sleep schedules.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Location & Geofencing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Shared with transparent live indicators for child safety and SOS alert response.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Web Filter Categories", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Filters mature content using official accessibility/browser APIs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    HorizontalDivider()

                    // Prohibited & Never Collected
                    Text(
                        text = "Strictly Prohibited & Never Collected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AlertRose
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "❌ Never keylog or capture private passwords",
                            "❌ Never record calls or activate camera/mic covertly",
                            "❌ Never read private chats, SMS, or private emails",
                            "❌ Never bypass lockscreen, PINs, or device root protections",
                            "❌ Never operate in stealth or hidden mode"
                        ).forEach { rule ->
                            Text(
                                text = rule,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Account Access & Data Retention
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Access & Retention",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Authorised Guardian", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = currentUser?.email ?: currentUser?.displayName ?: "Family Guardian Account",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = SafeGuardCyan.copy(alpha = 0.15f)) {
                            Text(
                                text = if (currentUser != null) "Verified Auth" else "Demo Mode",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeGuardCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (currentUser != null) {
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_privacy_sign_out"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRose)
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out of Firebase Account")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Audit Log Retention", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Automatically purged after retention period", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("30 Days Rolling", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Uninstall Protection & Anti-Tamper Configuration
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("card_parent_uninstall_protection"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SafeGuardBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SafeGuardBlue)
                        }
                        Column {
                            Text(
                                text = "Uninstall Protection (Anti-Tamper)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Prevent kids from uninstalling SafeGuard without parent approval",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider()

                    // Toggle: Lock App on Child Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Block App Removal on Child's Phone",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (!state.isUninstallAllowedByParent)
                                    "Locked: Child cannot uninstall without parent approval or Master PIN"
                                else
                                    "Unlocked: Temporary permission granted to uninstall",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (!state.isUninstallAllowedByParent) SafeGuardBlue else AlertRose
                            )
                        }

                        Switch(
                            checked = !state.isUninstallAllowedByParent,
                            onCheckedChange = { isLocked ->
                                child?.id?.let { cid ->
                                    onToggleAllowUninstall(cid, !isLocked)
                                }
                            },
                            modifier = Modifier.testTag("switch_lock_uninstall")
                        )
                    }

                    HorizontalDivider()

                    // Android Device Administrator Status & Activation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Android Device Administrator",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isDeviceAdminActive)
                                    "Active & Enforcing OS Protection"
                                else
                                    "Ready to Activate on Companion",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDeviceAdminActive) SafeGreen else SafeGuardBlue
                            )
                        }

                        if (!isDeviceAdminActive) {
                            Button(
                                onClick = {
                                    val intent = com.example.data.security.UninstallProtectionManager.createActivateAdminIntent(context)
                                    adminLauncher.launch(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SafeGuardBlue),
                                modifier = Modifier.testTag("button_activate_device_admin")
                            ) {
                                Text("Request Admin")
                            }
                        } else {
                            FilledTonalButton(
                                onClick = {},
                                enabled = false
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Active")
                            }
                        }
                    }

                    HorizontalDivider()

                    // Guardian Master PIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Guardian Master PIN",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Current default: 2468",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = { showPinChangeDialog = true },
                            modifier = Modifier.testTag("button_change_master_pin")
                        ) {
                            Text("Change PIN")
                        }
                    }
                }
            }
        }

        // Export Data Action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Export Personal Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Download or inspect all stored child profile data, rules, and telemetry in standardized JSON format.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onExportData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_export_personal_data"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGuardBlue)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate & Export Data (JSON)")
                    }

                    // Display exported JSON if present
                    state.exportedDataJson?.let { json ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = json,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            TextButton(onClick = onClearExportData) {
                                Text("Hide Export View")
                            }
                        }
                    }
                }
            }
        }

        // Danger Zone: Revoke Monitoring & Delete Account
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = AlertRose.copy(alpha = 0.08f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AlertRose.copy(alpha = 0.3f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Device Management & Revocation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlertRose
                    )
                    Text(
                        text = "Unpair device, revoke monitoring permissions, or permanently erase all telemetry logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRevokeDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_revoke_monitoring"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRose)
                        ) {
                            Text("Revoke Companion")
                        }

                        Button(
                            onClick = { showRevokeDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_erase_all_data"),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRose)
                        ) {
                            Text("Erase Logs")
                        }
                    }
                }
            }
        }
    }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            confirmButton = {
                Button(
                    onClick = { showRevokeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRose)
                ) {
                    Text("Confirm Revoke")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRevokeDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Revoke Device Link?") },
            text = {
                Text("This will immediately unpair ${child?.name ?: "the child"}'s device, release all screen limits, and delete local cached telemetry from the device.")
            }
        )
    }

    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinChangeDialog = false
                pinChangeSuccess = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length >= 4) {
                            com.example.data.security.UninstallProtectionManager.updateMasterPin(newPinText)
                            pinChangeSuccess = "Master PIN successfully updated!"
                            newPinText = ""
                        }
                    },
                    modifier = Modifier.testTag("button_save_new_pin"),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGuardBlue)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showPinChangeDialog = false
                    pinChangeSuccess = null
                }) {
                    Text("Close")
                }
            },
            title = { Text("Update Guardian Master PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter a new 4–6 digit security PIN for Guardian overrides and uninstall authorization:")
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { newPinText = it },
                        label = { Text("New Guardian PIN") },
                        placeholder = { Text("e.g. 5921") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    pinChangeSuccess?.let { msg ->
                        Text(msg, color = SafeGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}
