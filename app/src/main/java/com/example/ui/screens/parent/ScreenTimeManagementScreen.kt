package com.example.ui.screens.parent

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildProfileEntity
import com.example.data.model.ScreenTimeRuleEntity
import com.example.ui.theme.*
import com.example.viewmodel.SafeGuardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeManagementScreen(
    state: SafeGuardUiState,
    onSetDailyLimit: (String, Int) -> Unit,
    onTogglePause: (String) -> Unit,
    onToggleBedtime: (String) -> Unit,
    onToggleStudyMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val child = state.activeChild ?: return
    var selectedPeriod by remember { mutableStateOf("Today") }
    var showLimitDialog by remember { mutableStateOf(false) }
    var currentSliderValue by remember(child.dailyLimitMinutes) { mutableFloatStateOf(child.dailyLimitMinutes.toFloat()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Period Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Screen Time Management",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure healthy boundaries, schedules, and view usage reports for ${child.name}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Period Tabs
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Today", "This Week", "Monthly Report").forEachIndexed { index, period ->
                        SegmentedButton(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            modifier = Modifier.testTag("tab_period_$index")
                        ) {
                            Text(period)
                        }
                    }
                }
            }
        }

        // Daily Limit Configuration Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("card_daily_limit_config"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Daily Screen Time Limit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Applies across all non-essential apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SafeGuardBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${(currentSliderValue / 60).toInt()}h ${(currentSliderValue % 60).toInt()}m",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = SafeGuardBlue
                            )
                        }
                    }

                    // Slider
                    Slider(
                        value = currentSliderValue,
                        onValueChange = { currentSliderValue = it },
                        onValueChangeFinished = {
                            onSetDailyLimit(child.id, currentSliderValue.toInt())
                        },
                        valueRange = 30f..360f,
                        steps = 10,
                        modifier = Modifier.testTag("slider_daily_limit"),
                        colors = SliderDefaults.colors(
                            thumbColor = SafeGuardBlue,
                            activeTrackColor = SafeGuardBlue
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30 min (Strict)", style = MaterialTheme.typography.labelSmall)
                        Text("3 hours (Standard)", style = MaterialTheme.typography.labelSmall)
                        Text("6 hours (Flexible)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Weekly Usage Bar Chart (Jetpack Compose Canvas)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("card_weekly_chart"),
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Activity Pattern",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Avg: 2h 12m / day",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Chart Canvas
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val dayMinutes = listOf(110, 135, 95, 140, 160, 195, 130)
                    val maxLimitMinutes = child.dailyLimitMinutes.toFloat()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(top = 10.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = 24.dp.toPx()
                            val spacing = (size.width - (barWidth * days.size)) / (days.size + 1)
                            val chartHeight = size.height - 30.dp.toPx()

                            // Baseline Limit Line
                            val limitY = chartHeight * (1f - (maxLimitMinutes / 240f).coerceIn(0f, 1f))
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.4f),
                                start = Offset(0f, limitY),
                                end = Offset(size.width, limitY),
                                strokeWidth = 2.dp.toPx()
                            )

                            // Bars
                            days.forEachIndexed { i, _ ->
                                val mins = dayMinutes[i]
                                val barHeight = (chartHeight * (mins.toFloat() / 240f)).coerceAtLeast(10f)
                                val x = spacing + i * (barWidth + spacing)
                                val y = chartHeight - barHeight

                                val barColor = when {
                                    mins > maxLimitMinutes -> AlertRose
                                    mins > maxLimitMinutes * 0.8f -> WarningAmber
                                    else -> SafeGuardCyan
                                }

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }

                        // Day Labels Row below Canvas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            days.forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(SafeGuardCyan, RoundedCornerShape(2.dp)))
                            Text("Within Limit", style = MaterialTheme.typography.labelSmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(AlertRose, RoundedCornerShape(2.dp)))
                            Text("Exceeded Limit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Active Schedules & Modes
        item {
            Text(
                text = "Automated Schedules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // School/Study Hours
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SafeGuardCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = SafeGuardCyan
                            )
                        }
                        Column {
                            Text(
                                text = "School & Study Hours",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mon–Fri • 08:30 AM – 03:00 PM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Only educational & emergency apps allowed",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeGuardCyan
                            )
                        }
                    }
                    Switch(
                        checked = child.isStudyModeActive,
                        onCheckedChange = { onToggleStudyMode(child.id) },
                        modifier = Modifier.testTag("switch_study_hours")
                    )
                }
            }
        }

        // Bedtime Schedule
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PurpleAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = PurpleAccent
                            )
                        }
                        Column {
                            Text(
                                text = "Bedtime Downtime",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Every night • 09:30 PM – 07:00 AM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Device locked for sleep; Phone calls always allowed",
                                style = MaterialTheme.typography.labelSmall,
                                color = PurpleAccent
                            )
                        }
                    }
                    Switch(
                        checked = child.isBedtimeActive,
                        onCheckedChange = { onToggleBedtime(child.id) },
                        modifier = Modifier.testTag("switch_bedtime")
                    )
                }
            }
        }

        // Instant Pause Device Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (child.isDevicePaused) AlertRose.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                )
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (child.isDevicePaused) AlertRose else Color.Gray.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (child.isDevicePaused) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (child.isDevicePaused) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text(
                                text = if (child.isDevicePaused) "Device Currently Paused" else "Family Focus Pause",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (child.isDevicePaused) AlertRose else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (child.isDevicePaused) "Child screen is locked. Tap to resume." else "Instantly lock device for dinner or family time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { onTogglePause(child.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (child.isDevicePaused) SafeGreen else AlertRose
                        ),
                        modifier = Modifier.testTag("button_screen_time_pause_toggle")
                    ) {
                        Text(if (child.isDevicePaused) "Resume" else "Pause Now")
                    }
                }
            }
        }

        // Daily Rules Breakdown (Mon-Sun)
        item {
            Text(
                text = "Rules By Day of Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.screenTimeRules) { rule ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SafeGuardBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = rule.dayOfWeek,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SafeGuardBlue
                            )
                        }
                        Column {
                            Text(
                                text = "${rule.maxMinutes / 60}h ${rule.maxMinutes % 60}m allowed",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Bedtime: ${rule.bedTimeStart} • Study: ${rule.studyHoursStart}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (rule.isEnabled) SafeGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (rule.isEnabled) "Active" else "Disabled",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (rule.isEnabled) SafeGreen else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
