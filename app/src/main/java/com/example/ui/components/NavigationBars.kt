package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRole
import com.example.data.model.ChildProfileEntity
import com.example.ui.theme.AlertRose
import com.example.ui.theme.SafeGuardBlue
import com.example.ui.theme.SafeGuardCyan
import com.example.viewmodel.ChildNavDestination
import com.example.viewmodel.ParentNavDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeGuardTopAppBar(
    currentRole: AppRole,
    children: List<ChildProfileEntity>,
    selectedChildId: String,
    unreadAlertsCount: Int,
    pendingRequestsCount: Int,
    onSelectRole: (AppRole) -> Unit,
    onSelectChild: (String) -> Unit,
    onAddChildClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Row: App Name & Role Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SafeGuardBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "SafeGuard Kids",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SafeGuard Kids",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentRole == AppRole.PARENT) "Parent Guardian App" else "Teen/Child Companion",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeGuardCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Switcher Pill between Parent and Child Mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (currentRole == AppRole.PARENT) SafeGuardBlue else Color.Transparent)
                                    .clickable { onSelectRole(AppRole.PARENT) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("pill_parent_mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Parent",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentRole == AppRole.PARENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (currentRole == AppRole.CHILD) SafeGuardCyan else Color.Transparent)
                                    .clickable { onSelectRole(AppRole.CHILD) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("pill_child_mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Child",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentRole == AppRole.CHILD) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Privacy Button
                    IconButton(
                        onClick = onPrivacyClick,
                        modifier = Modifier.testTag("button_open_privacy_top")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = "Privacy & Encryption Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Child Profile Selector Chips (in Parent Mode)
            if (currentRole == AppRole.PARENT && children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    children.forEach { child ->
                        val isSelected = child.id == selectedChildId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectChild(child.id) },
                            label = {
                                Text(
                                    text = "${child.avatarEmoji} ${child.name}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SafeGuardBlue.copy(alpha = 0.15f),
                                selectedLabelColor = SafeGuardBlue
                            ),
                            modifier = Modifier.testTag("chip_child_${child.id}")
                        )
                    }

                    // Add Child Chip
                    AssistChip(
                        onClick = onAddChildClick,
                        label = { Text("+ Add Child") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add child",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("chip_add_child")
                    )
                }
            }
        }
    }
}

@Composable
fun ParentBottomNavigationBar(
    currentDestination: ParentNavDestination,
    unreadAlertsCount: Int,
    pendingRequestsCount: Int,
    onNavigate: (ParentNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("nav_bar_parent")
    ) {
        NavigationBarItem(
            selected = currentDestination == ParentNavDestination.DASHBOARD,
            onClick = { onNavigate(ParentNavDestination.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ParentNavDestination.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Overview"
                )
            },
            label = { Text("Overview") },
            modifier = Modifier.testTag("nav_overview")
        )
        NavigationBarItem(
            selected = currentDestination == ParentNavDestination.SCREEN_TIME,
            onClick = { onNavigate(ParentNavDestination.SCREEN_TIME) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ParentNavDestination.SCREEN_TIME) Icons.Filled.HourglassBottom else Icons.Outlined.HourglassEmpty,
                    contentDescription = "Screen Time"
                )
            },
            label = { Text("Screen Time") },
            modifier = Modifier.testTag("nav_screen_time")
        )
        NavigationBarItem(
            selected = currentDestination == ParentNavDestination.APPS || currentDestination == ParentNavDestination.WEBSITES,
            onClick = { onNavigate(ParentNavDestination.APPS) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ParentNavDestination.APPS) Icons.Filled.Apps else Icons.Outlined.Apps,
                    contentDescription = "Apps & Web"
                )
            },
            label = { Text("Controls") },
            modifier = Modifier.testTag("nav_apps")
        )
        NavigationBarItem(
            selected = currentDestination == ParentNavDestination.LOCATION,
            onClick = { onNavigate(ParentNavDestination.LOCATION) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ParentNavDestination.LOCATION) Icons.Filled.LocationOn else Icons.Outlined.LocationOn,
                    contentDescription = "Location"
                )
            },
            label = { Text("Location") },
            modifier = Modifier.testTag("nav_location")
        )
        NavigationBarItem(
            selected = currentDestination == ParentNavDestination.ALERTS || currentDestination == ParentNavDestination.REQUESTS,
            onClick = { onNavigate(ParentNavDestination.ALERTS) },
            icon = {
                BadgedBox(
                    badge = {
                        val count = unreadAlertsCount + pendingRequestsCount
                        if (count > 0) {
                            Badge(containerColor = AlertRose) {
                                Text(count.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentDestination == ParentNavDestination.ALERTS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Alerts"
                    )
                }
            },
            label = { Text("Alerts") },
            modifier = Modifier.testTag("nav_alerts")
        )
    }
}

@Composable
fun ChildBottomNavigationBar(
    currentDestination: ChildNavDestination,
    onNavigate: (ChildNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("nav_bar_child")
    ) {
        NavigationBarItem(
            selected = currentDestination == ChildNavDestination.DASHBOARD,
            onClick = { onNavigate(ChildNavDestination.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ChildNavDestination.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "My Wellbeing"
                )
            },
            label = { Text("My Time") },
            modifier = Modifier.testTag("nav_child_time")
        )
        NavigationBarItem(
            selected = currentDestination == ChildNavDestination.REQUESTS,
            onClick = { onNavigate(ChildNavDestination.REQUESTS) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ChildNavDestination.REQUESTS) Icons.Filled.Send else Icons.Outlined.Send,
                    contentDescription = "Requests"
                )
            },
            label = { Text("Ask Parent") },
            modifier = Modifier.testTag("nav_child_requests")
        )
        NavigationBarItem(
            selected = currentDestination == ChildNavDestination.SAFETY,
            onClick = { onNavigate(ChildNavDestination.SAFETY) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ChildNavDestination.SAFETY) Icons.Filled.Shield else Icons.Outlined.Shield,
                    contentDescription = "Safety & SOS"
                )
            },
            label = { Text("Safety & SOS") },
            modifier = Modifier.testTag("nav_child_safety")
        )
        NavigationBarItem(
            selected = currentDestination == ChildNavDestination.PRIVACY,
            onClick = { onNavigate(ChildNavDestination.PRIVACY) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == ChildNavDestination.PRIVACY) Icons.Filled.Lock else Icons.Outlined.Lock,
                    contentDescription = "My Privacy"
                )
            },
            label = { Text("Privacy") },
            modifier = Modifier.testTag("nav_child_privacy")
        )
    }
}
