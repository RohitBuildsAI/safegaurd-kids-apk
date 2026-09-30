package com.example.ui.screens.parent

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AppRuleEntity
import com.example.data.model.WebRuleEntity
import com.example.ui.theme.*
import com.example.viewmodel.SafeGuardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagementScreen(
    state: SafeGuardUiState,
    onSetAppBlocked: (Long, Boolean) -> Unit,
    onSetAppLimit: (Long, Int) -> Unit,
    onSetAppAlwaysAllowed: (Long, Boolean) -> Unit,
    onAddWebRule: (String, String, Boolean) -> Unit,
    onToggleWebBlocked: (Long, Boolean) -> Unit,
    onDeleteWebRule: (WebRuleEntity) -> Unit,
    onToggleSafeSearch: () -> Unit,
    onToggleStudyBrowsing: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Apps, 1 = Websites
    var appCategoryFilter by remember { mutableStateOf("All") }
    var showAddWebRuleDialog by remember { mutableStateOf(false) }
    var selectedAppForLimit by remember { mutableStateOf<AppRuleEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "App & Website Controls",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set daily limits, block distracting content, and manage safe browsing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Apps (${state.appRules.size})", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                        modifier = Modifier.testTag("tab_apps")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Websites (${state.webRules.size})", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Language, contentDescription = null) },
                        modifier = Modifier.testTag("tab_websites")
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // APPS TAB
            item {
                // Category Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Social", "Gaming", "Education", "Entertainment").forEach { cat ->
                        FilterChip(
                            selected = appCategoryFilter == cat,
                            onClick = { appCategoryFilter = cat },
                            label = { Text(cat) },
                            modifier = Modifier.testTag("chip_cat_$cat")
                        )
                    }
                }
            }

            val filteredApps = if (appCategoryFilter == "All") {
                state.appRules
            } else {
                state.appRules.filter { it.category.equals(appCategoryFilter, ignoreCase = true) }
            }

            items(filteredApps) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("card_app_${app.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = app.iconEmoji, fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${app.category} • ${app.usedMinutesToday} mins used today",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Status badge
                            when {
                                app.isBlocked -> {
                                    Surface(shape = RoundedCornerShape(8.dp), color = AlertRose.copy(alpha = 0.15f)) {
                                        Text(
                                            text = "Blocked",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AlertRose
                                        )
                                    }
                                }
                                app.isAlwaysAllowed -> {
                                    Surface(shape = RoundedCornerShape(8.dp), color = SafeGreen.copy(alpha = 0.15f)) {
                                        Text(
                                            text = "Always Allowed",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SafeGreen
                                        )
                                    }
                                }
                                app.limitMinutes > 0 -> {
                                    Surface(shape = RoundedCornerShape(8.dp), color = SafeGuardCyan.copy(alpha = 0.15f)) {
                                        Text(
                                            text = "Limit: ${app.limitMinutes}m",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SafeGuardCyan
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Block/Unblock Button
                            OutlinedButton(
                                onClick = { onSetAppBlocked(app.id, !app.isBlocked) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_block_${app.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (app.isBlocked) SafeGreen else AlertRose
                                )
                            ) {
                                Text(if (app.isBlocked) "Unblock" else "Block")
                            }

                            // Set Limit Button
                            OutlinedButton(
                                onClick = { selectedAppForLimit = app },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_limit_${app.id}")
                            ) {
                                Text("Set Limit")
                            }

                            // Always Allowed Toggle
                            OutlinedButton(
                                onClick = { onSetAppAlwaysAllowed(app.id, !app.isAlwaysAllowed) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_always_allowed_${app.id}")
                            ) {
                                Text(if (app.isAlwaysAllowed) "Standard" else "Allow Always")
                            }
                        }
                    }
                }
            }
        } else {
            // WEBSITES TAB
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Browser Protection Rules",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // SafeSearch Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SafeSearch & Restricted Mode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Filters explicit content on Google, Bing, and YouTube",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.safeSearchEnabled,
                                onCheckedChange = { onToggleSafeSearch() },
                                modifier = Modifier.testTag("switch_safesearch")
                            )
                        }

                        HorizontalDivider()

                        // Study-Only Browsing Mode Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Study-Only Browsing Mode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Only allows whitelisted educational domains; all other sites blocked",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.studyBrowsingMode,
                                onCheckedChange = { onToggleStudyBrowsing() },
                                modifier = Modifier.testTag("switch_study_browsing")
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Website Rules List",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddWebRuleDialog = true },
                        modifier = Modifier.testTag("button_add_web_rule")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Domain")
                    }
                }
            }

            items(state.webRules) { webRule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("card_web_rule_${webRule.id}"),
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
                            Icon(
                                imageVector = if (webRule.isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (webRule.isBlocked) AlertRose else SafeGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = webRule.urlOrDomain,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${webRule.category} • ${if (webRule.isBlocked) "Blocked" else "Allowed"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onToggleWebBlocked(webRule.id, webRule.isBlocked) },
                                modifier = Modifier.testTag("button_toggle_web_${webRule.id}")
                            ) {
                                Icon(
                                    imageVector = if (webRule.isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = "Toggle rule status",
                                    tint = if (webRule.isBlocked) SafeGreen else AlertRose
                                )
                            }
                            IconButton(
                                onClick = { onDeleteWebRule(webRule) },
                                modifier = Modifier.testTag("button_delete_web_${webRule.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Web Rule Dialog
    if (showAddWebRuleDialog) {
        var domainInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Education") }
        var isBlockedInput by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddWebRuleDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (domainInput.isNotBlank()) {
                            onAddWebRule(domainInput, categoryInput, isBlockedInput)
                            showAddWebRuleDialog = false
                        }
                    },
                    modifier = Modifier.testTag("button_confirm_add_web_rule")
                ) {
                    Text("Save Rule")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddWebRuleDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Add Website Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = domainInput,
                        onValueChange = { domainInput = it },
                        label = { Text("Domain or URL (e.g. reddit.com)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_web_domain")
                    )
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Category (e.g. Social, Gaming, Video)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_web_category")
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isBlockedInput,
                            onCheckedChange = { isBlockedInput = it },
                            modifier = Modifier.testTag("checkbox_is_blocked")
                        )
                        Text("Block this website")
                    }
                }
            }
        )
    }

    // Set App Limit Dialog
    selectedAppForLimit?.let { app ->
        var limitMinutesInput by remember { mutableIntStateOf(if (app.limitMinutes > 0) app.limitMinutes else 30) }

        AlertDialog(
            onDismissRequest = { selectedAppForLimit = null },
            confirmButton = {
                Button(
                    onClick = {
                        onSetAppLimit(app.id, limitMinutesInput)
                        selectedAppForLimit = null
                    },
                    modifier = Modifier.testTag("button_save_app_limit")
                ) {
                    Text("Apply Limit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedAppForLimit = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Set Daily Limit for ${app.appName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Current daily limit: $limitMinutesInput minutes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = limitMinutesInput.toFloat(),
                        onValueChange = { limitMinutesInput = it.toInt() },
                        valueRange = 10f..180f,
                        steps = 16,
                        modifier = Modifier.testTag("slider_app_limit")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OutlinedButton(onClick = { limitMinutesInput = 15 }) { Text("15m") }
                        OutlinedButton(onClick = { limitMinutesInput = 30 }) { Text("30m") }
                        OutlinedButton(onClick = { limitMinutesInput = 60 }) { Text("1h") }
                    }
                }
            }
        )
    }
}
