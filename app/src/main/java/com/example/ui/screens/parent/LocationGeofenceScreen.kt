package com.example.ui.screens.parent

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeofenceEntity
import com.example.data.model.LocationEventEntity
import com.example.ui.theme.*
import com.example.viewmodel.SafeGuardUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationGeofenceScreen(
    state: SafeGuardUiState,
    onToggleLocationSharing: (String) -> Unit,
    onAddGeofence: (String, String, Int, String) -> Unit,
    onDeleteGeofence: (GeofenceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val child = state.activeChild ?: return
    var showAddPlaceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Live Location & Safe Places",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Consent-based location sharing with geofence arrival and departure alerts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Location Card with Canvas Map Simulation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("card_live_location_map"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SafeGreen)
                            )
                            Text(
                                text = "Live Sharing Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SafeGuardCyan.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "GPS High Accuracy",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeGuardCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Map View Visualizer Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .testTag("box_simulated_map"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2

                            // Map grid lines
                            val gridSpacing = 40.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                drawLine(
                                    color = Color(0xFF1E293B),
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1f
                                )
                                x += gridSpacing
                            }
                            var y = 0f
                            while (y < size.height) {
                                drawLine(
                                    color = Color(0xFF1E293B),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f
                                )
                                y += gridSpacing
                            }

                            // Geofence rings (e.g. School, Home)
                            drawCircle(
                                color = SafeGuardCyan.copy(alpha = 0.15f),
                                radius = 70.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                            drawCircle(
                                color = SafeGuardCyan,
                                radius = 70.dp.toPx(),
                                center = Offset(centerX, centerY),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Child location marker & pulse
                            drawCircle(
                                color = SafeGreen.copy(alpha = 0.35f),
                                radius = 18.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                            drawCircle(
                                color = SafeGreen,
                                radius = 8.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                        }

                        // Child Avatar Pill overlay on map
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-32).dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(child.avatarEmoji, fontSize = 16.sp)
                                Text(
                                    text = "${child.name} (Now)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Current Address
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = child.locationAddress,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Coordinates: ${"%.4f".format(child.latitude)}, ${"%.4f".format(child.longitude)} • Updated 2 mins ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Safe Places (Geofences) Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Safe Places (Geofences)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant alerts when ${child.name} enters or leaves",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddPlaceDialog = true },
                    modifier = Modifier.testTag("button_add_safe_place")
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Place")
                }
            }
        }

        // Geofences List
        items(state.geofences) { geofence ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("card_geofence_${geofence.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        Text(text = geofence.iconEmoji, fontSize = 26.sp)
                        Column {
                            Text(
                                text = geofence.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = geofence.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${geofence.radiusMeters}m radius • Alerts on Enter & Exit",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeGuardCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDeleteGeofence(geofence) },
                        modifier = Modifier.testTag("button_delete_geofence_${geofence.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete safe place",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Location History Breadcrumbs Header
        item {
            Text(
                text = "Recent Location History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Location Events List
        items(state.locationEvents) { event ->
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (event.eventType.contains("Arrival")) Icons.Default.CheckCircle else Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = if (event.eventType.contains("Arrival")) SafeGreen else SafeGuardCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.locationName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = event.eventType,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = timeFormat.format(Date(event.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Add Safe Place Dialog
    if (showAddPlaceDialog) {
        var placeNameInput by remember { mutableStateOf("") }
        var addressInput by remember { mutableStateOf("") }
        var radiusMetersInput by remember { mutableIntStateOf(150) }
        var emojiInput by remember { mutableStateOf("🏡") }

        AlertDialog(
            onDismissRequest = { showAddPlaceDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (placeNameInput.isNotBlank()) {
                            onAddGeofence(placeNameInput, addressInput.ifBlank { "Safe Zone Area" }, radiusMetersInput, emojiInput)
                            showAddPlaceDialog = false
                        }
                    },
                    modifier = Modifier.testTag("button_confirm_add_geofence")
                ) {
                    Text("Save Place")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddPlaceDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Add Safe Place") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = placeNameInput,
                        onValueChange = { placeNameInput = it },
                        label = { Text("Place Name (e.g. Grandma's House)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_place_name")
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Address / Landmark") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_place_address")
                    )
                    OutlinedTextField(
                        value = emojiInput,
                        onValueChange = { emojiInput = it },
                        label = { Text("Icon Emoji (e.g. 🏠, 🏫, ⚽, 🌳)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_place_emoji")
                    )
                    Text(
                        text = "Geofence Radius: $radiusMetersInput meters",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = radiusMetersInput.toFloat(),
                        onValueChange = { radiusMetersInput = it.toInt() },
                        valueRange = 50f..500f,
                        steps = 8
                    )
                }
            }
        )
    }
}
