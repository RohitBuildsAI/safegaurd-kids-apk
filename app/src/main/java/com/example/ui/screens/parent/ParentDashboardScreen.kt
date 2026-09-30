package com.example.ui.screens.parent

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ParentNavDestination
import com.example.viewmodel.SafeGuardUiState

@Composable
fun ParentDashboardScreen(
    state: SafeGuardUiState,
    onTogglePause: (String) -> Unit,
    onToggleBedtime: (String) -> Unit,
    onToggleStudyMode: (String) -> Unit,
    onToggleScreenViewing: (String) -> Unit,
    onNavigate: (ParentNavDestination) -> Unit,
    onDismissSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val child = state.activeChild

    if (child == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SafeGuardBlue)
        }
        return
    }

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
        // Transparent Protection Info Banner
        item {
            ActiveMonitoringBanner(
                isChildMode = false,
                onPrivacyClick = { onNavigate(ParentNavDestination.PRIVACY) }
            )
        }

        // Active Child Status Summary Card
        item {
            DeviceStatusCard(child = child)
        }

        // Main Screen Time & Quick Pause Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("card_screen_time_summary"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Today's Screen Time",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Daily limit: ${child.dailyLimitMinutes / 60}h ${child.dailyLimitMinutes % 60}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = { onNavigate(ParentNavDestination.SCREEN_TIME) },
                            modifier = Modifier.testTag("button_manage_rules")
                        ) {
                            Text("Manage Rules")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ScreenTimeGauge(
                        usedMinutes = child.todayUsedMinutes,
                        limitMinutes = child.dailyLimitMinutes,
                        isPaused = child.isDevicePaused
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Mode Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pause Device Toggle
                        OutlinedButton(
                            onClick = { onTogglePause(child.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_quick_pause_device"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (child.isDevicePaused) AlertRose.copy(alpha = 0.12f) else Color.Transparent
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (child.isDevicePaused) AlertRose else MaterialTheme.colorScheme.outline
                                )
                            )
                        ) {
                            Icon(
                                imageVector = if (child.isDevicePaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = if (child.isDevicePaused) AlertRose else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (child.isDevicePaused) "Resume" else "Pause Device",
                                color = if (child.isDevicePaused) AlertRose else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Study Mode Toggle
                        OutlinedButton(
                            onClick = { onToggleStudyMode(child.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_quick_study_mode"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (child.isStudyModeActive) SafeGuardCyan.copy(alpha = 0.12f) else Color.Transparent
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (child.isStudyModeActive) SafeGuardCyan else MaterialTheme.colorScheme.outline
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = if (child.isStudyModeActive) SafeGuardCyan else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (child.isStudyModeActive) "Study Active" else "Study Mode",
                                color = if (child.isStudyModeActive) SafeGuardCyan else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bedtime & Transparent Screen Viewing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onToggleBedtime(child.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_quick_bedtime"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (child.isBedtimeActive) PurpleAccent.copy(alpha = 0.12f) else Color.Transparent
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (child.isBedtimeActive) PurpleAccent else MaterialTheme.colorScheme.outline
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = if (child.isBedtimeActive) PurpleAccent else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (child.isBedtimeActive) "Bedtime On" else "Bedtime",
                                color = if (child.isBedtimeActive) PurpleAccent else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onToggleScreenViewing(child.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_quick_screen_view"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (child.isScreenViewingActive) SafeGreen.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (child.isScreenViewingActive) SafeGreen else MaterialTheme.colorScheme.outline
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = null,
                                tint = if (child.isScreenViewingActive) SafeGreen else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (child.isScreenViewingActive) "Viewing Live" else "View Screen",
                                color = if (child.isScreenViewingActive) SafeGreen else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Live Transparent Screen Viewing Alert (when active)
        if (child.isScreenViewingActive) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SafeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Screen Viewing Stream Active",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                            Text(
                                text = "Consent indicator visible on child's device. MediaProjection session active.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onToggleScreenViewing(child.id) },
                            modifier = Modifier.testTag("button_stop_screen_view")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop stream",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Location Quick Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigate(ParentNavDestination.LOCATION) }
                    .testTag("card_dashboard_location"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SafeGuardCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = "Location",
                                tint = SafeGuardCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Current Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = child.locationAddress,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Safe zone active • Geofence verified",
                                style = MaterialTheme.typography.bodySmall,
                                color = SafeGreen
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Pending Requests Notification (if any)
        val pendingRequests = state.requests.filter { it.status == RequestStatus.PENDING }
        if (pendingRequests.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(ParentNavDestination.ALERTS) }
                        .testTag("card_pending_requests"),
                    colors = CardDefaults.cardColors(
                        containerColor = WarningAmber.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "${pendingRequests.size} Pending Child Request(s)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${pendingRequests.first().targetName}: ${pendingRequests.first().reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        TextButton(onClick = { onNavigate(ParentNavDestination.ALERTS) }) {
                            Text("Review", fontWeight = FontWeight.Bold, color = WarningAmber)
                        }
                    }
                }
            }
        }

        // Top Apps Usage Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Top App Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(ParentNavDestination.APPS) }) {
                    Text("View All Apps", color = SafeGuardCyan)
                }
            }
        }

        items(state.appRules.take(4)) { app ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = app.iconEmoji,
                            fontSize = 22.sp
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (app.isBlocked) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AlertRose.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Blocked",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AlertRose,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (app.isAlwaysAllowed) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SafeGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Always Allowed",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SafeGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${app.category} • ${app.usedMinutesToday} mins today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress bar
                    if (app.limitMinutes > 0) {
                        val appProgress = (app.usedMinutesToday.toFloat() / app.limitMinutes).coerceIn(0f, 1f)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${app.usedMinutesToday}/${app.limitMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { appProgress },
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (appProgress >= 1f) AlertRose else SafeGuardCyan,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Pair New Device Banner
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(ParentNavDestination.PAIRING) }
                    .testTag("card_pair_companion_device"),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SafeGuardBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = SafeGuardBlue
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pair Child Companion App",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate 6-digit secure pairing code or scan QR on child's phone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Pair",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
