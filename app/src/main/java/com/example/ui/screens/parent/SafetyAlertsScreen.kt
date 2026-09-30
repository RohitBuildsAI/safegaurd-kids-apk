package com.example.ui.screens.parent

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.SafeGuardUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyAlertsScreen(
    state: SafeGuardUiState,
    onMarkAlertRead: (Long) -> Unit,
    onMarkAllAlertsRead: () -> Unit,
    onRespondToRequest: (Long, Boolean, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubtab by remember { mutableIntStateOf(0) } // 0 = Alerts, 1 = Requests
    var respondingRequest by remember { mutableStateOf<AccessRequestEntity?>(null) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a • MMM dd", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Alerts & Family Requests",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time safety telemetry, geofence triggers, and incoming permission requests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedSubtab,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedSubtab == 0,
                        onClick = { selectedSubtab = 0 },
                        text = {
                            val unread = state.alerts.count { !it.isRead }
                            Text("Safety Alerts" + if (unread > 0) " ($unread)" else "", fontWeight = FontWeight.Bold)
                        },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        modifier = Modifier.testTag("tab_alerts")
                    )
                    Tab(
                        selected = selectedSubtab == 1,
                        onClick = { selectedSubtab = 1 },
                        text = {
                            val pending = state.requests.count { it.status == RequestStatus.PENDING }
                            Text("Requests" + if (pending > 0) " ($pending)" else "", fontWeight = FontWeight.Bold)
                        },
                        icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                        modifier = Modifier.testTag("tab_requests")
                    )
                }
            }
        }

        if (selectedSubtab == 0) {
            // ALERTS SUBTAB
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Event Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = onMarkAllAlertsRead,
                        modifier = Modifier.testTag("button_mark_all_read")
                    ) {
                        Text("Mark All Read")
                    }
                }
            }

            if (state.alerts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No alerts at this time. All devices secure.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(state.alerts) { alert ->
                    val alertColor = when (alert.severity) {
                        AlertSeverity.CRITICAL -> AlertRose
                        AlertSeverity.WARNING -> WarningAmber
                        AlertSeverity.INFO -> SafeGuardCyan
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .testTag("card_alert_${alert.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!alert.isRead) alertColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (!alert.isRead) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(alertColor.copy(alpha = 0.4f))
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(alertColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (alert.type) {
                                        AlertType.EMERGENCY_SOS -> Icons.Default.Warning
                                        AlertType.EXCESSIVE_SCREEN_TIME -> Icons.Default.HourglassBottom
                                        AlertType.GEOFENCE_ENTER, AlertType.GEOFENCE_EXIT -> Icons.Default.LocationOn
                                        AlertType.LOW_BATTERY -> Icons.Default.BatteryAlert
                                        AlertType.BLOCKED_WEBSITE_ATTEMPT -> Icons.Default.Block
                                        AlertType.NEW_APP_INSTALLED -> Icons.Default.AppRegistration
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = alertColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alert.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alert.severity == AlertSeverity.CRITICAL) AlertRose else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = timeFormat.format(Date(alert.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!alert.isRead) {
                                IconButton(
                                    onClick = { onMarkAlertRead(alert.id) },
                                    modifier = Modifier.testTag("button_read_alert_${alert.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Mark as read",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // REQUESTS SUBTAB
            item {
                Text(
                    text = "Incoming Permission & Screen-Time Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.requests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No pending requests from children.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(state.requests) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("card_request_${request.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = when (request.status) {
                                RequestStatus.PENDING -> WarningAmber.copy(alpha = 0.08f)
                                RequestStatus.APPROVED -> SafeGreen.copy(alpha = 0.08f)
                                RequestStatus.REJECTED -> MaterialTheme.colorScheme.surface
                            }
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SafeGuardCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (request.type) {
                                                RequestType.SCREEN_TIME_EXTENSION -> Icons.Default.HourglassTop
                                                RequestType.APP_UNLOCK -> Icons.Default.LockOpen
                                                RequestType.WEBSITE_UNLOCK -> Icons.Default.Language
                                                RequestType.PERMISSION_REQUEST -> Icons.Default.Security
                                            },
                                            contentDescription = null,
                                            tint = SafeGuardCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = request.targetName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${request.requestedMinutes} minutes requested",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (request.status) {
                                        RequestStatus.PENDING -> WarningAmber.copy(alpha = 0.2f)
                                        RequestStatus.APPROVED -> SafeGreen.copy(alpha = 0.2f)
                                        RequestStatus.REJECTED -> AlertRose.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = request.status.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (request.status) {
                                            RequestStatus.PENDING -> WarningAmber
                                            RequestStatus.APPROVED -> SafeGreen
                                            RequestStatus.REJECTED -> AlertRose
                                        }
                                    )
                                }
                            }

                            // Child's reason note
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = "“${request.reason}”",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            // Action buttons if PENDING
                            if (request.status == RequestStatus.PENDING) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onRespondToRequest(request.id, true, request.requestedMinutes, "Approved by parent")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("button_approve_req_${request.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onRespondToRequest(request.id, false, 0, "Limit reached for today")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("button_decline_req_${request.id}"),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRose)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Decline")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
