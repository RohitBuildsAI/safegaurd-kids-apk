package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun ActiveMonitoringBanner(
    isChildMode: Boolean,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SafeGuardCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onPrivacyClick() }
            .testTag("banner_active_monitoring"),
        colors = CardDefaults.cardColors(
            containerColor = SafeGuardCyan.copy(alpha = 0.08f)
        )
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SafeGuardCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified Transparent Monitoring",
                        tint = SafeGuardCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isChildMode) "Transparent Protection Active" else "SafeGuard Protected Device",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isChildMode)
                            "Protected with your knowledge • Official Android Wellbeing APIs"
                        else
                            "End-to-End Encrypted • Strict Zero-Bypass Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(
                onClick = onPrivacyClick,
                modifier = Modifier.testTag("button_view_privacy_banner")
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SafeGuardCyan
                )
            }
        }
    }
}

@Composable
fun ScreenTimeGauge(
    usedMinutes: Int,
    limitMinutes: Int,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (limitMinutes > 0) (usedMinutes.toFloat() / limitMinutes).coerceIn(0f, 1f) else 0f
    val remainingMinutes = (limitMinutes - usedMinutes).coerceAtLeast(0)
    val gaugeColor = when {
        isPaused -> AlertRose
        progress > 0.9f -> AlertRose
        progress > 0.75f -> WarningAmber
        else -> SafeGreen
    }

    Box(
        modifier = modifier
            .size(190.dp)
            .testTag("gauge_screen_time"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(gaugeColor.copy(alpha = 0.8f), gaugeColor)
                ),
                startAngle = 135f,
                sweepAngle = 270f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isPaused) {
                Icon(
                    imageVector = Icons.Default.PauseCircle,
                    contentDescription = "Device Paused",
                    tint = AlertRose,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = AlertRose
                )
                Text(
                    text = "Focus Lock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "${usedMinutes / 60}h ${usedMinutes % 60}m",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "of ${limitMinutes / 60}h ${limitMinutes % 60}m limit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = gaugeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$remainingMinutes min left",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceStatusCard(
    child: ChildProfileEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("card_device_status"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = child.avatarEmoji,
                        fontSize = 24.sp
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = child.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (child.onlineStatus) {
                                        DeviceOnlineStatus.ONLINE -> SafeGreen
                                        DeviceOnlineStatus.IDLE -> WarningAmber
                                        DeviceOnlineStatus.OFFLINE -> Color.Gray
                                    }
                                )
                        )
                        Text(
                            text = child.onlineStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = child.deviceModel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Battery & Wifi
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (child.batteryPct < 20) AlertRose.copy(alpha = 0.15f) else SafeGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (child.batteryPct > 50) Icons.Default.BatteryChargingFull else Icons.Default.Battery3Bar,
                            contentDescription = "Battery",
                            tint = if (child.batteryPct < 20) AlertRose else SafeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${child.batteryPct}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (child.batteryPct < 20) AlertRose else SafeGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SosEmergencyDialog(
    child: ChildProfileEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AlertRose),
                modifier = Modifier.testTag("button_dismiss_sos")
            ) {
                Text("Acknowledge & Respond")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("button_close_sos_dialog")
            ) {
                Text("Dismiss")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "SOS Alert",
                    tint = AlertRose
                )
                Text(
                    text = "EMERGENCY SOS ALERT",
                    fontWeight = FontWeight.Black,
                    color = AlertRose
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${child.name} has triggered an urgent emergency distress signal.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlertRose.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "📍 Location: ${child.locationAddress}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🔋 Device Battery: ${child.batteryPct}% • ${child.deviceModel}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "⏱️ Time: Just now (Live sharing active)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}
