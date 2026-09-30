package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppRole {
    PARENT,
    CHILD
}

enum class DeviceOnlineStatus {
    ONLINE,
    IDLE,
    OFFLINE
}

enum class AlertType {
    EXCESSIVE_SCREEN_TIME,
    DEVICE_OFFLINE,
    LOW_BATTERY,
    GEOFENCE_EXIT,
    GEOFENCE_ENTER,
    BLOCKED_WEBSITE_ATTEMPT,
    PERMISSION_CHANGED,
    NEW_APP_INSTALLED,
    EMERGENCY_SOS
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum class RequestType {
    SCREEN_TIME_EXTENSION,
    APP_UNLOCK,
    WEBSITE_UNLOCK,
    PERMISSION_REQUEST
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val name: String,
    val age: Int,
    val avatarEmoji: String,
    val deviceModel: String,
    val onlineStatus: DeviceOnlineStatus,
    val batteryPct: Int,
    val isCharging: Boolean,
    val lastSeenTimestamp: Long,
    val todayUsedMinutes: Int,
    val dailyLimitMinutes: Int,
    val isDevicePaused: Boolean,
    val isBedtimeActive: Boolean,
    val isStudyModeActive: Boolean,
    val isLocationSharingEnabled: Boolean,
    val isScreenViewingActive: Boolean,
    val locationAddress: String,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "screen_time_rules")
data class ScreenTimeRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: String,
    val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val maxMinutes: Int,
    val bedTimeStart: String, // e.g. "21:30"
    val bedTimeEnd: String,   // e.g. "07:00"
    val studyHoursStart: String, // e.g. "08:30"
    val studyHoursEnd: String,   // e.g. "15:00"
    val isEnabled: Boolean
)

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: String,
    val appName: String,
    val packageName: String,
    val category: String, // "Social", "Gaming", "Education", "Entertainment", "Utility"
    val iconEmoji: String,
    val usedMinutesToday: Int,
    val limitMinutes: Int, // 0 = no specific limit
    val isBlocked: Boolean,
    val isAlwaysAllowed: Boolean
)

@Entity(tableName = "web_rules")
data class WebRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: String,
    val urlOrDomain: String,
    val category: String, // "Social Media", "Gaming", "Adult", "Video Streaming", "Education", "General"
    val isBlocked: Boolean,
    val isCustomRule: Boolean
)

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    val isSafeZone: Boolean,
    val notifyOnEnter: Boolean,
    val notifyOnExit: Boolean,
    val iconEmoji: String
)

@Entity(tableName = "location_events")
data class LocationEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val timestamp: Long,
    val eventType: String // "Regular Ping", "Safe Zone Arrival", "Safe Zone Departure", "SOS Trigger"
)

@Entity(tableName = "safety_alerts")
data class SafetyAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val timestamp: Long,
    val severity: AlertSeverity,
    val isRead: Boolean
)

@Entity(tableName = "access_requests")
data class AccessRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: String,
    val type: RequestType,
    val targetName: String,
    val requestedMinutes: Int,
    val reason: String,
    val status: RequestStatus,
    val createdAt: Long,
    val parentResponseNotes: String = ""
)

@Entity(tableName = "pairing_codes")
data class PairingCodeEntity(
    @PrimaryKey val code: String, // 6-digit code e.g. "829410"
    val childId: String,
    val childName: String,
    val deviceModel: String,
    val expiresAt: Long
)
