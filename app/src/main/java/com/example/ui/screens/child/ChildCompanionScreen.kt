package com.example.ui.screens.child

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RequestType
import com.example.ui.components.ActiveMonitoringBanner
import com.example.ui.components.ScreenTimeGauge
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.theme.*
import com.example.viewmodel.ChildNavDestination
import com.example.viewmodel.SafeGuardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildCompanionScreen(
    state: SafeGuardUiState,
    onSubmitRequest: (RequestType, String, Int, String) -> Unit,
    onRequestUninstall: (String) -> Unit = {},
    onDismissUninstallNotice: () -> Unit = {},
    onTriggerSos: () -> Unit,
    onDismissSos: () -> Unit,
    onNavigate: (ChildNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val child = state.activeChild ?: return
    val context = androidx.compose.ui.platform.LocalContext.current
    var isDeviceAdminActive by remember {
        mutableStateOf(com.example.data.security.UninstallProtectionManager.isDeviceAdminActive(context))
    }
    val adminLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        isDeviceAdminActive = com.example.data.security.UninstallProtectionManager.isDeviceAdminActive(context)
    }

    var showRequestDialog by remember { mutableStateOf(false) }
    var showSosConfirmation by remember { mutableStateOf(false) }
    var showUninstallDialog by remember { mutableStateOf(false) }
    var uninstallReason by remember { mutableStateOf("") }
    var masterPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    if (state.isSosActive) {
        SosEmergencyDialog(child = child, onDismiss = onDismissSos)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Transparent Protection Notice
        item {
            ActiveMonitoringBanner(
                isChildMode = true,
                onPrivacyClick = { onNavigate(ChildNavDestination.PRIVACY) }
            )
        }

        // Child Greeting & Device Status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("card_child_greeting"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SafeGuardCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(child.avatarEmoji, fontSize = 28.sp)
                        }
                        Column {
                            Text(
                                text = "Hey, ${child.name}!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SafeGuard Companion • ${child.deviceModel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Battery
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SafeGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${child.batteryPct}% Battery",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SafeGreen
                        )
                    }
                }
            }
        }

        // Screen Time Remaining Gauge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("card_child_screen_time"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "My Screen Time Today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ScreenTimeGauge(
                        usedMinutes = child.todayUsedMinutes,
                        limitMinutes = child.dailyLimitMinutes,
                        isPaused = child.isDevicePaused
                    )

                    // Request Extra Time Button
                    Button(
                        onClick = { showRequestDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_child_request_time"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGuardCyan)
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Request More Time or App Unlock")
                    }
                }
            }
        }

        // Emergency SOS Panic Button Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("card_child_sos"),
                colors = CardDefaults.cardColors(containerColor = AlertRose.copy(alpha = 0.08f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AlertRose.copy(alpha = 0.4f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(AlertRose),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sos,
                                contentDescription = "Emergency SOS",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Emergency SOS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = AlertRose
                            )
                            Text(
                                text = "Instant alert with live GPS sent to your parents",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showSosConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRose),
                        modifier = Modifier.testTag("button_trigger_sos")
                    ) {
                        Text("SOS", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Today's Active Rules Breakdown
        item {
            Text(
                text = "Today's Active Rules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bedtime
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = PurpleAccent)
                        Column {
                            Text("Bedtime at 9:30 PM", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Phone calls & emergency apps remain open all night", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider()

                    // Location Sharing Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SafeGreen)
                        Column {
                            Text("Location Sharing is ON", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Sharing safely with Mom & Dad: ${child.locationAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider()

                    // Study Mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = SafeGuardCyan)
                        Column {
                            Text(
                                text = if (child.isStudyModeActive) "Study Mode is ACTIVE" else "Study Mode Scheduled",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Education apps (Duolingo, Khan Academy) are unrestricted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Uninstall Protection & Anti-Tamper Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("card_uninstall_protection_child"),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isUninstallAllowedByParent)
                        SafeGreen.copy(alpha = 0.08f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (state.isUninstallAllowedByParent) SafeGreen else SafeGuardBlue.copy(alpha = 0.3f)
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isUninstallAllowedByParent) Icons.Default.LockOpen else Icons.Default.EnhancedEncryption,
                                contentDescription = null,
                                tint = if (state.isUninstallAllowedByParent) SafeGreen else SafeGuardBlue
                            )
                            Text(
                                text = "Uninstall Protection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (state.isUninstallAllowedByParent)
                                SafeGreen.copy(alpha = 0.15f)
                            else
                                SafeGuardBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (state.isUninstallAllowedByParent) "UNLOCKED BY PARENT" else "LOCKED BY PARENT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.isUninstallAllowedByParent) SafeGreen else SafeGuardBlue,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Text(
                        text = if (state.isUninstallAllowedByParent)
                            "Your parent has granted permission to uninstall this app. You may deactivate device protection below."
                        else
                            "This app cannot be uninstalled from this phone until your parent allows it from their dashboard or enters the Guardian Master PIN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Device Administrator Status & Privilege Request
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Device Administrator",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isDeviceAdminActive)
                                        "Active: OS-level uninstall lock enforced"
                                    else
                                        "Not yet active: Tap to enforce lock",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDeviceAdminActive) SafeGreen else AlertRose
                                )
                            }

                            if (!isDeviceAdminActive) {
                                FilledTonalButton(
                                    onClick = {
                                        val intent = com.example.data.security.UninstallProtectionManager.createActivateAdminIntent(context)
                                        adminLauncher.launch(intent)
                                    },
                                    modifier = Modifier.testTag("button_child_request_admin")
                                ) {
                                    Text("Enforce Lock")
                                }
                            }
                        }
                    }

                    if (state.isUninstallAllowedByParent) {
                        Button(
                            onClick = {
                                val eligibility = com.example.data.security.UninstallProtectionManager.checkUninstallEligibility(context, child.id)
                                if (eligibility == com.example.data.security.UninstallStatus.PERMITTED) {
                                    com.example.data.security.UninstallProtectionManager.deactivateDeviceAdmin(context)
                                    com.example.data.security.UninstallProtectionManager.launchUninstallPrompt(context)
                                } else {
                                    showUninstallDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_deactivate_and_uninstall"),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Proceed with Uninstallation", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showUninstallDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_request_uninstall"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Uninstall Permission from Parent")
                        }
                    }
                }
            }
        }

        // Screen Viewing Transparency
        if (child.isScreenViewingActive) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SafeGreen.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SafeGreen)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CastConnected, contentDescription = null, tint = SafeGreen)
                        Column {
                            Text(
                                "Screen Sharing is Active",
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                            Text(
                                "Your parent can see your current screen. Transparent permission active.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    // Request Extra Time / App Unlock Dialog
    if (showRequestDialog) {
        var requestType by remember { mutableStateOf(RequestType.SCREEN_TIME_EXTENSION) }
        var targetName by remember { mutableStateOf("Homework & Project") }
        var requestedMinutes by remember { mutableIntStateOf(30) }
        var reason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitRequest(
                            requestType,
                            targetName.ifBlank { "Screen Time" },
                            requestedMinutes,
                            reason.ifBlank { "Need extra time for school work" }
                        )
                        showRequestDialog = false
                    },
                    modifier = Modifier.testTag("button_confirm_send_request"),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGuardCyan)
                ) {
                    Text("Send to Parent")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRequestDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Ask Your Parent") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("What would you like to request?")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = requestType == RequestType.SCREEN_TIME_EXTENSION,
                            onClick = {
                                requestType = RequestType.SCREEN_TIME_EXTENSION
                                targetName = "General Screen Time"
                            },
                            label = { Text("+ Time") }
                        )
                        FilterChip(
                            selected = requestType == RequestType.APP_UNLOCK,
                            onClick = {
                                requestType = RequestType.APP_UNLOCK
                                targetName = "Minecraft"
                            },
                            label = { Text("Unlock App") }
                        )
                        FilterChip(
                            selected = requestType == RequestType.WEBSITE_UNLOCK,
                            onClick = {
                                requestType = RequestType.WEBSITE_UNLOCK
                                targetName = "Website Access"
                            },
                            label = { Text("Unlock Site") }
                        )
                    }

                    OutlinedTextField(
                        value = targetName,
                        onValueChange = { targetName = it },
                        label = { Text("Target App / Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("How many extra minutes? $requestedMinutes mins")
                    Slider(
                        value = requestedMinutes.toFloat(),
                        onValueChange = { requestedMinutes = it.toInt() },
                        valueRange = 15f..120f,
                        steps = 6
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (e.g. studying for exam)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // SOS Confirmation Alert
    if (showSosConfirmation) {
        AlertDialog(
            onDismissRequest = { showSosConfirmation = false },
            confirmButton = {
                Button(
                    onClick = {
                        showSosConfirmation = false
                        onTriggerSos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRose),
                    modifier = Modifier.testTag("button_confirm_sos")
                ) {
                    Text("YES, SEND SOS NOW")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSosConfirmation = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRose)
                    Text("Send Emergency SOS?", color = AlertRose, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("This will immediately send an urgent distress alert with your live GPS location (${child.locationAddress}) and ring your parent's phone.")
            }
        )
    }

    // Uninstall Request Dialog
    if (showUninstallDialog) {
        var selectedMethod by remember { mutableStateOf(0) } // 0: Request Parent, 1: Enter Master PIN

        AlertDialog(
            onDismissRequest = { showUninstallDialog = false },
            confirmButton = {
                if (selectedMethod == 0) {
                    Button(
                        onClick = {
                            onRequestUninstall(uninstallReason.ifBlank { "Child companion requested app removal" })
                            showUninstallDialog = false
                            uninstallReason = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGuardBlue),
                        modifier = Modifier.testTag("button_submit_uninstall_request")
                    ) {
                        Text("Send Request to Parent")
                    }
                } else {
                    Button(
                        onClick = {
                            if (com.example.data.security.UninstallProtectionManager.verifyMasterPin(masterPinInput)) {
                                pinError = null
                                com.example.data.security.UninstallProtectionManager.deactivateDeviceAdmin(context)
                                showUninstallDialog = false
                                com.example.data.security.UninstallProtectionManager.launchUninstallPrompt(context)
                            } else {
                                pinError = "Incorrect Guardian Master PIN. Default is 2468."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        modifier = Modifier.testTag("button_verify_master_pin_uninstall")
                    ) {
                        Text("Unlock & Uninstall")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUninstallDialog = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SafeGuardBlue)
                    Text("Uninstall Protection")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "SafeGuard Kids is locked by Parent Guardian. To remove this app, either ask your parent to allow removal from their dashboard, or enter the Guardian Master PIN.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = selectedMethod == 0,
                            onClick = { selectedMethod = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Ask Parent", style = MaterialTheme.typography.labelSmall)
                        }
                        SegmentedButton(
                            selected = selectedMethod == 1,
                            onClick = { selectedMethod = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Guardian PIN", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (selectedMethod == 0) {
                        OutlinedTextField(
                            value = uninstallReason,
                            onValueChange = { uninstallReason = it },
                            label = { Text("Reason for removing app") },
                            placeholder = { Text("e.g., Changing phone or finished school term") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_uninstall_reason")
                        )
                    } else {
                        OutlinedTextField(
                            value = masterPinInput,
                            onValueChange = { masterPinInput = it },
                            label = { Text("Guardian Master PIN") },
                            placeholder = { Text("Default: 2468") },
                            singleLine = true,
                            isError = pinError != null,
                            supportingText = pinError?.let { { Text(it, color = AlertRose) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_guardian_master_pin")
                        )
                    }
                }
            }
        )
    }

    // Notice banner if uninstall requested
    state.uninstallAttemptNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = onDismissUninstallNotice,
            confirmButton = {
                Button(onClick = onDismissUninstallNotice) {
                    Text("OK, Understood")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SafeGuardBlue)
                    Text("Protection Notice")
                }
            },
            text = { Text(notice) }
        )
    }
}
